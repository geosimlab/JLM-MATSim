package jerusalem.scenario.archive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.OpeningTimeImpl;
import org.matsim.facilities.algorithms.WorldConnectLocations;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.HouseholdUtils;
import org.matsim.households.Households;
import org.matsim.households.HouseholdsFactory;
import org.matsim.households.HouseholdsImpl;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.households.Income;
import org.matsim.households.IncomeImpl;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;
import jerusalem.scenario.network.CreateNetwork;

public class HouseholdPlayground {
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final Properties props = DbUtils.readProperties("database.properties");
	private final static String HOUSEHOLDS_ID = "" + 1;
	private final static String FACILITIES_ID = "" + 1;
	private final static String POPULATION_ID = "" + 4;
	private final static String FAMILY_VEHICLES_ID = "" + 4;
	public final static String POPULATION_OUTPUT_PATH = props.getProperty("folder.output_folder") + POPULATION_ID
			+ ".population.xml.gz";
	public final static String HOUSEHOLDS_OUTPUT_PATH = props.getProperty("folder.output_folder") + HOUSEHOLDS_ID
			+ ".households.xml.gz";
	public final static String FACILITIES_OUTPUT_PATH = props.getProperty("folder.output_folder") + FACILITIES_ID
			+ ".facilities.xml.gz";
	public final static String FAMILY_VEHICLES_OUTPUT_PATH = props.getProperty("folder.output_folder")
			+ FAMILY_VEHICLES_ID + ".family_vehicles.xml.gz";


	public static void main(String[] args) throws SQLException {
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		Network network = new CreateNetwork().getJlmNet();
		new NetworkWriter(network).write(CreateNetwork.NETWORK_ID);
		Population population = createPersons(scenario, con);
		ArrayList<Object> temp1 = createHouseholds(scenario, con);
		Households households = (Households) temp1.get(0);
		Vehicles vehicles = (Vehicles) temp1.get(1);
		ActivityFacilities facilities = createFacilities(scenario, con);
		ArrayList<Object> temp2 = addFacilitiesToHouseholds(facilities, households);
		facilities = (ActivityFacilities) temp2.get(0);
		households = (Households) temp2.get(1);
		network = cutNetwork(network);
		new WorldConnectLocations(config).connectFacilitiesWithLinks(facilities, network);

		population = addHomeToPopulation(households, population);
		population = addPlansToPopulation(population, facilities, con);
		con.close();
		new FacilitiesWriter(facilities).write(FACILITIES_OUTPUT_PATH);
		new HouseholdsWriterV10(households).writeFile(HOUSEHOLDS_OUTPUT_PATH);
		new PopulationWriter(population).write(POPULATION_OUTPUT_PATH);
		new MatsimVehicleWriter(vehicles).writeFile(FAMILY_VEHICLES_OUTPUT_PATH);
	}

	/**
	 * function that cut the network to return links that agents can start their
	 * trip on
	 * 
	 * @param network
	 * @return network
	 */
	public static Network cutNetwork(Network network) {
		NetworkFilterManager nfm = new NetworkFilterManager(network);

		nfm.addLinkFilter(new NetworkLinkFilter() {
			@Override
			public boolean judgeLink(Link l) {
				boolean isLinkOnForbiddenRoadType = Arrays.asList(1.0, 2.0, 3.0, 4.0, 12.0, 66.0, 77.0)
						.contains(l.getId().toString().substring(l.getId().toString().length() - 1));
				boolean IsLinkCarAllowed = l.getAllowedModes().contains("car");
				if (!isLinkOnForbiddenRoadType && IsLinkCarAllowed) {
					return true;
				} else {
					return false;
				}
			}
		});
		Network filteredCarNetwork = nfm.applyFilters();
		return filteredCarNetwork;
	}

