package jerusalem.scenario.population;

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

	public static void main(String[] args) {
//		TODO create local database through java
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "matsim";

		try (Connection con = DriverManager.getConnection(url, user, password);
				PreparedStatement pst = con.prepareStatement(
						"SELECT t.hhid,t.pnum,t.personTripNum,t.origTaz,t.destTaz,t.origPurp,t.destPurp,t.finalDepartMinute,t.modeCode,\r\n"
								+ "		tzo.X AS origX,tzo.Y AS origY,\r\n" + "		tzd.X AS destX,tzd.Y AS destY,\r\n"
								+ "		MAX(t.personTripNum) OVER (PARTITION BY t.hhid,t.pnum) AS lastTripNum\r\n"
								+ "FROM trips AS t\r\n" + "LEFT JOIN TAZ_coordinates as tzo\r\n"
								+ "ON t.origTaz = tzo.taz\r\n" + "LEFT JOIN TAZ_coordinates as tzd\r\n"
								+ "ON t.destTaz = tzd.taz\r\n" + "ORDER BY t.hhid,t.pnum,t.personTripNum;");
				ResultSet rs = pst.executeQuery()) {
			Config config = ConfigUtils.createConfig();
			Scenario sc = ScenarioUtils.createScenario(config);
			Network network = sc.getNetwork();
			Population population = sc.getPopulation();
			PopulationFactory populationFactory = population.getFactory();
//			useless definition of person and plan
			Person person = populationFactory.createPerson(Id.create("1", Person.class));
			Plan plan = populationFactory.createPlan();
			int i = 0;
			while (rs.next()) {
//				creating a new person if one started
				if (rs.getInt("personTripNum") == 1) {
					String agentId = rs.getString("hhid") + "-" + rs.getString("pnum");
					person = populationFactory.createPerson(Id.create(agentId, Person.class));
					population.addPerson(person);
					plan = populationFactory.createPlan();
				}
//				getting parameters from table
				Coord origCoordinates = new Coord(rs.getDouble("origX"), rs.getDouble("origY"));
				String actType = PopUtils.ActivityType(rs.getInt("origPurp"));
				double endTime = rs.getDouble("finalDepartMinute") * 60 + 10800;
				String mode = PopUtils.Mode(rs.getInt("modeCode"));
//				adding activity and leg
				Activity activity = populationFactory.createActivityFromCoord(actType, origCoordinates);
				activity.setEndTime(endTime);
				plan.addActivity(activity);
				plan.addLeg(populationFactory.createLeg(mode));
//				last activity - adding person to population
				if (rs.getInt("personTripNum") == rs.getInt("lastTripNum")) {
					Coord destCoordinates = new Coord(rs.getDouble("destX"), rs.getDouble("destY"));
					actType = PopUtils.ActivityType(rs.getInt("destPurp"));
					activity = populationFactory.createActivityFromCoord(actType, destCoordinates);
					plan.addActivity(activity);
					person.addPlan(plan);
				}
				System.out.println(i);
				i++;
			}
//			writing to file
			MatsimWriter popWriter = new PopulationWriter(population, network);
			popWriter.write("D:/Users/User/Dropbox/matsim_begin/population.xml");

		} catch (SQLException ex) {

			Logger lgr = Logger.getLogger(PopCreator.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}