package jerusalem.scenario.network;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.MultimodalNetworkCleaner;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

import jerusalem.scenario.population.DbUtils;

/**
 * @author Golan Ben-Dor
 */
public class CreateNetwork {
	// Logger
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static Properties props = DbUtils.readProperties("database.properties");
	private final static String OUTPUT_NETWORK_FOLDER = props.getProperty("db.output_folder");
	private final static String NETWORK_ID = "" + 5;
	private static final String user = props.getProperty("db.username");
	private static final String password = props.getProperty("db.password");
	private static final String db_url = props.getProperty("db.url");
	private static final String port = props.getProperty("db.port");
	private static final String db_name = props.getProperty("db.db_name");
	private static final String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name + "?loggerLevel=DEBUG";

	private final static boolean REMOVE_CONNECTOR = true;

	public static void main(String[] args) throws IOException, SQLException {
		// Read nodes
		Map<String, Coord> nodesMap = readNodes();

		// Read links.csv
		Map<String, ArrayList<JerusalemLink>> linksMap = readLinks(REMOVE_CONNECTOR);

		// Create the Jerusalem MATSim Network
		Network jlmNet = createMATSimNet(nodesMap, linksMap);

		// Run network cleaner and multiModeNetworkCleaner- deleting nodes which do not
		// connects
		jerusalemNetworkCleaner(jlmNet);

		// Write network
		new NetworkWriter(jlmNet).write(OUTPUT_NETWORK_FOLDER + NETWORK_ID + ".network_2015.xml");
	}

	/**
	 * Reads nodes table from db and write the nodes into a TreeMap "String, Coord".
	 * <p>
	 * nodes table fields:
	 * <li>"i" - id (int)
	 * <li>"x" - x coordinate (double)
	 * <li>"y" - y coordinate (double) <br>
	 * <br>
	 * 
	 * 
	 * @return Map of "String, Coord"
	 */
	private static Map<String, Coord> readNodes() throws SQLException {
		log.info("Reading nodes");
		Map<String, Coord> nodesMap = new TreeMap();
		Connection con = DriverManager.getConnection(url, user, password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM nodes;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			String nodeId = resultSet.getString("i");
			double nodeX = resultSet.getDouble("x");
			double nodeY = resultSet.getDouble("y");
			nodesMap.put(nodeId, new Coord(nodeX, nodeY));
		}
		log.info("Finished Reading nodes");
		return nodesMap;
	}

	/**
	 * Reads links table from db and write the links into a TreeMap "String,
	 * ArrayList"
	 * <p>
	 * CSV fields = [i,j,length_met,mode,num_lanes,type,at,linkcap,s0link_m_per_s
	 * <li>[0] = "i" (int) - id of <b>from</b> node.
	 * <li>[1] = "j" (int) - id of <b>to</b> node.
	 * <li>[2] = "length_met" (double) - link <b>length</b> in meters.
	 * <li>[3] = mode (String) - allowed <b>modes</b> on links:
	 * <ul>
	 * <li>c=car
	 * <li>w=walk
	 * <li>b=bus
	 * <li>l=Light rail train
	 * <li>t=Train
	 * </ul>
	 * <li>[4] = "num_lanes" - (int) number of <b>lanes</b> for link.
	 * <li>[5] = "type" (int) - <b>road type</b> of link:
	 * <ul>
	 * <li>1 = Freeway
	 * <li>2 = Highway
	 * <li>3 = Regional Road
	 * <li>4 = Local / Access road
	 * <li>5 = Main Arterial
	 * <li>6 = Secondary Arterial
	 * <li>7 = Collector Street
	 * <li>8 = Local Street
	 * <li>9 = Zone Connector
	 * <li>10 = Walk
	 * <li>12 = LRT
	 * <li>66 = Busway
	 * <li>77 = Rail
	 * </ul>
	 * <li>[6] "at" - not important
	 * <li>[7] "linkcap" (double) - link <b>capacity</b>.
	 * <li>[8] "s0link_m_per_s" - <b>freespeed</b> of links (m/s) from EMME
	 * simulation (no congestion) <br>
	 * <br>
	 * 
	 * @param inputLinksCSV an absolute path for "links.csv"
	 * @param isConnector   remove connectors if true
	 * @return Map "String, ArrayList<JerusalemLink"
	 */
	private static Map<String, ArrayList<JerusalemLink>> readLinks(boolean isConnector) throws SQLException {
		log.info("Reading links");

		Map<String, ArrayList<JerusalemLink>> LinksMap = new TreeMap();
		Connection con = DriverManager.getConnection(url, user, password);
		PreparedStatement pst = con.prepareStatement("SELECT *, \"@linkcap\" as linkcap FROM links;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			if (resultSet.getDouble("type") == 9 && isConnector) {
				continue;
			}
			ArrayList<JerusalemLink> linkArr = new ArrayList<JerusalemLink>();
			JerusalemLink jerusalemLink = new JerusalemLink();
			jerusalemLink.setFromId(resultSet.getInt("i"));
			jerusalemLink.setToId(resultSet.getInt("j"));
			jerusalemLink.setLength(resultSet.getDouble("length_met"));
			jerusalemLink.setMode(JerusalemLink.parseMode(resultSet.getString("mode")));
			System.out.print(resultSet.getString("mode") + "<*>");
			if (!jerusalemLink.getMode().isEmpty()) {
				System.out.println(jerusalemLink.getMode());
			} else {
				System.out.println("not parsed");
			}

			jerusalemLink.setLaneNum(resultSet.getDouble("num_lanes"));
			jerusalemLink.setRoadType(resultSet.getDouble("type"));
			jerusalemLink.setCapacity(resultSet.getDouble("linkcap"));
			jerusalemLink.setFreeSpeed(resultSet.getDouble("s0link_m_per_s"));
			String id = jerusalemLink.getFromId() + "_" + jerusalemLink.getToId() + "_"
					+ (int) jerusalemLink.getRoadType();
			linkArr.add(jerusalemLink);
			LinksMap.put(id, linkArr);
		}

