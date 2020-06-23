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
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;

public class TransitScheduleGenerator {
	private static final Logger log = Logger.getLogger(TransitScheduleGenerator.class);
	private final static Properties props = DbUtils.readProperties("database.properties");

	public static TransitSchedule createStops(TransitSchedule transitSchedule) throws SQLException {
		log.info("Creating Stops");
		TransitScheduleFactory builder = transitSchedule.getFactory();
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM stops;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			TransitStopFacility stop = builder.createTransitStopFacility(
					Id.create(resultSet.getString("linkid"), TransitStopFacility.class),
					new Coord(resultSet.getDouble("x"), resultSet.getDouble("y")), false);

			stop.setName(resultSet.getString("linkid"));
			stop.setLinkId(Id.createLinkId(resultSet.getString("linkid")));
			transitSchedule.addStopFacility(stop);
		}
		con.close();
		return transitSchedule;
	}

	public static TransitSchedule createTransitLines(TransitSchedule transitSchedule) throws SQLException {
		log.info("Creating Transit Lines");
		TransitScheduleFactory builder = transitSchedule.getFactory();
		LinkNetworkRouteFactory routeFactory = new LinkNetworkRouteFactory();
		List<TransitRouteStop> stops = new ArrayList<>();
		List<Id<Link>> linkIDs = new ArrayList<Id<Link>>();
		Id<Link> startLinkId = null;
		double passing_time = 0;
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
				TransitRouteStop routeStop = builder.createTransitRouteStop(stop, passing_time, passing_time);
				passing_time = passing_time + resultSet.getDouble("passing_time");
				stops.add(routeStop);
			}
			if (resultSet.getInt("seq_number") == resultSet.getInt("last_link")) {
//				creating line route on links			
				Id<Link> endLinkId = Id.create(resultSet.getString("linkid"), Link.class);
				NetworkRoute networkRoute = (NetworkRoute) routeFactory.createRoute(startLinkId, endLinkId);
				networkRoute.setLinkIds(startLinkId, linkIDs, endLinkId);
//				creating line route and stops
				Id<TransitRoute> routeId = Id.create(resultSet.getString("line"), TransitRoute.class);
				TransitRoute transitRoute = builder.createTransitRoute(routeId, networkRoute, stops,
						resultSet.getString("transport_mode_string"));

//				creating line
				Id<TransitLine> transitLineId = Id.create(
						resultSet.getString("description") + "_" + resultSet.getString("line"), TransitLine.class);
				TransitLine transitLine = builder.createTransitLine(transitLineId);
				transitLine.addRoute(transitRoute);
//				adding line to schedule
				transitSchedule.addTransitLine(transitLine);
//				reseting stops and linkids for next line
				stops = new ArrayList<>();
				linkIDs = new ArrayList<Id<Link>>();
				passing_time = 0;
			}

		}
		con.close();
		return transitSchedule;
	}

	public static Vehicles createVehicleTypes() throws SQLException {
		log.info("Creating Vehicle types");
		Vehicles veh = VehicleUtils.createVehiclesContainer();
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM vehicle_types;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			Id<VehicleType> vehTypeId = Id.create(resultSet.getString("vehicle_type"), VehicleType.class);
			VehicleType type = veh.getFactory().createVehicleType(vehTypeId);
			VehicleCapacity cap = type.getCapacity();
//			TODO add columns with data about vehicles to vehicle_types table
			cap.setSeats(50);
//			cap.setStandingRoom(standingRoom);
//			type.setLength(length);
//			VehicleUtils.setDoorOperationMode(type, doorOperationMode);

//			type.setMaximumVelocity(maxVelocity);
//			type.setPcuEquivalents(pcu);

			veh.addVehicleType(type);
			System.out.println(type.getId());
		}
		return veh;
	}

	public static ArrayList<Object> createVehiclesAndDepartures(TransitSchedule transitSchedule, Vehicles vehicles)
			throws SQLException {
		log.info("Creating Vehicles and departures");
		TransitScheduleFactory builder = transitSchedule.getFactory();
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM readable_headway;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			String lineNo = resultSet.getString("line");
			log.info("*******************************************");
			log.info("Creating Vehicles and departures for lineNo" + lineNo);
			Id<TransitLine> line = Id.create(resultSet.getString("description") + "_" + resultSet.getString("line"),
					TransitLine.class);
			TransitLine transitline = transitSchedule.getTransitLines().get(line);
			Id<TransitRoute> route = Id.create(resultSet.getString("line"), TransitRoute.class);
			TransitRoute transitroute = transitline.getRoutes().get(route);
			int firstDepartureTime = TransitUtils.secFromStr(resultSet.getString("start_time"));
			int lastDepartureTime = TransitUtils.secFromStr(resultSet.getString("end_time"));
			int headway_sec = (int) (60 * resultSet.getDouble("headway"));
			int iters = (int) ((lastDepartureTime - firstDepartureTime) / headway_sec);
			for (int i = 0; i < iters; i++) {
				String id = lineNo + "_" + TransitUtils.strFromSec(firstDepartureTime + i * headway_sec);
				log.info("Creating departure: " + id);
				Id<Vehicle> vehicleId = Id.create(id, Vehicle.class);
				Id<VehicleType> vehTypeId = Id.create(resultSet.getString("vehicle_type"), VehicleType.class);
				Vehicle vehicle = vehicles.getFactory().createVehicle(vehicleId,
						vehicles.getVehicleTypes().get(vehTypeId));
				vehicles.addVehicle(vehicle);
				Id<Departure> departureId = Id.create(id, Departure.class);
				Departure firstDeparture = builder.createDeparture(departureId, firstDepartureTime + i * headway_sec);
				firstDeparture.setVehicleId(vehicleId);
				transitroute.addDeparture(firstDeparture);
			}

		}
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(transitSchedule);
		result.add(vehicles);
		return result;

	}

	public static void main(String[] args) throws SQLException {
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitSchedule transitSchedule = sc.getTransitSchedule();
		TransitSchedule transitResult = createStops(transitSchedule);
		transitResult = createTransitLines(transitSchedule);
		Vehicles vehiclesResult = createVehicleTypes();
		ArrayList<Object> temp = createVehiclesAndDepartures(transitResult, vehiclesResult);
		transitResult = (TransitSchedule) temp.get(0);
		vehiclesResult = (Vehicles) temp.get(1);
		new TransitScheduleWriter(transitResult)
				.writeFile(props.getProperty("db.output_folder") + "transitschedule1.xml");

		new MatsimVehicleWriter(vehiclesResult).writeFile(props.getProperty("db.output_folder") + "vehicles1.xml");
//		
	}
}
