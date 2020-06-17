package jerusalem.scenario.transit;

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
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;
import jerusalem.scenario.network.CreateNetwork;

public class StopsGenerator {
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static Properties props = DbUtils.readProperties("database.properties");

	private static void readStop() throws SQLException {
		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		TransitSchedule transitSchedule = sc.getTransitSchedule();
		TransitScheduleFactory builder = transitSchedule.getFactory();

		log.info("Reading nodes");
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM stops;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {

			TransitStopFacility berFac = builder.createTransitStopFacility(
					Id.create(resultSet.getString("row_number"), TransitStopFacility.class),
					new Coord(resultSet.getDouble("x"), resultSet.getDouble("y")), false);
			berFac.setName(resultSet.getString("linkid"));
			System.out.println(berFac.toString());
			transitSchedule.addStopFacility(berFac);
		}
		new TransitScheduleWriter(transitSchedule)
				.writeFile(props.getProperty("db.output_folder") + "transitschedule1.xml");

	}

	public static void main(String[] args) throws SQLException {
		readStop();
	}
}