		return LinksMap;
	}

	/**
	 * Creates a MATSim network from nodesMap - "String, Coord" and from linksMap
	 * "String, ArrayList(JerusalemLink)"
	 * 
	 * @param nodesMap "String, Coord"
	 * @param linksMap "String, ArrayList(JerusalemLink)"
	 * @return MATSim network
	 */
	private static Network createMATSimNet(Map<String, Coord> nodesMap,
			Map<String, ArrayList<JerusalemLink>> linksMap) {
		log.info("creating a MATSim network");

		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();

		log.info("creating MATSim nodes");

		for (Map.Entry<String, Coord> entry : nodesMap.entrySet()) {
			String nodeId = entry.getKey();
			Coord nodeCoord = entry.getValue();
			Node node = fac.createNode(Id.createNodeId(nodeId), nodeCoord);
			net.addNode(node);
		}

		log.info("creating MATSim links");

		// iterate through link IDs
		for (Map.Entry<String, ArrayList<JerusalemLink>> entry : linksMap.entrySet()) {
			String linkId = entry.getKey();
			ArrayList<JerusalemLink> linkArr = entry.getValue();
			Link link = null;

			// iterate through link attributes
			for (JerusalemLink jerusalemLink : linkArr) {
				// create node ID object to get read from the network
				Id<Node> fromID = Id.createNodeId(jerusalemLink.getFromId());
				Id<Node> toID = Id.createNodeId(jerusalemLink.getToId());

				Node fromNode = net.getNodes().get(fromID);
				Node toNode = net.getNodes().get(toID);

				link = fac.createLink(Id.createLinkId(linkId), fromNode, toNode);
				double travelTime = jerusalemLink.getLength() / jerusalemLink.getFreeSpeed();
				setLinkAttributes(link, jerusalemLink.getCapacity(), jerusalemLink.getLength(), travelTime,
						jerusalemLink.getMode(), jerusalemLink.getLaneNum());
			}
			net.addLink(link);
		}
		return net;
	}

	/**
	 * set a MATSim link with attributes
	 * 
	 * @param link       a MATSim link
	 * @param capacity   hourly capacity of link
	 * @param length     meters
	 * @param travelTime on link
	 * @param modes      a set of allowed modes
	 */
	private static void setLinkAttributes(Link link, double capacity, double length, double travelTime,
			Set<String> modes, double numberOfLanes) {
		link.setCapacity(capacity);
		link.setLength(length);

		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		link.setFreespeed(link.getLength() / (travelTime - 0.1));
		link.setAllowedModes(modes);
		link.setNumberOfLanes(numberOfLanes);
	}

	/**
	 * Method to clean the network (dead nodes which do not connect to anywhere)
	 * 
	 * @param network Jerusalem Network (not cleaned)
	 * @return Cleaned MATSim network
	 */
	private static void jerusalemNetworkCleaner(Network jlmNet) {
		log.info("Running NetworkCleaner");

		NetworkCleaner nwCleaner = new NetworkCleaner();
		nwCleaner.run(jlmNet);

		Set<String> setModeWalk = new java.util.HashSet<>();
		Set<String> setModeCar = new java.util.HashSet<>();
		Set<String> setModeTrain = new java.util.HashSet<>();
		Set<String> setModePT = new java.util.HashSet<>();
		Set<String> setModeBus = new java.util.HashSet<>();
		Set<String> setModeLrt = new java.util.HashSet<>();

		setModeCar.add("car");
		setModeTrain.add("train");
		setModeWalk.add("walk");
		setModeBus.add("bus");
		setModeLrt.add("lrt");
		setModePT.add("pt");

		log.info("Running MultimodalNetworkCleaner");
		MultimodalNetworkCleaner multiModalNetClean = new MultimodalNetworkCleaner(jlmNet);

		// clean each mode separately
		multiModalNetClean.run(setModeCar);
		multiModalNetClean.run(setModeTrain);
		multiModalNetClean.run(setModeWalk);
		multiModalNetClean.run(setModeLrt);
		multiModalNetClean.run(setModeBus);
		multiModalNetClean.run(setModePT);

		// clean each mode separately
		multiModalNetClean.run(setModeCar, setModePT);
		multiModalNetClean.run(setModeCar, setModeBus);
		multiModalNetClean.run(setModeWalk, setModeTrain);
		multiModalNetClean.run(setModeWalk, setModeBus);
		multiModalNetClean.run(setModeWalk, setModeLrt);
		multiModalNetClean.run(setModeBus, setModeCar);
		multiModalNetClean.run(setModeLrt, setModeCar);
		multiModalNetClean.run(setModeLrt, setModeWalk);

	}
}
