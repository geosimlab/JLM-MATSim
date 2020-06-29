package jerusalem.scenario.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.OpeningTimeImpl;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.HouseholdUtils;
import org.matsim.households.Households;
import org.matsim.households.HouseholdsFactory;
import org.matsim.households.HouseholdsImpl;
import org.matsim.households.Income;
import org.matsim.households.IncomeImpl;

import jerusalem.scenario.DbInitialize;
import jerusalem.scenario.DbUtils;
import jerusalem.scenario.network.CreateNetwork;
import jerusalem.scenario.population.PopUtils;

public class HouseholdPlayground {
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private static final Properties props = DbUtils.readProperties("database.properties");
	private final static String HOUSEHOLDS_ID = "" + 1;
	private final static String FACILITIES_ID = "" + 1;
	private final static String POPULATION_ID = "" + 4;

	public static void main(String[] args) throws SQLException {
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
//		Population population = createPersons(scenario);
//		Households households = createHouseholds(scenario);
		Map<String, Map<String, ActivityFacilities>> tazFacilities = createFacilities(scenario);
//		ArrayList<Object> temp = addFacilitiesToHouseholds(tazFacilities, households);
//		tazFacilities = (Map<String, Map<String, ActivityFacilities>>) temp.get(0);
//		households = (Households) temp.get(1);
		ActivityFacilities facilities = uniteFacilites(tazFacilities);
//		population = addHomeToPopulation(households, population);
		new FacilitiesWriter(facilities)
				.write(props.getProperty("folder.output_folder") + FACILITIES_ID + ".facilities.xml.gz");
//		new HouseholdsWriterV10(households)
//				.writeFile(props.getProperty("folder.output_folder") + HOUSEHOLDS_ID + ".households.xml.gz");
//		new PopulationWriter(population)
//				.write(props.getProperty("folder.output_folder") + POPULATION_ID + ".population.xml.gz");

	}

//	FIXME create function that connects to database and executes query
	public static Population createPersons(Scenario scenario) throws SQLException {
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM persons;");
		ResultSet resultSet = pst.executeQuery();
		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		log.info("Reading persons");
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
		}
		return population;
	}

	public static Households createHouseholds(Scenario scenario) throws SQLException {
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("SELECT * FROM households;");
		ResultSet resultSet = pst.executeQuery();
		HouseholdsImpl households = (HouseholdsImpl) scenario.getHouseholds();
		HouseholdsFactory householdsFactory = households.getFactory();
		log.info("Reading households");
		while (resultSet.next()) {
//			initializing household
			Id<Household> householdId = Id.create(resultSet.getString("hhid"), Household.class);
			HouseholdImpl household = (HouseholdImpl) householdsFactory.createHousehold(householdId);
//			setting income
			IncomeImpl income = new IncomeImpl(resultSet.getInt("hhincomedollars"), Income.IncomePeriod.month);
			household.setIncome(income);
//			setting homeTAZ
			HouseholdUtils.putHouseholdAttribute(household, "HomeTAZ", "" + resultSet.getInt("hometaz"));
//			setting members
			List<Id<Person>> memberIds = (List<Id<Person>>) new ArrayList<Id<Person>>();
			for (int i = 0; i < resultSet.getInt("hhsize"); i++) {
				Id<Person> personId = Id.create(resultSet.getString("hhid") + "-" + i, Person.class);
				memberIds.add(personId);
			}
			household.setMemberIds(memberIds);
//			TODO setting vehicles
			households.addHousehold(household);
		}
		return households;
	}

	public static Map<String, Map<String, ActivityFacilities>> createFacilities(Scenario scenario) throws SQLException {
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		PreparedStatement pst = con.prepareStatement("select taz from inner_taz;");
		ResultSet resultSet = pst.executeQuery();
		Map<String, Map<String, ActivityFacilities>> tazMap = new TreeMap<String, Map<String, ActivityFacilities>>();
		log.info("Reading taz numbers, creating containers");
		while (resultSet.next()) {
			Map<String, ActivityFacilities> fullMap = new TreeMap<String, ActivityFacilities>();
			ActivityFacilities af = FacilitiesUtils.createActivityFacilities();
			fullMap.put("empty", af);
			ActivityFacilities bf = FacilitiesUtils.createActivityFacilities();
			fullMap.put("full", bf);
			ActivityFacilities cf = FacilitiesUtils.createActivityFacilities();
			fullMap.put("amenities", cf);

			tazMap.put("" + resultSet.getInt("taz"), fullMap);
		}
//		TODO remove fake nodes from this query, if not double facilities will be created  
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
			activityFacility.getAttributes().putAttribute("TAZ", "" + resultSet.getInt("hometaz"));
			activityFacility.getAttributes().putAttribute("housing_units", housingUnits);
			activityFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits);
			if (housingUnits == occupiedHousingUnits) {
				tazMap.get(activityFacility.getAttributes().getAttribute("TAZ")).get("full")
						.addActivityFacility(activityFacility);
			} else {
				tazMap.get(activityFacility.getAttributes().getAttribute("TAZ")).get("empty")
						.addActivityFacility(activityFacility);
			}

			if (i % 10000 == 0) {
				log.info("read line #" + i + " from bental_households table");
			}
			i++;
		}