	/**
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static Population createPersons(Scenario scenario, Connection con) throws SQLException {
//		PreparedStatement pst = con.prepareStatement("select * from persons where hhid in (select hhid from households h2 where hometaz >1000);");
		PreparedStatement pst = con.prepareStatement("select * from persons;");
		ResultSet resultSet = pst.executeQuery();
		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		log.info("Reading persons");
		int i = 0;
		while (resultSet.next()) {
			Id<Person> personId = Id.create(resultSet.getString("hhid") + "-" + resultSet.getString("pnum"),
					Person.class);
			Person person = populationFactory.createPerson(personId);
			PersonUtils.setAge(person, resultSet.getInt("age"));// persons.age
			PersonUtils.setSex(person, resultSet.getInt("gender") == 1 ? "male" : "female");// persons.gender
			PersonUtils.setEmployed(person, PopUtils.Employed(resultSet.getInt("perstypedetailed")));// persons.persTypeDetailed
			PersonUtils.setLicence(person, resultSet.getInt("driverlicense") == 1 ? "yes" : "no");// persons.driverLicense
			person.getAttributes().putAttribute("hhid", resultSet.getString("hhid"));
			population.addPerson(person);
			if (i % 10000 == 0) {
				log.info("read line #" + i + " from persons table");
			}
			i++;
		}
		return population;
	}

	/**
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<Object> createHouseholds(Scenario scenario, Connection con) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM households;");
//		PreparedStatement pst = con.prepareStatement("SELECT * FROM households where hometaz > 1000;");
		ResultSet resultSet = pst.executeQuery();
		HouseholdsImpl households = (HouseholdsImpl) scenario.getHouseholds();
		HouseholdsFactory householdsFactory = households.getFactory();
		Vehicles vehicles = VehicleUtils.createVehiclesContainer();
		Id<VehicleType> vehTypeId = Id.create("family", VehicleType.class);
		VehicleType type = vehicles.getFactory().createVehicleType(vehTypeId);
		VehicleCapacity cap = type.getCapacity();
		cap.setSeats(50);
		cap.setStandingRoom(0);
		type.setLength(7.5);
		type.setPcuEquivalents(1);
		type.setNetworkMode("car");
		type.setFlowEfficiencyFactor(1);
		vehicles.addVehicleType(type);
		log.info("Reading households");
		int j = 0;
		while (resultSet.next()) {
			// initializing household
			Id<Household> householdId = Id.create(resultSet.getString("hhid"), Household.class);
			HouseholdImpl household = (HouseholdImpl) householdsFactory.createHousehold(householdId);
			// setting income
			IncomeImpl income = new IncomeImpl(resultSet.getInt("hhincomedollars"), Income.IncomePeriod.month);
			household.setIncome(income);
			// setting homeTAZ
			HouseholdUtils.putHouseholdAttribute(household, "HomeTAZ", "" + resultSet.getInt("hometaz"));
			HouseholdUtils.putHouseholdAttribute(household, "sector", "" + resultSet.getInt("sector"));
			// setting members
			List<Id<Person>> memberIds = (List<Id<Person>>) new ArrayList<Id<Person>>();
			for (int i = 0; i < resultSet.getInt("hhsize"); i++) {
				Id<Person> personId = Id.create(resultSet.getString("hhid") + "-" + i, Person.class);
				memberIds.add(personId);
			}
			household.setMemberIds(memberIds);
//			might be redundant
			List<Id<Vehicle>> vehicleIds = new ArrayList<Id<Vehicle>>();
			for (int i = 1; i <= resultSet.getInt("numauto"); i++) {
				Id<Vehicle> vehicleId = Id.create("h-" + resultSet.getString("hhid") + i, Vehicle.class);
				Vehicle vehicle = vehicles.getFactory().createVehicle(vehicleId,
						vehicles.getVehicleTypes().get(vehTypeId));
				vehicles.addVehicle(vehicle);
				vehicleIds.add(vehicleId);
			}
			household.setVehicleIds(vehicleIds);
			households.addHousehold(household);
			if (j % 10000 == 0) {
				log.info("read line #" + j + " from households table");
			}
			j++;
		}
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(households);
		result.add(vehicles);
		return result;
	}

	/**
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static ActivityFacilities createFacilities(Scenario scenario, Connection con) throws SQLException {
		PreparedStatement pst = con.prepareStatement("select taz from taz600;");
		ResultSet resultSet = pst.executeQuery();
		ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities();
		log.info("Reading taz numbers, creating containers");
		while (resultSet.next()) {
			TazFacilities tazFacilities = new TazFacilities();
			facilities.getAttributes().putAttribute("" + resultSet.getInt("taz"), tazFacilities);
		}
		String query = "select st_x(centroid) x, st_y(centroid) y, hometaz,households_at_bldg,uniq_id from bental_households;";
		pst = con.prepareStatement(query);
		resultSet = pst.executeQuery();
		ActivityFacilitiesFactory aff = scenario.getActivityFacilities().getFactory();
		log.info("Reading bental_households");
		int i = 0;
		while (resultSet.next()) {
			Id<ActivityFacility> facilityId = Id.create("" + resultSet.getInt("uniq_id"), ActivityFacility.class);
			Coord facilityCoord = new Coord(resultSet.getDouble("x"), resultSet.getDouble("y"));
			ActivityFacilityImpl activityFacility = (ActivityFacilityImpl) aff.createActivityFacility(facilityId,
					facilityCoord);
			activityFacility.createAndAddActivityOption("home");
			int housingUnits = resultSet.getInt("households_at_bldg");
			int occupiedHousingUnits = 0;
			String taz = "" + resultSet.getInt("hometaz");
			activityFacility.getAttributes().putAttribute("TAZ", taz);
			activityFacility.getAttributes().putAttribute("housing_units", housingUnits);
			activityFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits);
			if (housingUnits == occupiedHousingUnits) {
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "full");
			} else {
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "empty");
			}
			facilities.addActivityFacility(activityFacility);
			if (i % 10000 == 0) {
				log.info("read line #" + i + " from bental_households table");
			}
			i++;
		}
		// TODO amenities doesn't include a methodology to decide on opening and closing
		// times. should add it.
		pst = con.prepareStatement("select * from amenities;");
		resultSet = pst.executeQuery();
		log.info("Reading amenities");
		i = 0;
		while (resultSet.next()) {
			String amen_uniq_id = "" + resultSet.getInt("uniq_id");
			Id<ActivityFacility> facilityId = Id.create(amen_uniq_id, ActivityFacility.class);
			String taz = "" + resultSet.getInt("taz");
			ActivityFacilityImpl activityFacility = null;
			if (!facilities.getFacilities().containsKey(facilityId)) {

				Coord facilityCoord = new Coord(resultSet.getDouble("x"), resultSet.getDouble("y"));
				activityFacility = (ActivityFacilityImpl) aff.createActivityFacility(facilityId, facilityCoord);
				facilities.addActivityFacility(activityFacility);
			} else {
				activityFacility = (ActivityFacilityImpl) facilities.getFacilities().get(facilityId);
			}

			double start_time = (resultSet.getDouble("opening_time") + 180) * 60;
			double end_time = (resultSet.getDouble("closing_time") + 180) * 60;
			// TODO due to major error in JTMT trips data, we use this condition to bypass
			// error
			if (end_time < start_time) {
				start_time = 0;
				end_time = 30 * 3600;
			}
			OpeningTimeImpl openingTime = new OpeningTimeImpl(start_time, end_time);
			activityFacility.createAndAddActivityOption(resultSet.getString("matsim_activity"))
					.addOpeningTime(openingTime);
			activityFacility.getAttributes().putAttribute("TAZ", taz);
			((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId,
					resultSet.getString("matsim_activity"));
			if (i % 10000 == 0) {
				log.info("read line #" + i + " from amenities table");
			}
			i++;
		}
		return facilities;

	}

	/**
	 * @param facilities
	 * @param households
	 * @return
	 */
	public static ArrayList<Object> addFacilitiesToHouseholds(ActivityFacilities facilities, Households households) {
		log.info("Adding facilities to households");
		for (Id<Household> householdId : households.getHouseholds().keySet()) {
			Household household = households.getHouseholds().get(householdId);
			String taz = (String) household.getAttributes().getAttribute("HomeTAZ");
			TazFacilities tazFacilities = ((TazFacilities) facilities.getAttributes().getAttribute(taz));
			ArrayList<Id<ActivityFacility>> facilitiesWithRoom = tazFacilities.getHouseholdsEmpty();
			Random generator = new Random();
			Object[] values = facilitiesWithRoom.toArray();
			Id<ActivityFacility> randomFacilityId = (Id<ActivityFacility>) values[generator.nextInt(values.length)];
			ActivityFacility randomFacility = facilities.getFacilities().get(randomFacilityId);
			int housingUnits = (int) randomFacility.getAttributes().getAttribute("housing_units");
			int occupiedHousingUnits = (int) randomFacility.getAttributes().getAttribute("occupiedHousingUnits");
			household.getAttributes().putAttribute("homeFacilityRefId", randomFacility.getId().toString());
			Coord coord = randomFacility.getCoord();
			household.getAttributes().putAttribute("x", coord.getX());
			household.getAttributes().putAttribute("y", coord.getY());
			randomFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits + 1);
			if (housingUnits == occupiedHousingUnits + 1) {
				tazFacilities.removeFromList(randomFacilityId, "empty");
				tazFacilities.addToList(randomFacilityId, "full");
			}

		}

