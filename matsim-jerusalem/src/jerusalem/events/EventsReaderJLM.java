package jerusalem.events;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;

public class EventsReaderJLM {
	private final static Logger log = Logger.getLogger(EventsReaderJLM.class);

	public static void main(String[] args) throws IOException {
//		Network network = NetworkUtils
//				.readNetwork("D:/Users/User/Dropbox/matsim_begin/output/7/7.output_network.xml.gz");
		log.info("reading events");
		String eventsFile = "D:/Users/User/Dropbox/matsim_begin/output/7/7.output_events.xml.gz";

		// create an event object
		EventsManager events = EventsUtils.createEventsManager();

		// create the handler and add it
		CongestionDetectionEventHandler handler1 = new CongestionDetectionEventHandler();
		events.addHandler(handler1);
		// create the reader and read the file
		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(eventsFile);
		writeEventsToFile(handler1.getDistanceDistribution(), "D:/Users/User/Dropbox/matsim_begin/output/q.csv");

//		Population population = PopulationUtils
//				.readPopulation("D:/Users/User/Dropbox/matsim_begin/output/7/7.output_plans.xml.gz");
//		for (Id<Person> id : population.getPersons().keySet()) {
//			Person person = population.getPersons().get(id);
//			Plan plan = person.getPlans().get(0);
//			if (plan.getPlanElements().size() > 1) {
//				Leg firstLeg = (Leg) plan.getPlanElements().get(1);
//				if(firstLeg.getMode() == "car") {
//					System.out.println(firstLeg.getRoute()..toString());
//				}
//				
//			}
//		}
	}

	static void writeEventsToFile(ArrayList<String> distanceDistribution, String fileName) {
		BufferedWriter bw = IOUtils.getBufferedWriter(fileName);
		try {
			bw.write("Vehicle,Time,Link,excessTravelTime,vehiclesOnLink,enevtType");
			for (String string : distanceDistribution) {
				bw.newLine();
				bw.write(string);
			}
			bw.flush();
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
