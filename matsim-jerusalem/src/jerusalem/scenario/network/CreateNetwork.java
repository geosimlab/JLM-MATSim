package jerusalem.scenario.network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.matsim.core.network.io.NetworkWriter;


/**
 * @author Golan Ben-Dor
 */
public class CreateNetwork
{
	//Logger
	private static final Logger log = Logger.getLogger(CreateNetwork.class);

	private final static String INPUT_LINKS_CSV = "E:\\Golan\\Jerusalem MATSim\\Data recieved\\Current State\\network_pt\\2015\\links.csv";
	private final static String INPUT_NODES_CSV = "E:\\Golan\\Jerusalem MATSim\\Data recieved\\Current State\\network_pt\\2015\\nodes.csv";
	private final static String OUTPUT_NETWORK_FOLDER = "E:\\Golan\\Jerusalem MATSim\\MATSim Input\\";


	public static void main(String[] args) throws IOException
	{
		//	Read nodes.csv
		Map<String, Coord> nodesMap = readNodesCSV(INPUT_NODES_CSV);
		
		//	Read links.csv
		Map<String, ArrayList<JerusalemLink>> linksMap = readLinksCSV(INPUT_LINKS_CSV);
		
		//	Create the Jerusalem MATSim Network
		Network jlmNet = createMATSimNet(nodesMap, linksMap);

		//	Write network
		new NetworkWriter(jlmNet).write(OUTPUT_NETWORK_FOLDER+"network_2015.xml");
	}
	

