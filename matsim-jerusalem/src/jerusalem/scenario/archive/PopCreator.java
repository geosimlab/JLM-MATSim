package jerusalem.scenario.archive;

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
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;
import jerusalem.scenario.network.CreateNetwork;

/**
 * @author Ido Klein
 */
public class PopCreator {

	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final Properties props = DbUtils.readProperties("database.properties");
	private final static String POPULATION_ID = "" + 3;
	public final static String POPULATION_OUTPUT_PATH = props.getProperty("folder.output_folder") + POPULATION_ID
			+ ".population.xml.gz";
	private static final String pathQuery = "./sql_scripts/pop_query.sql";
	private static Scenario sc;
	private static Population population;
	private static PopulationFactory populationFactory;
	private static Person person;
	private static Plan plan;

	public static void main(String[] args) throws SQLException {

		// initial setup
		initialPopulationSetup();

		// read pop_query.sql
		String query = readQueryFile(pathQuery);

		// read population from database
		readPopulation(query);
		// FIXME Change all logic - first read persons, then assign to households, then
		// add activities and legs
		// write population
		new PopulationWriter(sc.getPopulation(), sc.getNetwork()).write(POPULATION_OUTPUT_PATH);
	}

	/**
	 * Reads in pop_query.sql - query that returns all separate trips of all agents
	 * 
	 * @param path
	 * @return
	 */
//	TODO get rid of this function, move query to sql view/table
	private static String readQueryFile(String path) {
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
	 * @throws SQLException
	 */
	private static void readPopulation(String query) throws SQLException {
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		con.setAutoCommit(false);
		PreparedStatement pst = con.prepareStatement(query);
		pst.setFetchSize(50000);
		ResultSet resultSet = pst.executeQuery();
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
		con.close();
	}

	/**
	 * Method to initialize population generator
	 */
//	TODO remove, this is unnecessary 
	private static void initialPopulationSetup() {
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
	private static void createAgent(ResultSet resultSet) throws SQLException {
		// TODO this has to change - move as many attributes as possible to households
		String agentId = resultSet.getString("hhid") + "-" + resultSet.getString("pnum");
		person = populationFactory.createPerson(Id.create(agentId, Person.class));
		// setting attributes
		PersonUtils.setAge(person, resultSet.getInt("age"));// persons.age
		PersonUtils.setSex(person, resultSet.getInt("gender") == 1 ? "male" : "female");// persons.gender
		PersonUtils.setEmployed(person, PopUtils.Employed(resultSet.getInt("perstypedetailed")));// persons.persTypeDetailed
		PersonUtils.setLicence(person, resultSet.getInt("driverlicense") == 1 ? "yes" : "no");// persons.driverLicense
		PersonUtils.setCarAvail(person,
				PopUtils.CarAvail(resultSet.getInt("numauto"), resultSet.getInt("usualDriver")));// households.numauto,persons.usualDriver
		person.getAttributes().putAttribute("sector", PopUtils.Sector(resultSet.getInt("sector")));// households.sector
		population.addPerson(person);
		plan = populationFactory.createPlan();
	}

	/**
	 * method to create activity and leg to agent for each row
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	private static void addActivityAndLeg(ResultSet resultSet) throws SQLException {
		// TODO clean and improve logic, get rid of centroids
		// when agent only stays at home
		if (resultSet.getInt("personTripNum") == 0) {
			Coord origCoordinates = new Coord(resultSet.getDouble("homeX"), resultSet.getDouble("homeY"));
			String actType = PopUtils.ActivityType(0);// code for home
			Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
			plan.addActivity(activity);
			person.addPlan(plan);
		} else {
			String actType = PopUtils.ActivityType(resultSet.getInt("origPurp"));
			Coord origCoordinates;
//			TODO why am I doing this? I remember there was a good reason. figure it out
			resultSet.getDouble("actX");
			if (!resultSet.wasNull()) {
				origCoordinates = new Coord(resultSet.getDouble("actX"), resultSet.getDouble("actY"));
			} else if (actType.equals("home")) {
				origCoordinates = new Coord(resultSet.getDouble("homeX"), resultSet.getDouble("homeY"));
			} else {
				origCoordinates = new Coord(resultSet.getDouble("origX"), resultSet.getDouble("origY"));
			}

			// if activity is in home, use home coordinates
			double endTime = resultSet.getDouble("finalDepartMinute") * 60 + 3 * 60 * 60;
			String mode = PopUtils.Mode(resultSet.getInt("modeCode"));

			// adding activity and leg
			Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
			activity.setEndTime(endTime);
			plan.addActivity(activity);
			plan.addLeg(populationFactory.createLeg(mode));
			// TODO change logic to get rid of dest coordinates
			// last activity - adding destination activity and adding person to population
			if (resultSet.getInt("personTripNum") == resultSet.getInt("lastTripNum")) {
				Coord destCoordinates = new Coord(resultSet.getDouble("homeX"), resultSet.getDouble("homeY"));
				actType = PopUtils.ActivityType(resultSet.getInt("destPurp"));
				activity = populationFactory.createActivityFromCoord(actType, destCoordinates);
				plan.addActivity(activity);
				person.addPlan(plan);
			}
		}

	}
}