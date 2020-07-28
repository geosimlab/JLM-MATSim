package jerusalem.scenario.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkExpandNode;
import org.matsim.core.network.algorithms.NetworkExpandNode.TurnInfo;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.lanes.Lane;
import org.matsim.lanes.Lanes;
import org.matsim.lanes.LanesFactory;
import org.matsim.lanes.LanesToLinkAssignment;
import org.matsim.lanes.LanesUtils;
import org.matsim.lanes.LanesWriter;

public class CreateLanes
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		Config config = ConfigUtils.createConfig();
//		config.network().setInputCRS(TransformationFactory.ATLANTIS);
//		config.network().setInputFile("D:/matsim_jlm/output/null/null.output_network.xml.gz");
//		Scenario scenario = ScenarioUtils.loadScenario(config);
		MutableScenario scenario = ScenarioUtils.createMutableScenario(config);
		Network network = NetworkUtils.readNetwork("D:/matsim_jlm/output/increased_storge_cap_0.0_decay_2.0_min_flow_capcity_0.network.xml.gz");
		NetworkExpandNode nen = new NetworkExpandNode(network,20,10);
		ArrayList<TurnInfo> turns = new ArrayList<>();
		Link inlink = network.getLinks().get(Id.create("10274_10299_8", Link.class));
		Link outlink = network.getLinks().get(Id.create("10299_10295_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10295_10299_6", Link.class));
		outlink = network.getLinks().get(Id.create("10299_10308_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10308_10299_6", Link.class));
		outlink = network.getLinks().get(Id.create("10299_10295_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		nen.expandNode(Id.create("10299", Node.class), turns);
		
		turns = new ArrayList<>();
		inlink = network.getLinks().get(Id.create("10294_10308_7", Link.class));
		outlink = network.getLinks().get(Id.create("10308_10312_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10312_10308_6", Link.class));
		outlink = network.getLinks().get(Id.create("10308_10299_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10299_10308_6", Link.class));
		outlink = network.getLinks().get(Id.create("10308_10312_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		nen.expandNode(Id.create("10308", Node.class), turns);
		
		turns = new ArrayList<>();
		inlink = network.getLinks().get(Id.create("10308_10312_6", Link.class));
		outlink = network.getLinks().get(Id.create("10312_10321_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10321_10312_6", Link.class));
		outlink = network.getLinks().get(Id.create("10312_10308_6", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10308_10312_6", Link.class));
		outlink = network.getLinks().get(Id.create("10312_10298_8", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		inlink = network.getLinks().get(Id.create("10321_10312_6", Link.class));
		outlink = network.getLinks().get(Id.create("10312_10298_8", Link.class));
		turns.add(new TurnInfo(inlink.getId(),outlink.getId()));
		nen.expandNode(Id.create("10312", Node.class), turns);
		new NetworkWriter(network).write("D:/matsim_jlm/output/null/null2.output_network.xml.gz");
//		scenario.setNetwork(network);
//		Map<Id<Link>, ? extends Link> linksFromNetwork = scenario.getNetwork().getLinks();
//		Map<String, Id<Link>> links = new HashMap<>();
//		for(Id<Link> id :linksFromNetwork.keySet()) {
//			links.put(id.toString(), id);
//		}
//		Lanes lanes = scenario.getLanes();
//		LanesFactory fac = lanes.getFactory();
//		
//		Link link = network.getLinks().get(Id.create("10308_10312_6", Link.class));
//		Id<Link> id = link.getId();
//		double capacity = link.getCapacity();
//		double link_length = link.getLength();
//		int num_lanes = (int) link.getNumberOfLanes();
//		LanesToLinkAssignment linkAssignment = fac.createLanesToLinkAssignment(links.get(id.toString()));
//		List<Id<Lane>> laneList = new ArrayList<>();
//		for(int i = 1; i <= num_lanes;i++) {
//			laneList.add(Id.create(id+"_"+i, Lane.class));			
//		}
//		LanesUtils.createAndAddLane(linkAssignment, 
//				fac, 
//				Id.create(id + ".ol", Lane.class), 
//				capacity, 
//				link_length,
//				0, 
//				num_lanes,
//				null, //to link ids
//				laneList//to lane ids
//				);
//		LanesUtils.createAndAddLane(linkAssignment, 
//				fac, 
//				Id.create(id+"_1", Lane.class), 
//				capacity/num_lanes, 
//				link_length-1, 
//				-1, 
//				1,
//				Collections.singletonList(links.get("10312_10298_8")), 
//				null);
//		LanesUtils.createAndAddLane(linkAssignment, 
//				fac, 
//				Id.create(id+"_2", Lane.class), 
//				capacity/num_lanes, 
//				link_length-1, 
//				0, 
//				1,
//				Collections.singletonList(links.get("10312_10321_6")), 
//				null);
//		lanes.addLanesToLinkAssignment(linkAssignment);
//	
//		
//		link = network.getLinks().get(Id.create("10274_10299_8", Link.class));
//		id = link.getId();
//		capacity = link.getCapacity();
//		link_length = link.getLength();
//		num_lanes = (int) link.getNumberOfLanes();
//		linkAssignment = fac.createLanesToLinkAssignment(links.get(id.toString()));
//		laneList = new ArrayList<>();
//		for(int i = 1; i <= num_lanes;i++) {
//			laneList.add(Id.create(id+"_"+i, Lane.class));			
//		}
//		LanesUtils.createAndAddLane(linkAssignment, 
//				fac, 
//				Id.create(id + ".ol", Lane.class), 
//				capacity, 
//				link_length,
//				0, 
//				num_lanes,
//				null, //to link ids
//				laneList//to lane ids
//				);
//		LanesUtils.createAndAddLane(linkAssignment, 
//				fac, 
//				Id.create(id+"_1", Lane.class), 
//				capacity/num_lanes, 
//				link_length-1, 
//				0, 
//				1,
//				Collections.singletonList(links.get("10299_10295_6")), 
//				null);
//		lanes.addLanesToLinkAssignment(linkAssignment);
//		
//		new LanesWriter(lanes).write("D:/matsim_jlm/output/lanes.xml.gz");
	}
}
