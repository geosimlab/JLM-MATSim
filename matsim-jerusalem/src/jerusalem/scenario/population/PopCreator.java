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
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import jerusalem.scenario.network.CreateNetwork;

/**
 * @author Ido Klein
 */
public class PopCreator {

	private final static String OUTPUT_POPULATION_CSV = "D:/Users/User/Dropbox/matsim_begin/population.xml";
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final String path = "src/database.properties";
	private static final Properties props = DbUtils.readProperties(path);
	private static final String user = props.getProperty("db.username");
	private static final String password = props.getProperty("db.password");
	private static final String db_url = props.getProperty("db.url");
	private static final String port = props.getProperty("db.port");
	private static final String db_name = props.getProperty("db.db_name");
	private static final String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name;
	private static final String pathQuery = "./sql_scripts/pop_query.sql";

	public static void main(String[] args) {
		// read pop_query.sql
		String query = readQueryFile(pathQuery);

		try (Connection con = DriverManager.getConnection(url, user, password);
				PreparedStatement pst = con.prepareStatement(query);
				ResultSet resultSet = pst.executeQuery()) {
			Config config = ConfigUtils.createConfig();
			Scenario sc = ScenarioUtils.createScenario(config);
			Network network = sc.getNetwork();
			Population population = sc.getPopulation();
			PopulationFactory populationFactory = population.getFactory();

			// useless definition of person and plan
			Person person = populationFactory.createPerson(Id.create("1", Person.class));
			Plan plan = populationFactory.createPlan();
			int i = 0;
			while (resultSet.next()) {

				// creating a new person if one started
				if (resultSet.getInt("personTripNum") == 1) {
					String agentId = resultSet.getString("hhid") + "-" + resultSet.getString("pnum");
					person = populationFactory.createPerson(Id.create(agentId, Person.class));
					population.addPerson(person);
					plan = populationFactory.createPlan();
				}

				// getting parameters from table
				Coord origCoordinates = new Coord(resultSet.getDouble("origX"), resultSet.getDouble("origY"));
				String actType = PopUtils.ActivityType(resultSet.getInt("origPurp"));
				double endTime = resultSet.getDouble("finalDepartMinute") * 60 + 10800;
				String mode = PopUtils.Mode(resultSet.getInt("modeCode"));

				// adding activity and leg
				Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
				activity.setEndTime(endTime);
				plan.addActivity(activity);
				plan.addLeg(populationFactory.createLeg(mode));

				// last activity - adding person to population
				if (resultSet.getInt("personTripNum") == resultSet.getInt("lastTripNum")) {
					Coord destCoordinates = new Coord(resultSet.getDouble("destX"), resultSet.getDouble("destY"));
					actType = PopUtils.ActivityType(resultSet.getInt("destPurp"));
					activity = populationFactory.createActivityFromCoord(actType, destCoordinates);
					plan.addActivity(activity);
					person.addPlan(plan);
				}
				System.out.println(i);
				i++;
			}

			// writing population
			new PopulationWriter(population, network).write(OUTPUT_POPULATION_CSV);

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static String readQueryFile(String path) {
		log.info("reading load_csv.sql");
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
}