	/**
	 * Reads "nodes.csv" and write the nodes into a TreeMap "String, Coord".
	 * <p>
	 *  CSV fields:
	 * <li>
	 *  [0] = "id" (int)
	 *	<li>
	 *  [2] = "x" (double)
	 *	<li>
	 *  [3] = "y" (double)
	 *  <br>
	 *	<br>
	 *	@param inputNodesCSV absolute path for "nodes.csv".         
	 *	@return Map of "String, Coord"
	 */
	private static Map<String, Coord> readNodesCSV(String inputNodesCSV)
	{
		log.info("Reading nodes.csv") ;

		Map<String, Coord> nodesMap = new TreeMap();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try
		{
			br = new BufferedReader(new FileReader(inputNodesCSV));
			
			//	skip first line (header)
			line = br.readLine();
			
			while ((line = br.readLine()) != null)
			{
				//	use comma as separator
				String[] lineArr = line.split(cvsSplitBy);
				String nodeId = lineArr[0];
				double nodeX = Double.parseDouble(lineArr[2]);
				double nodeY = Double.parseDouble(lineArr[3]);

				//	write nodes to map Map<String, Coord>
				nodesMap.put(nodeId, new Coord(nodeX, nodeY));
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return nodesMap;
	}
	
	/**
	 * Reads "links.csv" and write the links into a TreeMap "String, ArrayList"
	 * <p>
	 * CSV fields = [i,j,length_met,mode,num_lanes,type,at,linkcap,s0link_m_per_s
	 * <li>
	 *  [0] = "i" (int) - id of <b>from</b> node.
	 *	<li>
	 *  [1] = "j" (int) - id of <b>to</b> node.
		<li>
	 *  [2] = "length_met" (double) - link <b>length</b> in meters.
	 *  <li>
	 *  [3] = mode (String) - allowed <b>modes</b> on links: <ul>
	 *  	<li>c=car
	 *  	<li>w=walk
	 *  	<li>b=bus
	 *  	<li>l=Light rail train
	 *  	<li>t=Train</ul>
	 *  <li>
	 *  [4] = "num_lanes" - (int) number of <b>lanes</b> for link.
	 *  <li>
	 *  [5] = "type" (int) - <b>road type</b> of link: <ul>
	 *  	<li>1 = Freeway
	 *  	<li>2 = Highway
	 *  	<li>3 = Regional Road
	 *  	<li>4 = Local / Access road
	 *		<li>5 = Main Arterial
	 *		<li>6 = Secondary Arterial
	 *		<li>7 = Collector Street
	 *		<li>8 = Local Street
	 *		<li>9 = Zone Connector 
	 *		<li>10 = Walk
	 *		<li>12 = LRT
	 *		<li>66 = Busway
	 *		<li>77 = Rail</ul>
	 *  <li>
	 *  [6] "at" - not important
	 *  <li>
	 *  [7] "linkcap" (double) - link <b>capacity</b>.
	 *  <li>
	 *  [8] "s0link_m_per_s" - <b>freespeed</b> of links (m/s) from EMME simulation (no congestion)
	 *  <br>
	 *	<br>
	 * @param inputLinksCSV an absolute path for "links.csv"
	 * @return Map "String, ArrayList<JerusalemLink"
	 */
	private static Map<String, ArrayList<JerusalemLink>> readLinksCSV(String inputLinksCSV)
	{
		log.info("Reading links.csv") ;

		Map<String, ArrayList<JerusalemLink>> LinksMap = new TreeMap();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try
		{
			br = new BufferedReader(new FileReader(inputLinksCSV));
			
			// skip first line (header)
			line = br.readLine();
			
			while ((line = br.readLine()) != null)
				{
				ArrayList<JerusalemLink> linkArr = new ArrayList<JerusalemLink>();
				
				// use comma as separator
				String[] lineArr = line.split(cvsSplitBy);
				
				JerusalemLink jerusalemLink = new JerusalemLink(lineArr);			

				//add link elements to arrLink
				linkArr.add(jerusalemLink);
				
				//create id of link [fromId_toId_roadType]
				String id = jerusalemLink.getFromId()+"_"+jerusalemLink.getToId()+"_"+jerusalemLink.getRoadType();
				
				LinksMap.put(id, linkArr);
				}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return LinksMap;
	}
	

	
	/**
	 * Creates a MATSim network from nodesMap - "String, Coord" and from linksMap "String, ArrayList(JerusalemLink)"
	 *	@param nodesMap "String, Coord"
	 *	@param linksMap "String, ArrayList(JerusalemLink)"
	 *	@return MATSim network 
	 */
	private static Network createMATSimNet(Map<String, Coord> nodesMap,Map<String, ArrayList<JerusalemLink>> linksMap )
	{
		log.info("creating a MATSim network") ;

		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();
		
		log.info("creating MATSim nodes") ;

		for (Map.Entry<String,Coord> entry : nodesMap.entrySet()) 
		{
		    String nodeId = entry.getKey();
		    Coord nodeCoord = entry.getValue();
			Node node = fac.createNode(Id.createNodeId(nodeId),nodeCoord);
			net.addNode(node);
		}
		
		log.info("creating MATSim links") ;
		
		//	iterate through link IDs
		for (Map.Entry<String,ArrayList<JerusalemLink>> entry : linksMap.entrySet()) 
		{
		    String linkId = entry.getKey();
		    ArrayList<JerusalemLink> linkArr = entry.getValue();
		    Link link = null;
		    
		    //	iterate through link attributes
		    for (JerusalemLink jerusalemLink : linkArr)
			{
		    	//	create node ID object to get read from the network
		    	Id<Node> fromID = Id.createNodeId(jerusalemLink.getFromId());
		    	Id<Node> toID = Id.createNodeId(jerusalemLink.getToId());
		    	
		    	Node fromNode = net.getNodes().get(fromID);
		    	Node toNode = net.getNodes().get(toID);	
		    	
		    	link = fac.createLink(Id.createLinkId(linkId), fromNode,toNode);
			    double travelTime = jerusalemLink.getLength() / jerusalemLink.getFreeSpeed();
				setLinkAttributes(link, jerusalemLink.getCapacity(), jerusalemLink.getLength(), travelTime, jerusalemLink.getMode());
			}
			net.addLink(link);		    
		}
		return net;
		
	}
	
	/**
	 * 	set a MATSim link with attributes
	 *	@param link			a MATSim link
	 *	@param capacity		hourly capacity of link
	 *	@param length		meters
	 *	@param travelTime	on link 
	 *	@param modes		a set of allowed modes
	 */
	private static void setLinkAttributes(Link link, double capacity, double length, double travelTime, Set<String> modes)
	{
		link.setCapacity(capacity);
		link.setLength(length);
		
		// agents have to reach the end of the link before the time step ends to
		// be able to travel forward in the next time step (matsim time step logic)
		link.setFreespeed(link.getLength() / (travelTime - 0.1));
		link.setAllowedModes(modes);
	}
}
