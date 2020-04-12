package jerusalem.scenario.population;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import jerusalem.scenario.network.CreateNetwork;

/**
 * @author Ido Klein
 */
public class PopCreator {

	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final String path = "src/database.properties";
	private static final Properties props = DbUtils.readProperties(path);
	private static final String user = props.getProperty("db.username");
	private static final String password = props.getProperty("db.password");
	private static final String db_url = props.getProperty("db.url");
	private static final String port = props.getProperty("db.port");
	private static final String db_name = props.getProperty("db.db_name");
	private static final String OUTPUT_POPULATION_XML = props.getProperty("db.output_population_xml_path");
	private static final String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name + "?loggerLevel=DEBUG";
	private static final String pathQuery = "./sql_scripts/pop_query.sql";
	private static Scenario sc;
	private static Population population;
	private static PopulationFactory populationFactory;
	private static Person person;
	private static Plan plan;

	public static void main(String[] args) {

		// initial setup
		initialPopulationSetup();

		// read pop_query.sql
		String query = readQueryFile(pathQuery);

		// read population from database
		readPopulation(query);

		// write population
		new PopulationWriter(sc.getPopulation(), sc.getNetwork()).write(OUTPUT_POPULATION_XML);
	}

	/**
	 * Reads in pop_query.sql - query that returns all separate trips of all agents
	 * TODO add idle agents - with only home activity (load persons table to db,
	 * join it, sql manipulation)
	 * 
	 * @param path
	 * @return
	 */
	public static String readQueryFile(String path) {
		log.info("reading pop_query.sql");
		BufferedReader br = null;
		String query = "";
		String line = "";

		try {
			br = new BufferedReader(new FileReader(path));

			while ((line = br.readLine()) != null) {
				System.out.println(line);
				query = query + line;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return query;
	}

	/**
	 * method that reads in sql query, sends it to db, creates agents to the
	 * population and adds activities to the agents
	 * 
	 * @param query
	 */
	public static void readPopulation(String query) {
		try (Connection con = DriverManager.getConnection(url, user, password);
				PreparedStatement pst = con.prepareStatement(query);
				ResultSet resultSet = pst.executeQuery()) {

			int i = 0;
			while (resultSet.next()) {

				// creating a new person if one started. 1 is for a person with at least one
				// trip, 0 is for a person that stays home
				boolean firstTrip = resultSet.getInt("personTripNum") == 1 | resultSet.getInt("personTripNum") == 0;
				if (firstTrip) {
					createAgent(resultSet);
				}
				addActivityAndLeg(resultSet);
				if (i % 100000 == 0) {
					log.info("read line #" + i + " from trips table");
				}
				i++;
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method to initialize population generator
	 */
	public static void initialPopulationSetup() {
		sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		population = sc.getPopulation();
		populationFactory = population.getFactory();
		log.info("Population Initialized");
	}

	/**
	 * Method to create new agents
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void createAgent(ResultSet resultSet) throws SQLException {
		String agentId = resultSet.getString("hhid") + "-" + resultSet.getString("pnum");
		person = populationFactory.createPerson(Id.create(agentId, Person.class));
		population.addPerson(person);
		plan = populationFactory.createPlan();
	}

	/**
	 * method to create activity and leg to agent for each row
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void addActivityAndLeg(ResultSet resultSet) throws SQLException {

		// when agent only stays at home
		if (resultSet.getInt("personTripNum") == 0) {
			Coord origCoordinates = new Coord(resultSet.getDouble("homeX"), resultSet.getDouble("homeY"));
			String actType = PopUtils.ActivityType(0);// code for home
			Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
			plan.addActivity(activity);
			person.addPlan(plan);
		} else {
			Coord origCoordinates = new Coord(resultSet.getDouble("origX"), resultSet.getDouble("origY"));
			String actType = PopUtils.ActivityType(resultSet.getInt("origPurp"));
			double endTime = resultSet.getDouble("finalDepartMinute") * 60 + 10800;
			String mode = PopUtils.Mode(resultSet.getInt("modeCode"));

			// adding activity and leg
			Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
			activity.setEndTime(endTime);
			plan.addActivity(activity);
			plan.addLeg(populationFactory.createLeg(mode));

			// last activity - adding destination activity and adding person to population
			if (resultSet.getInt("personTripNum") == resultSet.getInt("lastTripNum")) {
				Coord destCoordinates = new Coord(resultSet.getDouble("destX"), resultSet.getDouble("destY"));
				actType = PopUtils.ActivityType(resultSet.getInt("destPurp"));
				activity = populationFactory.createActivityFromCoord(actType, destCoordinates);
				plan.addActivity(activity);
				person.addPlan(plan);
			}
		}

	}
}