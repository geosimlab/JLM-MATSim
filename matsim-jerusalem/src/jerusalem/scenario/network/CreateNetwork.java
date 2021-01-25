package jerusalem.scenario.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.MultimodalNetworkCleaner;
import org.matsim.core.network.algorithms.NetworkAdaptLength;
import org.matsim.core.network.algorithms.NetworkCalcTopoType;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.NetworkExpandNode;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.algorithms.NetworkTurnInfoBuilder;
import org.matsim.core.network.algorithms.NetworkExpandNode.TurnInfo;
import org.matsim.core.network.algorithms.NetworkInverter;
import org.matsim.core.network.algorithms.intersectionSimplifier.IntersectionSimplifier;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import jerusalem.scenario.db.DbInitialize;
import jerusalem.scenario.db.DbUtils;

/**
 * @author Golan Ben-Dor and Ido Klein
 */
public class CreateNetwork {
	// Logger
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static Properties props = DbUtils.readProperties("database.properties");
//	All link have minimum 500 capacity in MATSim. common practice, asked kai nagel. 
	private final static int MIN_FLOW_CAPACITY = 500;
	public final static String NETWORK_ID = "13";
	public final static String NETWORK_OUTPUT_PATH = props.getProperty("folder.output_folder") + NETWORK_ID
			+ ".network.xml.gz";
//	whether links of certain types be removed. 
	private final static boolean REMOVE_CONNECTOR = true;
	private final static boolean REMOVE_WALK_LINKS = false;
	private final static boolean REMOVE_LOCAL_STREETS = false;

	private Network NET;
//	Constructor creates network
	public CreateNetwork() {
		try {
			Network newNetwork = createJLMNet();
			this.NET = newNetwork;
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
//  method to return the network
	public Network getJlmNet() {
		return this.NET;
	}
//  main, method to write network to file
	public static void main(String[] args) throws SQLException {
		Network newNetwork = createJLMNet();
		new NetworkWriter(newNetwork).write(NETWORK_OUTPUT_PATH);
	}
//	
	/**
	 * main function to create network. 
	 * can be changed to include other network manipulations. 
	 * they exist in previous version of population branch
	 * @return Network 
	 * @throws SQLException
	 */
	private static Network createJLMNet() throws SQLException
	{
		// Read nodes
		Map<String, Coord> nodesMap = readNodes();
		// Read links
		Map<String, ArrayList<JerusalemLink>> linksMap = readLinks(REMOVE_LOCAL_STREETS,REMOVE_CONNECTOR, REMOVE_WALK_LINKS);
		// Create the Jerusalem MATSim Network
		Network jlmNet = createMATSimNet(nodesMap, linksMap);
		log.info("Cleaning the network...");
		// Run network cleaner and multiModeNetworkCleaner- deleting nodes which do not
		// connect
		jerusalemNetworkCleaner(jlmNet);
		// euclidean distance adaptation		
		new NetworkAdaptLength().run(jlmNet);
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
	private static Map<String, ArrayList<JerusalemLink>> readLinks(boolean isLocal, boolean isConnector, boolean isWalk)
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
			jerusalemLink.setLaneNum(num_lanes);
			jerusalemLink.setRoadType(resultSet.getDouble("type"));
			// setting capacity and speed in pt or walk links			
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
				// setting min capacity of links				
				jerusalemLink.setCapacity(Math.max(MIN_FLOW_CAPACITY,(int) resultSet.getDouble("linkcap")));
				jerusalemLink.setFreeSpeed((double) (int) resultSet.getDouble("s0link_m_per_s"));
			}
//			if(jerusalemLink.getFreeSpeed() < 0) {
//				System.out.println("what?");
//			}
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
				link = fac.createLink(Id.createLinkId(linkId), fromNode, toNode);

				double travelTime = jerusalemLink.getLength() / jerusalemLink.getFreeSpeed();
				setLinkAttributes(link, jerusalemLink.getCapacity(), jerusalemLink.getLength(),
						jerusalemLink.getFreeSpeed(),travelTime ,jerusalemLink.getMode(), jerusalemLink.getLaneNum());
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
	 * @param numberOfLanes   
	 */
	private static void setLinkAttributes(Link link, double capacity, double length, double freespeed,double travelTime,
			Set<String> modes, double numberOfLanes) {
		link.setCapacity(capacity);
		link.setLength(length);
		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		if((travelTime - 0.1) < 0) {
			link.setFreespeed(link.getLength() / (travelTime));
		}else {
			link.setFreespeed(link.getLength() / (travelTime - 0.1));	
		}
		
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
