package jerusalem.scenario.population;

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
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
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



import jerusalem.scenario.db.DbInitialize;
import jerusalem.scenario.db.DbUtils;
import jerusalem.scenario.network.CreateNetwork;

public class FacilitiesHouseholdsPopulationCreator
{
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final Properties props = DbUtils.readProperties("database.properties");
	private final static String HOUSEHOLDS_ID = "" + 1;
	private final static String FACILITIES_ID = "" + 3;
	private final static String POPULATION_ID = "" + 7;
	private final static String FAMILY_VEHICLES_ID = "" + 4;
	public final static String POPULATION_OUTPUT_PATH = props.getProperty("folder.output_folder") + POPULATION_ID
			+ ".population.xml.gz";
	public final static String HOUSEHOLDS_OUTPUT_PATH = props.getProperty("folder.output_folder") + HOUSEHOLDS_ID
			+ ".households.xml.gz";
	public final static String FACILITIES_OUTPUT_PATH = props.getProperty("folder.output_folder") + FACILITIES_ID
			+ ".facilities.xml.gz";
	public final static String FAMILY_VEHICLES_OUTPUT_PATH = props.getProperty("folder.output_folder")
			+ FAMILY_VEHICLES_ID + ".family_vehicles.xml.gz";

	public static void main(String[] args) throws SQLException
	{
//		connection to the database
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
//		starting new config and scenario
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
//		loading network in order that facilities will connect only with allowed links
		Network network = new CreateNetwork().getJlmNet();
//		running this makes running main in createnetwork redundant
		new NetworkWriter(network).write(CreateNetwork.NETWORK_OUTPUT_PATH);
//		creating persons and households  
		Population population = createPersons(scenario, con);
		ArrayList<Object> temp1 = createHouseholds(scenario, con);
//		unloading households and vehicles
		Households households = (Households) temp1.get(0);
		Vehicles vehicles = (Vehicles) temp1.get(1);
//		creating facilities
		ActivityFacilities facilities = createFacilities(scenario, con);
//		connectiong households and facilities
		ArrayList<Object> temp2 = addFacilitiesToHouseholds(facilities, households);
//		unloading both
		facilities = (ActivityFacilities) temp2.get(0);
		households = (Households) temp2.get(1);
//		cutting the network to link allowed to start/end trip on
		network = cutNetwork(network);
		new WorldConnectLocations(config).connectFacilitiesWithLinks(facilities, network);
//		creating population from persons, households and facilities 
		population = addHomeToPopulation(households, population);
		population = addPlansToInternalAgents(population, facilities, con);
		population = addExternalAgents(population, facilities, con);
		con.close();
//		Writing all files
		new FacilitiesWriter(facilities).write(FACILITIES_OUTPUT_PATH);
		new HouseholdsWriterV10(households).writeFile(HOUSEHOLDS_OUTPUT_PATH);
		new PopulationWriter(population).write(POPULATION_OUTPUT_PATH);
	}

	/**
	 * function that cuts the network to return links that agents can start their
	 * trip on. Meant to defer to start in Freeway,Highway,Regional Road,Local / Access road,LRT, Busway and Rail
	 * This means network manipulations have already been carried out
	 * 
	 * @param network
	 * @return network
	 */
	public static Network cutNetwork(Network network)
	{
		NetworkFilterManager nfm = new NetworkFilterManager(network);

		nfm.addLinkFilter(new NetworkLinkFilter()
		{
			@Override
			public boolean judgeLink(Link l)
			{
				boolean isLinkOnForbiddenRoadType = Arrays.asList(1.0, 2.0, 3.0, 4.0, 12.0, 66.0, 77.0)
						.contains(l.getId().toString().substring(l.getId().toString().length() - 1));
				boolean IsLinkCarAllowed = l.getAllowedModes().contains("car");
				if (!isLinkOnForbiddenRoadType && IsLinkCarAllowed)
				{
					return true;
				} else
				{
					return false;
				}
			}
		});
		Network filteredCarNetwork = nfm.applyFilters();
		return filteredCarNetwork;
	}