		ArrayList<Object> result = new ArrayList<Object>();
		result.add(facilities);
		result.add(households);
		return result;
	}

	/**
	 * @param households
	 * @param population
	 * @return
	 */
	public static Population addHomeToPopulation(Households households, Population population) {
		log.info("Adding home attribute to population");
		for (Id<Person> personId : population.getPersons().keySet()) {
			Person person = population.getPersons().get(personId);
			Id<Household> housholdId = Id.create(person.getAttributes().getAttribute("hhid").toString(),
					Household.class);
			Household houshold = households.getHouseholds().get(housholdId);
			String homeFacilityRefId = (String) houshold.getAttributes().getAttribute("homeFacilityRefId");
			person.getAttributes().putAttribute("homeFacilityRefId", homeFacilityRefId);

		}
		return population;

	}

	/**
	 * @param population
	 * @param facilities
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static Population addPlansToPopulation(Population population, ActivityFacilities facilities, Connection con)
			throws SQLException {
		log.info("Adding plans to population");

		con.setAutoCommit(false);
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(50000);
//		all trips
		String query = "select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips;";
//		without city centre, change households and persons as well
//		String query = "select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips where origtaz > 1000 and hhid in (select hhid from households where hometaz > 1000);");
//		taz < 1000
//		String query = "with first_table as(select distinct hhid,pnum from trips where origtaz < 1000), second_table as(select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips) select second_table.* from first_table left join second_table using (hhid,pnum);";
//		taz = 3,14,15,304,409
//		String query = "with first_table as(select distinct hhid,pnum from trips where origtaz in (3,14,15,304,409)), second_table as(select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips) select second_table.* from first_table left join second_table using (hhid,pnum);";
//		taz < 2000
//		String query = "with first_table as(select distinct hhid,pnum from trips where origtaz < 2000), second_table as(select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips) select second_table.* from first_table left join second_table using (hhid,pnum);";
		ResultSet resultSet = statement.executeQuery(query);
		
		// TODO trips has major problems. just notice they are repaired
		PopulationFactory populationFactory = population.getFactory();
		Person person = null;
		Plan plan = populationFactory.createPlan();
		Activity activity = null;
		int i = 0;
		while (resultSet.next()) {
			String activityType = PopUtils.ActivityType(resultSet.getInt("origPurp"));
			String id = resultSet.getInt("hhid") + "-" + resultSet.getInt("pnum");
			Id<Person> personId = Id.create(id, Person.class);
			double endTime = resultSet.getDouble("finalDepartMinute") * 60 + 3 * 60 * 60;
			// if home, go to person attributes
			if (activityType == "home") {
				activity = createHomeActivity(population, populationFactory, personId, facilities);
			}
			// get random facility from amenities in taz
			else {
				String taz = "" + resultSet.getInt("origtaz");
				ArrayList<Id<ActivityFacility>> tazFacilities = ((TazFacilities) facilities.getAttributes()
						.getAttribute(taz)).getAList(activityType);
				Random generator = new Random();
				Object[] values = tazFacilities.toArray();
				Id<ActivityFacility> randomFacilityId = (Id<ActivityFacility>) values[generator.nextInt(values.length)];

				activity = populationFactory.createActivityFromActivityFacilityId(activityType, randomFacilityId);
				activity.setCoord(facilities.getFacilities().get(randomFacilityId).getCoord());
				activity.setLinkId(facilities.getFacilities().get(randomFacilityId).getLinkId());
			}
			// add activity and leg to plan
			activity.setEndTime(endTime);
			plan.addActivity(activity);
			String mode = PopUtils.Mode(resultSet.getInt("modeCode"));
			plan.addLeg(populationFactory.createLeg(mode));
			// checking if person ended or that table ended

			if (!resultSet.next() || resultSet.getInt("personTripNum") == 1) {
				activity = createHomeActivity(population, populationFactory, personId, facilities);
				plan.addActivity(activity);
				person = population.getPersons().get(personId);
				person.addPlan(plan);
				plan = populationFactory.createPlan();
			}
			resultSet.previous();
			if (i % 10000 == 0) {
				log.info("read line #" + i + " from trips table");
			}
			i++;
		}
		// scan through population. if person has no plan, create a plan where
		// person stays at home
		for (Id<Person> personId : population.getPersons().keySet()) {
			person = population.getPersons().get(personId);
			if (person.getPlans().isEmpty()) {
				plan = populationFactory.createPlan();
				activity = createHomeActivity(population, populationFactory, personId, facilities);
				plan.addActivity(activity);
				person.addPlan(plan);
			}
		}
		return population;
	}

	/**
	 * @param population
	 * @param populationFactory
	 * @param personId
	 * @param facilities
	 * @return
	 */
	public static Activity createHomeActivity(Population population, PopulationFactory populationFactory,
			Id<Person> personId, ActivityFacilities facilities) {
		String homeFacilityRefId = (String) population.getPersons().get(personId).getAttributes()
				.getAttribute("homeFacilityRefId");
		Id<ActivityFacility> facilityId = Id.create(homeFacilityRefId, ActivityFacility.class);
		Activity activity = populationFactory.createActivityFromActivityFacilityId("home", facilityId);
		activity.setCoord(facilities.getFacilities().get(facilityId).getCoord());
		activity.setLinkId(facilities.getFacilities().get(facilityId).getLinkId());
		return activity;
	}

}