//		TODO amenities doesn't include a methodology to decide on opening and closing times. should add it.
		pst = con.prepareStatement("select * from amenities;");
		resultSet = pst.executeQuery();
		log.info("Reading amenities");
		i = 0;
		while (resultSet.next()) {
			String amen_uniq_id = "" + resultSet.getInt("uniq_id");
			Id<ActivityFacility> facilityId = Id.create(amen_uniq_id, ActivityFacility.class);

			String taz = "" + resultSet.getInt("taz");
			ActivityFacilityImpl activityFacility = null;
			Map<Id<ActivityFacility>, ActivityFacility> test1 = (Map<Id<ActivityFacility>, ActivityFacility>) tazMap
					.get(taz).get("amenities").getFacilities();
			ActivityFacilityImpl test = (ActivityFacilityImpl) tazMap.get(taz).get("amenities").getFacilities()
					.get(amen_uniq_id);
			boolean bldg_exists = tazMap.get(taz).get("amenities").getFacilities().containsKey(facilityId);
			if (!bldg_exists) {
				Coord facilityCoord = new Coord(resultSet.getDouble("x"), resultSet.getDouble("y"));
				activityFacility = (ActivityFacilityImpl) aff.createActivityFacility(facilityId, facilityCoord);
				tazMap.get(taz).get("amenities").addActivityFacility(activityFacility);
			} else {
				activityFacility = (ActivityFacilityImpl) tazMap.get(taz).get("amenities").getFacilities()
						.get(facilityId);
			}
			double start_time = (resultSet.getDouble("opening_time") + 180) * 60;
			double end_time = (resultSet.getDouble("closing_time") + 180) * 60;
//			TODO due to major error in JTMT trips data, we use this condition to bypass error
			if (end_time < start_time) {
				start_time = 0;
				end_time = 30 * 3600;
			}
			OpeningTimeImpl openingTime = new OpeningTimeImpl(start_time, end_time);
			activityFacility.createAndAddActivityOption(resultSet.getString("matsim_activity"))
					.addOpeningTime(openingTime);
			activityFacility.getAttributes().putAttribute("TAZ", taz);
			if (i % 10000 == 0) {
				log.info("read line #" + i + " from amenities table");
			}
			i++;
		}
		return tazMap;

	}

	public static ArrayList<Object> addFacilitiesToHouseholds(
			Map<String, Map<String, ActivityFacilities>> tazFacilities, Households households) {
		log.info("Adding facilities to households");
		for (Id<Household> householdId : households.getHouseholds().keySet()) {
			Household household = households.getHouseholds().get(householdId);
			Map<String, ActivityFacilities> allTazFacilities = tazFacilities
					.get(household.getAttributes().getAttribute("HomeTAZ"));
			Map<Id<ActivityFacility>, ActivityFacility> facilitiesWithRoom = (Map<Id<ActivityFacility>, ActivityFacility>) allTazFacilities
					.get("empty").getFacilities();
			Map<Id<ActivityFacility>, ActivityFacility> facilitiesWithoutRoom = (Map<Id<ActivityFacility>, ActivityFacility>) allTazFacilities
					.get("full").getFacilities();
			Random generator = new Random();
			Object[] values = facilitiesWithRoom.values().toArray();
			ActivityFacilityImpl randomFacility = (ActivityFacilityImpl) values[generator.nextInt(values.length)];
			int housingUnits = (int) randomFacility.getAttributes().getAttribute("housing_units");
			int occupiedHousingUnits = (int) randomFacility.getAttributes().getAttribute("occupiedHousingUnits");
			household.getAttributes().putAttribute("homeFacilityRefID", randomFacility.getId().toString());
			Coord coord = randomFacility.getCoord();
			household.getAttributes().putAttribute("x", coord.getX());
			household.getAttributes().putAttribute("y", coord.getY());
			randomFacility.getAttributes().putAttribute("occupiedHousingUnits", occupiedHousingUnits + 1);
			if (housingUnits == occupiedHousingUnits + 1) {
				facilitiesWithoutRoom.put(randomFacility.getId(), randomFacility);
				facilitiesWithRoom.remove(randomFacility.getId());
			}

		}

		ArrayList<Object> result = new ArrayList<Object>();
		result.add(tazFacilities);
		result.add(households);
		return result;
	}

	public static ActivityFacilities uniteFacilites(Map<String, Map<String, ActivityFacilities>> tazFacilities) {
		log.info("Uniting facilities");
		ActivityFacilities af = FacilitiesUtils.createActivityFacilities();
		for (Map.Entry<String, Map<String, ActivityFacilities>> taz : tazFacilities.entrySet()) {
			for (Map.Entry<String, ActivityFacilities> container : taz.getValue().entrySet()) {
				for (Entry<Id<ActivityFacility>, ? extends ActivityFacility> facility : container.getValue()
						.getFacilities().entrySet()) {
					af.addActivityFacility(facility.getValue());
				}
			}

		}
		return af;

	}

	public static Population addHomeToPopulation(Households households, Population population) {
		log.info("Adding home attribute to population");
		for (Id<Person> personId : population.getPersons().keySet()) {
			Person person = population.getPersons().get(personId);
			Id<Household> housholdId = Id.create(person.getAttributes().getAttribute("hhid").toString(),
					Household.class);
			Household houshold = households.getHouseholds().get(housholdId);
			String homeFacilityRefID = (String) houshold.getAttributes().getAttribute("homeFacilityRefID");
			person.getAttributes().putAttribute("homeFacilityRefID", homeFacilityRefID);

		}
		return population;

	}

	public static Population addPlansToPopulation(Population population,
			Map<String, Map<String, ActivityFacilities>> tazFacilities) throws SQLException {
		log.info("Adding plans to population");
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
//		TODO trips has major problems. just notice they are repaired
		PreparedStatement pst = con.prepareStatement("select * from trips;");
		ResultSet resultSet = pst.executeQuery();
		PopulationFactory populationFactory = population.getFactory();
		Person person = null;
		Plan plan = populationFactory.createPlan();
		Activity activity = null;

		while (resultSet.next()) {
//			if home, go to person attributes
			if (PopUtils.ActivityType(resultSet.getInt("origPurp")) == "home") {
				Id<Person> personId = Id.create(resultSet.getInt("hhid") + "-" + resultSet.getInt("pnum"),
						Person.class);
				String homeFacilityRefID = (String) population.getPersons().get(personId).getAttributes()
						.getAttribute("homeFacilityRefID");
				Id<ActivityFacility> facilityId = Id.create(homeFacilityRefID, ActivityFacility.class);
				activity = populationFactory.createActivityFromActivityFacilityId("home", facilityId);
			}
//			get random facility from facilties, that relates to person's taz
			else {
				Map<Id<ActivityFacility>, ActivityFacility> tazAmenities = (Map<Id<ActivityFacility>, ActivityFacility>) tazFacilities
						.get(resultSet.getInt("origtaz")).get("amenities").getFacilities();
				Random generator = new Random();
				Object[] values = tazAmenities.values().toArray();
//				this is wrong. you should specify the right activity for the facility, maybe use facilitiesacttypefilter
				ActivityFacilityImpl randomFacility = (ActivityFacilityImpl) values[generator.nextInt(values.length)];
				Id<ActivityFacility> facilityId = randomFacility.getId();
				activity = populationFactory.createActivityFromActivityFacilityId(
						PopUtils.ActivityType(resultSet.getInt("origpurp")), facilityId);
			}
			plan.addActivity(activity);

//			checking if person ended or that table ended
			if (resultSet.getInt("personTripNum") == 0 | !resultSet.next()) {
//				TODO create home activity and add to plan
				Id<Person> personId = Id.create(resultSet.getInt("hhid") + "-" + resultSet.getInt("pnum"),
						Person.class);
				person = population.getPersons().get(personId);
				person.addPlan(plan);
				population.addPerson(person);
				plan = populationFactory.createPlan();
			}
			resultSet.previous();
		}
//		scan through population. if person has no plan, create a plan where person stays at home
		return population;
	}

}
