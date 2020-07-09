package jerusalem.events;

import java.util.ArrayList;
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
import org.matsim.vehicles.Vehicle;

/**
 * This EventHandler implementation counts the travel time of all agents and
 * provides the average travel time per agent. Actually, handling Departures and
 * Arrivals should be sufficient for this (may 2014)
 * 
 * @author dgrether
 *
 */
public class CongestionDetectionEventHandler implements LinkEnterEventHandler, LinkLeaveEventHandler,
		PersonDepartureEventHandler, PersonArrivalEventHandler {

	private Map<Id<Vehicle>, Double> timeOnLink = new HashMap<>();
	private Map<Id<Link>, Integer> linkCount = new HashMap<>();
	private ArrayList<String> distanceDistribution = new ArrayList<String>();

	public ArrayList<String> getDistanceDistribution() {
		return distanceDistribution;
	}

	public void setDistanceDistribution(ArrayList<String> distanceDistribution) {
		this.distanceDistribution = distanceDistribution;
	}

	public Map<Id<Vehicle>, Double> getTimeOnLink() {
		return timeOnLink;
	}

	public void setTimeOnLink(Map<Id<Vehicle>, Double> earliestLinkExitTime) {
		this.timeOnLink = earliestLinkExitTime;
	}

	@Override
	public void reset(int iteration) {
		this.timeOnLink.clear();
		this.linkCount.clear();
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		System.out.println("departure");
		if (event.getLegMode() == "car") {
			Id<Vehicle> vehId = Id.create(event.getPersonId(), Vehicle.class);
			Id<Link> linkId = Id.create(event.getLinkId(), Link.class);
			this.timeOnLink.put(vehId, event.getTime());
			if (!this.linkCount.containsKey(linkId)) {
				this.linkCount.put(linkId, 0);
			}
			this.linkCount.put(linkId, this.linkCount.get(linkId) + 1);
			distanceDistribution.add(vehId + "," + event.getTime() + "," + linkId + "," + 0 + ","
					+ this.linkCount.get(linkId) + "," + "departure");
		}

	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		System.out.println("exit");

		Id<Vehicle> vehId = Id.create(event.getVehicleId(), Vehicle.class);
		Id<Link> linkId = Id.create(event.getLinkId(), Link.class);
		double excessTravelTime = event.getTime() - this.timeOnLink.get(vehId);
		this.timeOnLink.put(vehId, excessTravelTime);
		this.linkCount.put(linkId, -1 + this.linkCount.get(linkId));
		distanceDistribution.add(vehId + "," + event.getTime() + "," + linkId + "," + excessTravelTime + ","
				+ this.linkCount.get(linkId) + "," + "exit");
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		System.out.println("enter");
		Id<Vehicle> vehId = Id.create(event.getVehicleId(), Vehicle.class);
		Id<Link> linkId = Id.create(event.getLinkId(), Link.class);
		double excessTravelTime = event.getTime() - this.timeOnLink.get(vehId);
		this.timeOnLink.put(vehId, excessTravelTime);
		if (!this.linkCount.containsKey(linkId)) {
			this.linkCount.put(linkId, 0);
		}
		this.linkCount.put(linkId, 1 + this.linkCount.get(linkId));

		distanceDistribution.add(vehId + "," + event.getTime() + "," + linkId + "," + excessTravelTime + ","
				+ this.linkCount.get(linkId) + "," + "enter");
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		System.out.println("arrival");
		if (event.getLegMode() == "car") {
			Id<Vehicle> vehId = Id.create(event.getPersonId(), Vehicle.class);
			Id<Link> linkId = Id.create(event.getLinkId(), Link.class);
			double excessTravelTime = event.getTime() - this.timeOnLink.get(vehId);
			this.timeOnLink.put(vehId, excessTravelTime);
			this.linkCount.put(linkId, -1 + this.linkCount.get(linkId));
			distanceDistribution.add(vehId + "," + event.getTime() + "," + linkId + "," + excessTravelTime + ","
					+ this.linkCount.get(linkId) + "," + "arrival");
		}

	}
}