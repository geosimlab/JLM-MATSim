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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author Ido Klein
 */
public class PopCreator {

	private final static String INPUT_POPULATION_CSV = "D:/Users/User/Dropbox/matsim_begin/population.xml";

	public static void main(String[] args) {
//		TODO create local database through java
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "matsim";
		BufferedReader br = null;
		String query = "";
		String line = "";
		
		try {
			br = new BufferedReader(new FileReader("./sql_scripts/pop_query.sql"));

			while ((line = br.readLine()) != null) {
				query = query + line;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		System.out.println(query);
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

			// writing to file
			MatsimWriter popWriter = new PopulationWriter(population, network);
			popWriter.write(INPUT_POPULATION_CSV);

		} catch (SQLException ex) {

			Logger lgr = Logger.getLogger(PopCreator.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}