package jerusalem.scenario.network;

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
import org.matsim.core.network.algorithms.NetworkAdaptLength;
import org.matsim.core.network.algorithms.NetworkCalcTopoType;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.NetworkExpandNode;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.algorithms.intersectionSimplifier.IntersectionSimplifier;
import org.matsim.core.network.io.NetworkWriter;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;

/**
 * @author Golan Ben-Dor
 */
public class CreateNetwork {
	// Logger
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static Properties props = DbUtils.readProperties("database.properties");
	private final static int MIN_STORAGE_LENGTH = 100; 
	private final static double STORAGE_LENGTH_DECAY = 1;
	private final static int MIN_FLOW_CAPACITY = 600;
	public final static String NETWORK_ID = "increased_storge_cap_"+MIN_STORAGE_LENGTH+"_decay_"+STORAGE_LENGTH_DECAY+"_min_flow_capcity_" + MIN_FLOW_CAPACITY;
	public final static String NETWORK_OUTPUT_PATH = props.getProperty("folder.output_folder") + NETWORK_ID
			+ ".network.xml.gz";
	private final static boolean REMOVE_CONNECTOR = true;
	private final static boolean REMOVE_WALK_LINKS = false;
	private final static boolean REMOVE_LOCAL_STREETS = false;
	
	private Network NET;
	private String network_id;
	private String network_output_path; 
//	TODO unless we use objects as inputs, this is unnecessary
	public CreateNetwork(double min_storage_length, double storage_length_decay, int min_flow_capacity) {
		try {
			Network newNetwork = createJLMNet(min_storage_length, storage_length_decay, min_flow_capacity);
			this.NET = newNetwork;
			this.network_id = "increased_storge_cap_"+min_storage_length+"_decay_"+storage_length_decay+"_min_flow_capcity_" + min_flow_capacity;
			this.network_output_path = props.getProperty("folder.output_folder") + network_id
					+ ".network.xml.gz";
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public Network getJlmNet() {
		return this.NET;
	}
	public String getId() {
		return this.network_id;
	}
	public String getPath() {
		return this.network_output_path;
	}

	public static void main(String[] args) throws SQLException {
		Network newNetwork = createJLMNet(MIN_STORAGE_LENGTH,STORAGE_LENGTH_DECAY,MIN_FLOW_CAPACITY);
		new NetworkWriter(newNetwork).write(NETWORK_OUTPUT_PATH);
	}

	private static Network createJLMNet(double min_storage_length, double storage_length_decay, int min_flow_capacity) throws SQLException
	{
		// Read nodes
		Map<String, Coord> nodesMap = readNodes();

		// Read links
		Map<String, ArrayList<JerusalemLink>> linksMap = readLinks(REMOVE_LOCAL_STREETS,REMOVE_CONNECTOR, REMOVE_WALK_LINKS,min_storage_length, storage_length_decay, min_flow_capacity);

		// Create the Jerusalem MATSim Network
		Network jlmNet = createMATSimNet(nodesMap, linksMap);
		log.info("Simplifying the network...");
//		NetworkSimplifier ns = new NetworkSimplifier();
//		ns.setMergeLinkStats(true);
//		ns.run(jlmNet);
//		ns = new NetworkSimplifier();
//		ns.setMergeLinkStats(true);
//		ns.run(jlmNet);
//		simplify intersections
//		IntersectionSimplifier is = new IntersectionSimplifier(50.0, 2);
//		jlmNet = is.simplify(jlmNet);
		NetworkCalcTopoType nct = new NetworkCalcTopoType();
		nct.run(jlmNet);
		
		
		log.info("Cleaning the network...");
		// Run network cleaner and multiModeNetworkCleaner- deleting nodes which do not
		// connect
		jerusalemNetworkCleaner(jlmNet);
		new NetworkAdaptLength().run(jlmNet);
		nct.run(jlmNet);
		return jlmNet;
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
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM nodes;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			String nodeId = resultSet.getString("i");
			double nodeX = resultSet.getDouble("x");
			double nodeY = resultSet.getDouble("y");
			nodesMap.put(nodeId, new Coord(nodeX, nodeY));
		}
		log.info("Finished Reading nodes");
		con.close();
		return nodesMap;
	}

	/**
	 * Reads links table from db and write the links into a TreeMap "String,
	 * ArrayList"
	 * <p>
	 * table fields = [i,j,length_met,mode,num_lanes,type,linkcap,s0link_m_per_s
	 * <li>"i" (int) - id of <b>from</b> node.
	 * <li>"j" (int) - id of <b>to</b> node.
	 * <li>"length_met" (double) - link <b>length</b> in meters.
	 * <li>"mode" (String) - allowed <b>modes</b> on links:
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
	 * <li>"linkcap" (double) - link <b>capacity</b>.
	 * <li>"s0link_m_per_s" - <b>freespeed</b> of links (m/s) from EMME simulation
	 * (no congestion) <br>
	 * <br>
	 * 
	 * @param isConnector remove connectors if true
	 * @param min_flow_capacity 
	 * @return Map "String, ArrayList<JerusalemLink"
	 */
	private static Map<String, ArrayList<JerusalemLink>> readLinks(boolean isLocal, boolean isConnector, boolean isWalk, double min_storage_length, double storage_length_decay, int min_flow_capacity)
			throws SQLException {
		log.info("Reading links");

		Map<String, ArrayList<JerusalemLink>> LinksMap = new TreeMap();
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT *, \"@linkcap\" as linkcap FROM links;");
		ResultSet resultSet = pst.executeQuery();
		while (resultSet.next()) {
			if ((resultSet.getDouble("type") == 8 && isLocal) || (resultSet.getDouble("type") == 9 && isConnector) || (resultSet.getDouble("type") == 10 && isWalk)) {
				continue;
			}
			int i = resultSet.getInt("i");
			int j = resultSet.getInt("j");
			double length = resultSet.getDouble("length_met");
			ArrayList<JerusalemLink> linkArr = new ArrayList<JerusalemLink>();
			String mode = resultSet.getString("mode");
			double num_lanes = resultSet.getDouble("num_lanes");
			JerusalemLink jerusalemLink = new JerusalemLink();
			jerusalemLink.setFromId(i);
			jerusalemLink.setToId(j);
			jerusalemLink.setLength(length);
			jerusalemLink.setMode(JerusalemLink.parseMode(mode));
			if (length >= min_storage_length) {
				jerusalemLink.setLaneNum(num_lanes);
			} else {
				jerusalemLink.setLaneNum(num_lanes *Math.pow(Math.ceil(min_storage_length/length),storage_length_decay));
			}
			jerusalemLink.setRoadType(resultSet.getDouble("type"));
//			fixing links with capacity = 0, speed = 0
			if (mode.equals("[Mode(b), Mode(w)]") | mode.equals("[Mode(b)]") | mode.equals("[Mode(l)]")) {
				jerusalemLink.setCapacity(5000);
				jerusalemLink.setFreeSpeed(8.333333333);
			} else if (mode.equals("[Mode(r)]")) {
				jerusalemLink.setCapacity(5000);
				jerusalemLink.setFreeSpeed(16.66666667);
			} else if (mode.equals("[Mode(w)]")) {
				jerusalemLink.setCapacity(500);
				jerusalemLink.setFreeSpeed(1.39);
			} else {
				jerusalemLink.setCapacity(Math.max(min_flow_capacity,(int) resultSet.getDouble("linkcap")));
				jerusalemLink.setFreeSpeed((double) (int) resultSet.getDouble("s0link_m_per_s"));
			}

			String id = jerusalemLink.getFromId() + "_" + jerusalemLink.getToId() + "_"
					+ (int) jerusalemLink.getRoadType();

			linkArr.add(jerusalemLink);
			LinksMap.put(id, linkArr);
		}
		con.close();
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
//				double euclideanDistance = NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord());
//				euclideanDistance = euclideanDistance > jerusalemLink.getLength() ? euclideanDistance
//						: jerusalemLink.getLength();
				link = fac.createLink(Id.createLinkId(linkId), fromNode, toNode);
//				double travelTime = euclideanDistance / jerusalemLink.getFreeSpeed();
				setLinkAttributes(link, jerusalemLink.getCapacity(), jerusalemLink.getLength(),
						jerusalemLink.getFreeSpeed(), jerusalemLink.getMode(), jerusalemLink.getLaneNum());
//				link.getAttributes().putAttribute("roadType", jerusalemLink.getRoadType());
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
	private static void setLinkAttributes(Link link, double capacity, double length, double freespeed,
			Set<String> modes, double numberOfLanes) {
		link.setCapacity(capacity);
		link.setLength(length);

		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		link.setFreespeed(freespeed);
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
		multiModalNetClean.run(setModeCar, setModeLrt);
		multiModalNetClean.run(setModeBus, setModeCar);
	}
}
