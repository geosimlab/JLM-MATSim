package jerusalem.scenario.transit;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

/**
 * Generates transit vehicles for each given transit line of a given schedule.
 * 
 * @author Ihab
 *
 */

public class VehiclesGenerator {
	private final static Logger log = Logger.getLogger(VehiclesGenerator.class);
	private Vehicles veh = VehicleUtils.createVehiclesContainer();

	/**
	 * Generates transit vehicles for each given transit line of a given schedule.
	 * If pcu is 0.0 the pcu value is calculated based on the given length. If the
	 * pcu value is not 0.0 the length is ignored and has no effect.
	 * 
	 */
	public void createVehicles(TransitSchedule schedule, List<Id<TransitLine>> lineIDs, int busSeats, int standingRoom,
			double length, Id vehTypeId, double egressSeconds, double accessSeconds,
			VehicleType.DoorOperationMode doorOperationMode, double pcu, double maxVelocity) {

		if (pcu == 0.) {
			log.info(
					"Passenger car equivalents (pcu) is 0.0. Calculating a pcu value based on the given vehicle length of "
							+ length + " meters.");
			log.info(
					"Assumptions: 1 pcu = 7.5 meters (default); Adding 3 meters on top of the vehicle length (1.5m before and behind the vehicle)");
			pcu = (length + 3) / 7.5;
			log.info("Calculated pcu: " + pcu);
		} else {
			log.warn("Ignoring vehicle length. Using pcu instead.");
		}

		for (Id<TransitLine> transitLineId : lineIDs) {
			log.info("Creating transit vehicles for transit line " + transitLineId);
			List<Id> vehicleIDs = new ArrayList<Id>();

			for (TransitRoute transitRoute : schedule.getTransitLines().get(transitLineId).getRoutes().values()) {

				for (Departure dep : transitRoute.getDepartures().values()) {

					if (vehicleIDs.contains(dep.getVehicleId())) {
						// vehicle Id already in list
					} else {
						vehicleIDs.add(dep.getVehicleId());
					}
				}
			}

			VehicleType type = veh.getFactory().createVehicleType(vehTypeId);
			VehicleCapacity cap = type.getCapacity();
			cap.setSeats(busSeats);
			cap.setStandingRoom(standingRoom);
//			type.setCapacity(cap);
			type.setLength(length);
			type.setAccessTime(accessSeconds);
			type.setEgressTime(egressSeconds);
//			type.setDoorOperationMode(doorOperationMode);
			VehicleUtils.setDoorOperationMode(type, doorOperationMode);

			type.setMaximumVelocity(maxVelocity);
			type.setPcuEquivalents(pcu);

			veh.addVehicleType(type);

			if (vehicleIDs.isEmpty()) {
				throw new RuntimeException("At least 1 Bus is expected. Aborting...");
			} else {
				for (Id vehicleId : vehicleIDs) {
					Vehicle vehicle = veh.getFactory().createVehicle(vehicleId, veh.getVehicleTypes().get(vehTypeId));
					veh.addVehicle(vehicle);
				}
			}
		}
	}

	public Vehicles getVehicles() {
		return this.veh;
	}

}
