package jerusalem.scenario.transit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.LinkNetworkRouteFactory;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;

public class TransitScheduleGenerator {
	private static final Logger log = Logger.getLogger(TransitScheduleGenerator.class);
	private final static Properties props = DbUtils.readProperties("database.properties");

	private static TransitSchedule createStops(TransitSchedule transitSchedule) throws SQLException {
		log.info("Creating Stops");
		TransitScheduleFactory builder = transitSchedule.getFactory();
//		Network network = new CreateNetwork().getJlmNet();
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM stops;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			TransitStopFacility stop = builder.createTransitStopFacility(
					Id.create(resultSet.getString("linkid"), TransitStopFacility.class),
					new Coord(resultSet.getDouble("x"), resultSet.getDouble("y")), false);

			stop.setName(resultSet.getString("linkid"));
			stop.setLinkId(Id.createLinkId(resultSet.getString("linkid")));
//			System.out.println(stop.toString());
			transitSchedule.addStopFacility(stop);
		}
		con.close();
		return transitSchedule;
	}

	private static TransitSchedule createTransitLines(TransitSchedule transitSchedule) throws SQLException {
		log.info("Creating Transit Lines");
		TransitScheduleFactory builder = transitSchedule.getFactory();
		LinkNetworkRouteFactory routeFactory = new LinkNetworkRouteFactory();
		List<TransitRouteStop> stops = new ArrayList<>();
		List<Id<Link>> linkIDs = new ArrayList<Id<Link>>();
		Id<Link> startLinkId = null;
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM pt_routes;");
		ResultSet resultSet = pst.executeQuery();

		while (resultSet.next()) {
//			if origin - saving the origin link for transit route network, else - adding links to route 
			if (resultSet.getInt("seq_number") == 0) {
				startLinkId = Id.create(resultSet.getString("linkid"), Link.class);
			} else if (resultSet.getInt("seq_number") < resultSet.getInt("last_link")) {
				Id<Link> linkId = Id.create(resultSet.getString("linkid"), Link.class);
				linkIDs.add(linkId);
			}

			if (resultSet.getInt("stop") == 1) {
//				adding a stop on link, if stop exists
				Id<TransitStopFacility> stopId = Id.create(resultSet.getString("linkid"), TransitStopFacility.class);
				TransitStopFacility stop = transitSchedule.getFacilities().get(stopId);
				TransitRouteStop routeStop = builder.createTransitRouteStop(stop, 0, 0);
				stops.add(routeStop);
			}
			if (resultSet.getInt("seq_number") == resultSet.getInt("last_link")) {
//				creating line route on links			
				Id<Link> endLinkId = Id.create(resultSet.getString("linkid"), Link.class);
				NetworkRoute networkRoute = (NetworkRoute) routeFactory.createRoute(startLinkId, endLinkId);
				networkRoute.setLinkIds(startLinkId, linkIDs, endLinkId);
//				creating line route and stops
//				TODO switch transport mode for bus, train or light rail. decide whether here or in sql
				Id<TransitRoute> routeId = Id.create(resultSet.getString("line"), TransitRoute.class);
				TransitRoute transitRoute = builder.createTransitRoute(routeId, networkRoute, stops,
						resultSet.getString("transport_mode"));

//				creating line
				Id<TransitLine> transitLineId = Id.create(resultSet.getString("line"), TransitLine.class);
				TransitLine transitLine = builder.createTransitLine(transitLineId);
				transitLine.addRoute(transitRoute);
//				adding line to schedule
				transitSchedule.addTransitLine(transitLine);
//				reseting stops and linkids for next line
				stops = new ArrayList<>();
				linkIDs = new ArrayList<Id<Link>>();
			}

		}
		con.close();
		return transitSchedule;
	}

	public static void main(String[] args) throws SQLException {
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitSchedule transitSchedule = sc.getTransitSchedule();
		TransitSchedule result = createStops(transitSchedule);
		result = createTransitLines(transitSchedule);
		new TransitScheduleWriter(result).writeFile(props.getProperty("db.output_folder") + "transitschedule1.xml");
	}
}