	/**
	 * reading the persons sql table into an initial population object 
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static Population createPersons(Scenario scenario, Connection con) throws SQLException
	{
		PreparedStatement pst = con.prepareStatement("select * from persons;");
		ResultSet resultSet = pst.executeQuery();
		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		log.info("Reading persons");
		int i = 0;
		while (resultSet.next())
		{
			Id<Person> personId = Id.create(resultSet.getString("hhid") + "-" + resultSet.getString("pnum"),
					Person.class);//creating a unique matsin ID for each agent 
			Person person = populationFactory.createPerson(personId);
			PersonUtils.setAge(person, resultSet.getInt("age"));// persons.age
			PersonUtils.setSex(person, resultSet.getInt("gender") == 1 ? "male" : "female");// persons.gender
			PersonUtils.setEmployed(person, PopUtils.Employed(resultSet.getInt("perstypedetailed")));// persons.persTypeDetailed
			PersonUtils.setLicence(person, resultSet.getInt("driverlicense") == 1 ? "yes" : "no");// persons.driverLicense
			person.getAttributes().putAttribute("hhid", resultSet.getString("hhid"));// persons.hhid
			
			population.addPerson(person);
			if (i % 10000 == 0)
			{
				log.info("read line #" + i + " from persons table");
			}
			i++;
		}
		return population;
	}

	/**
	 * reading the households sql table into an initial households object
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<Object> createHouseholds(Scenario scenario, Connection con) throws SQLException
	{
		PreparedStatement pst = con.prepareStatement("SELECT * FROM households;");
		ResultSet resultSet = pst.executeQuery();
		// Initialising households
		HouseholdsImpl households = (HouseholdsImpl) scenario.getHouseholds();
		HouseholdsFactory householdsFactory = households.getFactory();
		// setting up vehicles (might be redundant)
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
		while (resultSet.next())
		{
			// Initialising household
			Id<Household> householdId = Id.create(resultSet.getString("hhid"), Household.class);
			HouseholdImpl household = (HouseholdImpl) householdsFactory.createHousehold(householdId);
			// setting income
			IncomeImpl income = new IncomeImpl(resultSet.getInt("hhincomedollars"), Income.IncomePeriod.month);
			household.setIncome(income);
			// setting homeTAZ
			HouseholdUtils.putHouseholdAttribute(household, "HomeTAZ", "" + resultSet.getInt("hometaz"));
			// setting sector
			String sector = null;
			switch(resultSet.getInt("sector")) {
			case 1:
				sector = "arab";
				break;
			case 2:
				sector = "Ultra-Orthodox";
				break;
			case 3:
				sector = "Secular";
				break;
			case 4:
				sector = "Palestine";
				break;
			}
			HouseholdUtils.putHouseholdAttribute(household, "sector", sector);
			// setting members
			List<Id<Person>> memberIds = (List<Id<Person>>) new ArrayList<Id<Person>>();
			for (int i = 0; i < resultSet.getInt("hhsize"); i++)
			{
				Id<Person> personId = Id.create(resultSet.getString("hhid") + "-" + i, Person.class);
				memberIds.add(personId);
			}
			household.setMemberIds(memberIds);
			// setting vehicle (might be redundant, not if ridesharing is possible)
			List<Id<Vehicle>> vehicleIds = new ArrayList<Id<Vehicle>>();
			for (int i = 1; i <= resultSet.getInt("numauto"); i++)
			{
				Id<Vehicle> vehicleId = Id.create("h-" + resultSet.getString("hhid") + i, Vehicle.class);
				Vehicle vehicle = vehicles.getFactory().createVehicle(vehicleId,
						vehicles.getVehicleTypes().get(vehTypeId));
				vehicles.addVehicle(vehicle);
				vehicleIds.add(vehicleId);
			}
			household.setVehicleIds(vehicleIds);
			households.addHousehold(household);
			if (j % 10000 == 0)
			{
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
	 * creating activity facilities from from tazs using three sql tables: taz map, bental_households and amentied
	 * @param scenario
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static ActivityFacilities createFacilities(Scenario scenario, Connection con) throws SQLException
	{
		// Initialising facilities
		PreparedStatement pst = con.prepareStatement("select taz from taz600;");
		ResultSet resultSet = pst.executeQuery();
		ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities();
		// creating a taz facilities object for each taz
		log.info("Reading taz numbers, creating containers");
		while (resultSet.next())
		{
			TazFacilities tazFacilities = new TazFacilities();
			facilities.getAttributes().putAttribute("" + resultSet.getInt("taz"), tazFacilities);
		}
		// getting housing facilities from bental
		String query = "select st_x(centroid) x, st_y(centroid) y, taz,households_at_bldg,uniq_id from bental_households;";
		pst = con.prepareStatement(query);
		resultSet = pst.executeQuery();
		ActivityFacilitiesFactory aff = scenario.getActivityFacilities().getFactory();
		log.info("Reading bental_households");
		int i = 0;
		while (resultSet.next())
		{
			// uniq id
			Id<ActivityFacility> facilityId = Id.create("" + resultSet.getInt("uniq_id"), ActivityFacility.class);
			// coordinates
			Coord facilityCoord = new Coord(resultSet.getDouble("x"), resultSet.getDouble("y"));
			// new activiry facility
			ActivityFacilityImpl activityFacility = (ActivityFacilityImpl) aff.createActivityFacility(facilityId,
					facilityCoord);
			// all facilities in bental households have home, tjlm and fjlm activites			
			activityFacility.createAndAddActivityOption("home");
			activityFacility.createAndAddActivityOption("tjlm");
			activityFacility.createAndAddActivityOption("fjlm");
			// setting number of housing units and occupied housing units
			int housingUnits = resultSet.getInt("households_at_bldg");
			int occupiedHousingUnits = 0;
			String taz = "" + resultSet.getInt("taz");
			activityFacility.getAttributes().putAttribute("TAZ", taz);
			activityFacility.getAttributes().putAttribute("housing_units", housingUnits);
			activityFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits);
			((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "tjlm");
			((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "fjlm");
			// if all housing units occupied (=0), push them to full. else - push to empty
			if (housingUnits == occupiedHousingUnits)
			{
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "full");
			} else
			{
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "empty");
			}
			facilities.addActivityFacility(activityFacility);
			if (i % 10000 == 0)
			{
				log.info("read line #" + i + " from bental_households table");
			}
			i++;
		}
		// TODO amenities doesn't include a methodology to decide on opening and closing
		// times. should add it. additionally  - there is no capacity for amenties. maybe add it in the future.
		pst = con.prepareStatement("select * from amenities;");
		resultSet = pst.executeQuery();
		log.info("Reading amenities");
		i = 0;
		while (resultSet.next())
		{
			// setting id			
			String amen_uniq_id = "" + resultSet.getInt("uniq_id");
			Id<ActivityFacility> facilityId = Id.create(amen_uniq_id, ActivityFacility.class);
			String taz = "" + resultSet.getInt("taz");
			ActivityFacilityImpl activityFacility = null;
			// condition to differntiate between mixed housing facilities and pure amenities
			if (!facilities.getFacilities().containsKey(facilityId))
			{
				Coord facilityCoord = new Coord(resultSet.getDouble("x"), resultSet.getDouble("y"));
				activityFacility = (ActivityFacilityImpl) aff.createActivityFacility(facilityId, facilityCoord);
				activityFacility.createAndAddActivityOption("tjlm");
				activityFacility.createAndAddActivityOption("fjlm");
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "tjlm");
				((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId, "fjlm");
				facilities.addActivityFacility(activityFacility);
			} else
			{
				activityFacility = (ActivityFacilityImpl) facilities.getFacilities().get(facilityId);
			}
			// adding opening and closing time to the facility
			// opening and closing time is computed by taz in the sql query create amenites
			double start_time = (resultSet.getDouble("opening_time") + 180) * 60;
			double end_time = (resultSet.getDouble("closing_time") + 180) * 60;
			// due to major error in JTMT trips data, we use this condition to bypass error 
			if (end_time < start_time)
			{
				start_time = 0;
				end_time = 30 * 3600;
			}
			OpeningTimeImpl openingTime = new OpeningTimeImpl(start_time, end_time);
			activityFacility.createAndAddActivityOption(resultSet.getString("matsim_activity"))
					.addOpeningTime(openingTime);
			// adding taz
			activityFacility.getAttributes().putAttribute("TAZ", taz);
			// pushing to the right container in the taz facilities object
			((TazFacilities) facilities.getAttributes().getAttribute(taz)).addToList(facilityId,
					resultSet.getString("matsim_activity"));
			if (i % 10000 == 0)
			{
				log.info("read line #" + i + " from amenities table");
			}
			i++;
		}
		return facilities;

	}

	/**
	 * Assigning households to facilities
	 * Assigning facilty as home for each household
	 * @param facilities
	 * @param households
	 * @return
	 */
	public static ArrayList<Object> addFacilitiesToHouseholds(ActivityFacilities facilities, Households households)
	{
		// Setting random seed for assignment of households in facilities
		Random generator = new Random();
		generator.setSeed(1234);
		log.info("Adding facilities to households");
		for (Id<Household> householdId : households.getHouseholds().keySet())
		{
			Household household = households.getHouseholds().get(householdId);
			// getting taz of household
			String taz = (String) household.getAttributes().getAttribute("HomeTAZ");
			// Getting taz facilities
			TazFacilities tazFacilities = ((TazFacilities) facilities.getAttributes().getAttribute(taz));
			// getting empty facilities
			ArrayList<Id<ActivityFacility>> facilitiesWithRoom = tazFacilities.getHouseholdsEmpty();
			// choosing a random empty facility in the taz
			Object[] values = facilitiesWithRoom.toArray();
			Id<ActivityFacility> randomFacilityId = (Id<ActivityFacility>) values[generator.nextInt(values.length)];
			ActivityFacility randomFacility = facilities.getFacilities().get(randomFacilityId);
			// setting household facility id and coords
			household.getAttributes().putAttribute("homeFacilityRefId", randomFacility.getId().toString());
			Coord coord = randomFacility.getCoord();
			household.getAttributes().putAttribute("x", coord.getX());
			household.getAttributes().putAttribute("y", coord.getY());
			// adding one housing units to the occupied housing units
			int housingUnits = (int) randomFacility.getAttributes().getAttribute("housing_units");
			int occupiedHousingUnits = (int) randomFacility.getAttributes().getAttribute("occupiedHousingUnits");
			randomFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits + 1);
			// if all housing units in facility are full, push it into the full list
			if (housingUnits == occupiedHousingUnits + 1)
			{
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
	 * adding households attributes to agents
	 * @param households
	 * @param population
	 * @return
	 */
	public static Population addHomeToPopulation(Households households, Population population)
	{
		// this is done because persons do not inherit from households
		log.info("Adding home attribute to population");
		for (Id<Person> personId : population.getPersons().keySet())
		{
			Person person = population.getPersons().get(personId);
			Id<Household> housholdId = Id.create(person.getAttributes().getAttribute("hhid").toString(),
					Household.class);
			Household houshold = households.getHouseholds().get(housholdId);
			String homeFacilityRefId = (String) houshold.getAttributes().getAttribute("homeFacilityRefId");
			String sector = (String) houshold.getAttributes().getAttribute("sector");
			person.getAttributes().putAttribute("homeFacilityRefId", homeFacilityRefId);
			person.getAttributes().putAttribute("subpopulation", "internal_"+sector);
		}
		return population;

	}

	/**
	 * Adding plans to agents
	 * @param population
	 * @param facilities
	 * @param con
	 * @return population
	 * @throws SQLException
	 */
	public static Population addPlansToInternalAgents(Population population, ActivityFacilities facilities, Connection con)
			throws SQLException
	{
		log.info("Adding plans to population");
		// large table, set maximum pulling data to 50000 rows, make going to previous
		// row possible
		con.setAutoCommit(false);
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(50000);
		String query = "select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips;";
		ResultSet resultSet = statement.executeQuery(query);
		PopulationFactory populationFactory = population.getFactory();
		// Initialising necessary variables
		Person person = null;
		Plan plan = populationFactory.createPlan();
		Activity activity = null;
		Random generator = new Random();
		generator.setSeed(1234);
		int i = 0;
		// looping through trips
		while (resultSet.next())
		{
			String activityType = PopUtils.ActivityType(resultSet.getInt("origPurp"));
			String id = resultSet.getInt("hhid") + "-" + resultSet.getInt("pnum");
			Id<Person> personId = Id.create(id, Person.class);
			double endTime = resultSet.getDouble("finalDepartMinute") * 60 + 3 * 60 * 60;
			// if home, go to person attributes and set a home activity
			if (activityType == "home")
			{
				activity = createHomeActivity(population, populationFactory, personId, facilities);
			}
			// if amenity, get random facility from amenities in taz
			else
			{
				String taz = "" + resultSet.getInt("origtaz");
				ArrayList<Id<ActivityFacility>> tazFacilities = ((TazFacilities) facilities.getAttributes()
						.getAttribute(taz)).getAList(activityType);
				Object[] values = tazFacilities.toArray();
				Id<ActivityFacility> randomFacilityId = (Id<ActivityFacility>) values[generator.nextInt(values.length)];
				activity = populationFactory.createActivityFromActivityFacilityId(activityType, randomFacilityId);
				// TODO check if prohibited road types are excluded
				activity.setCoord(facilities.getFacilities().get(randomFacilityId).getCoord());
				activity.setLinkId(facilities.getFacilities().get(randomFacilityId).getLinkId());
			}
			// add activity and leg to plan
			activity.setEndTime(endTime);
			plan.addActivity(activity);
			String mode = PopUtils.Mode(resultSet.getInt("modeCode"));
			plan.addLeg(populationFactory.createLeg(mode));
			// checking if person trips ended or that table ended
			if (!resultSet.next() || resultSet.getInt("personTripNum") == 1)
			{
				// create home activity as final activity
				activity = createHomeActivity(population, populationFactory, personId, facilities);
				// add activity to plan
				plan.addActivity(activity);
				// add plan to person
				person = population.getPersons().get(personId);
				person.addPlan(plan);
				// create new plan for next person
				plan = populationFactory.createPlan();
			}
			resultSet.previous();
			if (i % 10000 == 0)
			{
				log.info("read line #" + i + " from trips table");
			}
			i++;
		}
		// scan through population. if person has no plan, create a plan where
		// person stays at home
		for (Id<Person> personId : population.getPersons().keySet())
		{
			person = population.getPersons().get(personId);
			if (person.getPlans().isEmpty())
			{
				plan = populationFactory.createPlan();
				activity = createHomeActivity(population, populationFactory, personId, facilities);
				plan.addActivity(activity);
				person.addPlan(plan);
			}
		}
		return population;
	}

	/**
	 * Helper function to create home activity
	 * @param population
	 * @param populationFactory
	 * @param personId
	 * @param facilities
	 * @return
	 */
	public static Activity createHomeActivity(Population population, PopulationFactory populationFactory,
			Id<Person> personId, ActivityFacilities facilities)
	{
		String homeFacilityRefId = (String) population.getPersons().get(personId).getAttributes()
				.getAttribute("homeFacilityRefId");
		Id<ActivityFacility> facilityId = Id.create(homeFacilityRefId, ActivityFacility.class);
		Activity activity = populationFactory.createActivityFromActivityFacilityId("home", facilityId);
		activity.setCoord(facilities.getFacilities().get(facilityId).getCoord());
		activity.setLinkId(facilities.getFacilities().get(facilityId).getLinkId());
		return activity;
	}

	/**
	 * adding external agents derived from OD matrix
	 * @param population
	 * @param facilities
	 * @param con
	 * @return
	 * @throws SQLException
	 */
	public static Population addExternalAgents(Population population, ActivityFacilities facilities, Connection con)
			throws SQLException
	{
		PreparedStatement pst = con.prepareStatement("select * from external_agents where destination in (select taz from taz600) and origin in (select taz from taz600);");
		ResultSet resultSet = pst.executeQuery();
		PopulationFactory pf = population.getFactory();
		Random rnd = MatsimRandom.getLocalInstance();
		rnd.setSeed(1234);
		Random origgenerator = new Random();
		origgenerator.setSeed(1234);
		Random destgenerator = new Random();
		destgenerator.setSeed(1234);
		log.info("Reading External agents");
		int i = 0;
		while (resultSet.next())
		{
			String activityTargetString;
			if (resultSet.getInt("origin") < 9000)
			{
				activityTargetString = "fjlm";
			} else
			{
				activityTargetString = "tjlm";
			}
			// person attributes settings
			Id<Person> personId = Id.create(
					activityTargetString + "_" + resultSet.getInt("origin") + "_" + resultSet.getInt("destination")
							+ "_" + resultSet.getInt("day_period") + "_" + resultSet.getInt("generate_series"),
					Person.class);
			Person person = pf.createPerson(personId);
			PersonUtils.setAge(person, (int) (Math.floor(rnd.nextDouble() * 67) + 18));// random age between 18 and 84
			PersonUtils.setSex(person, rnd.nextDouble() > 0.5 ? "male" : "female");// random male female
			PersonUtils.setEmployed(person, true);// all external agents are working in JLM metro
			PersonUtils.setLicence(person, "yes");// all drivers have license
			person.getAttributes().putAttribute("subpopulation", "external");
			// origin
			String origtaz = "" + resultSet.getInt("origin");
			ArrayList<Id<ActivityFacility>> origTazFacilities = ((TazFacilities) facilities.getAttributes()
					.getAttribute(origtaz)).getAList(activityTargetString);
			Object[] origvalues = origTazFacilities.toArray();
			Id<ActivityFacility> origRandomFacilityId = (Id<ActivityFacility>) origvalues[origgenerator
					.nextInt(origvalues.length)];
			// destination
			String desttaz = "" + resultSet.getInt("destination");
			ArrayList<Id<ActivityFacility>> destTazFacilities = ((TazFacilities) facilities.getAttributes()
					.getAttribute(desttaz)).getAList(activityTargetString);
			Object[] destvalues = destTazFacilities.toArray();
			Id<ActivityFacility> destRandomFacilityId = (Id<ActivityFacility>) destvalues[destgenerator
					.nextInt(destvalues.length)];
			// trip settings, setting plan
			Plan plan = pf.createPlan();
			Activity activityHome = pf.createActivityFromActivityFacilityId("home",
					Id.create(origRandomFacilityId, ActivityFacility.class));
			activityHome.setCoord(facilities.getFacilities().get(origRandomFacilityId).getCoord());
			activityHome.setLinkId(facilities.getFacilities().get(origRandomFacilityId).getLinkId());
			activityHome.setEndTime(resultSet.getInt("mock_timestamp"));
			Leg leg = pf.createLeg(PopUtils.Mode(1));
			Activity activityTarget = pf.createActivityFromActivityFacilityId(activityTargetString,
					Id.create(destRandomFacilityId, ActivityFacility.class));
			activityTarget.setCoord(facilities.getFacilities().get(destRandomFacilityId).getCoord());
			activityTarget.setLinkId(facilities.getFacilities().get(destRandomFacilityId).getLinkId());
			plan.addActivity(activityHome);
			plan.addLeg(leg);
			plan.addActivity(activityTarget);
			person.addPlan(plan);
			population.addPerson(person);
			if (i % 10000 == 0)
			{
				log.info("read line #" + i + " from external_agents table");

			}
			i++;
		}
		return population;

	}

}
