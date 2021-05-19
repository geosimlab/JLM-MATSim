package jerusalem.scenario.transit;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class FilterStops
{
	public static void main(String[] args) {
		Config config = ConfigUtils.createConfig();
//		config.plans().setInputFile(path);
		config.transit().setTransitScheduleFile("D:/matsim_jlm/output/10.transitschedule.xml.gz");
		config.network().setInputFile("D:/matsim_jlm/output/17.network.xml.gz");
		config.global().setCoordinateSystem("EPSG:2039");
		Scenario sc =  ScenarioUtils.loadScenario(config);
		Map<Id<TransitStopFacility>, TransitStopFacility> stopFacilities = sc.getTransitSchedule().getFacilities();
		Network network = sc.getNetwork();
		List<Id<TransitStopFacility>> ids = new ArrayList<Id<TransitStopFacility>>(stopFacilities.keySet());
		TransitSchedule ts = sc.getTransitSchedule().getFactory().createTransitSchedule();
		for(Id<TransitStopFacility> id:ids) {
			TransitStopFacility stop = stopFacilities.get(id);
			Link link = network.getLinks().get(stop.getLinkId());
			boolean containsCar = link.getAllowedModes().contains("car");
			if (containsCar) {
				ts.addStopFacility(stop);
			}
		}
		new TransitScheduleWriter(ts).writeFile("D:/matsim_jlm/output/2.stopsonly.xml.gz");
	}
	
}
