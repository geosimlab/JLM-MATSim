package jerusalem.events;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;

/**
 * This event handler prints some event information to the console.
 * 
 * @author dgrether
 *
 */
public class MyEventHandler1 implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonDepartureEventHandler,
		PersonArrivalEventHandler {
	public Map<Id<Link>, Integer> onLink = new HashMap<>();

	@Override
	public void reset(int iteration) {
		System.out.println("reset...");
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {

		this.onLink.put(event.getLinkId(), 1 + this.onLink.get(event.getLinkId()));
//		System.out.println("LinkEnterEvent");
//		System.out.println("Time: " + event.getTime());
//		System.out.println("LinkId: " + event.getLinkId());
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		this.onLink.put(event.getLinkId(), -1 + this.onLink.get(event.getLinkId()));
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		// TODO Auto-generated method stub
		System.out.println("arrived");
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		// TODO Auto-generated method stub
		System.out.println("depateed");
	}

}