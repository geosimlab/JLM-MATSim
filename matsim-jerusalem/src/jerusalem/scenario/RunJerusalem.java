package jerusalem.scenario;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup.EventsFileFormat;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;

import jerusalem.scenario.network.CreateNetwork;
import jerusalem.scenario.test.HouseholdPlayground;

/**
 * @author Golan Ben-Dor
 */
public class RunJerusalem {
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	final public static Properties props = DbUtils.readProperties("database.properties");
	final public static String OUTPUT_FOLDER = props.getProperty("folder.output_folder");
	final public static String RUN_ID = "" + 8;

	public static void main(String[] args) {
		// create a new MATSim config for JLM
//		log.getRootLogger().setLevel(Level.DEBUG);
		Config config = createJeruslaemConfig();

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controler = new Controler(scenario);

		controler.run();
	}

	/**
	 * Create a MATSim Config file
	 * 
	 * @return MATSim Config
	 */
	/**
	 * @return
	 */
	public static Config createJeruslaemConfig() {
		Config config = ConfigUtils.createConfig();

		config.network().setInputFile(CreateNetwork.NETWORK_OUTPUT_PATH);
		config.plans().setInputFile(HouseholdPlayground.POPULATION_OUTPUT_PATH);
		config.facilities().setInputFile(HouseholdPlayground.FACILITIES_OUTPUT_PATH);
		config.households().setInputFile(HouseholdPlayground.HOUSEHOLDS_OUTPUT_PATH);
		config.vehicles().setVehiclesFile(HouseholdPlayground.FAMILY_VEHICLES_OUTPUT_PATH);

		// modify controler
		config.controler().setWriteEventsInterval(1);
		config.controler().setWritePlansInterval(1);
		config.controler().setEventsFileFormats(EnumSet.of(EventsFileFormat.xml));
		config.controler().setOutputDirectory(OUTPUT_FOLDER + RUN_ID + "/");
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.controler().setFirstIteration(1);
		config.controler().setLastIteration(1);
		config.controler().setMobsim("qsim");
		config.controler().setRoutingAlgorithmType(RoutingAlgorithmType.FastAStarLandmarks);
		config.controler().setRunId(RUN_ID);

		// modify Qsim
		config.qsim().setStartTime(0.0);
		config.qsim().setEndTime(30 * 3600);
		config.qsim().setFlowCapFactor(0.3);
		config.qsim().setStorageCapFactor(Math.pow(0.3, 0.75));
		config.qsim().setNumberOfThreads(4);
		config.qsim().setSnapshotPeriod(1);
		config.qsim().setRemoveStuckVehicles(false);
		config.qsim().setStuckTime(3600);
		config.qsim().setTimeStepSize(1);
		config.qsim().setTrafficDynamics(TrafficDynamics.queue);
		config.qsim().setMainModes(Arrays.asList(TransportMode.car));
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);

		// modify global
		config.global().setCoordinateSystem("EPSG:2039");
		config.global().setNumberOfThreads(8);
		// ?? random seed?<param name="randomSeed" value="4711" />

		// Add sub-tour mode choice
		config.subtourModeChoice()
				.setModes(new String[] { TransportMode.car, TransportMode.pt, TransportMode.walk, TransportMode.bike });
		// TODO check that bike is biycle
		config.subtourModeChoice().setChainBasedModes(new String[] { TransportMode.car });

		// Add sub-tour mode choice
		config.timeAllocationMutator().setMutationRange(3600);

		// Add strategy
		config.strategy().setMaxAgentPlanMemorySize(5);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);

		// Add strategy - plan selector
		StrategySettings changeExpStrategy = new StrategySettings();
		changeExpStrategy.setDisableAfter(-1);
		changeExpStrategy.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		changeExpStrategy.setWeight(0.8);
		config.strategy().addStrategySettings(changeExpStrategy);

		// Add strategy - time-mutation
//		StrategySettings timeMutatorStrategy = new StrategySettings();
//		timeMutatorStrategy
//				.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator.toString());
//		timeMutatorStrategy.setWeight(0.1);
//		config.strategy().addStrategySettings(timeMutatorStrategy);

		// Add strategy - re-route
		StrategySettings reRouteStrategy = new StrategySettings();
		reRouteStrategy.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute.toString());
		reRouteStrategy.setWeight(0.1);
		config.strategy().addStrategySettings(reRouteStrategy);

		// Add strategy - Sub-tour strategy
//		StrategySettings subTourModeChoiceStrategy = new StrategySettings();
//		subTourModeChoiceStrategy
//				.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice.toString());
//		subTourModeChoiceStrategy.setWeight(0.1);
//		config.strategy().addStrategySettings(subTourModeChoiceStrategy);

		// add car Availability after adding attributes to popualtion
		// config.subtourModeChoice().setConsiderCarAvailability(true);

		// add network modes which are simulated on network in future add more modes
		// config.plansCalcRoute().setNetworkModes(Arrays.asList(TransportMode.car));
		// config.plansCalcRoute().setInsertingAccessEgressWalk(true);

		// // just a place hodler
		// ModeRoutingParams taxiModeRoute = new ModeRoutingParams();
		// taxiModeRoute.setMode(TransportMode.taxi);
		// taxiModeRoute.setTeleportedModeSpeed(100.0);
		// config.plansCalcRoute().addModeRoutingParams(taxiModeRoute);

		// global scoring values taken from TLVM model = SF 14
		config.planCalcScore().setEarlyDeparture_utils_hr(0.0);
		config.planCalcScore().setLateArrival_utils_hr(0);
		config.planCalcScore().setMarginalUtilityOfMoney(0.062);
		config.planCalcScore().setPerforming_utils_hr(0.96);
		config.planCalcScore().setUtilityOfLineSwitch(0);
		config.planCalcScore().setMarginalUtlOfWaitingPt_utils_hr(-0.18);

		// car scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams carCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.car);
		carCalcScoreParams.setConstant(-0.562);
		carCalcScoreParams.setMode("car");
		carCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		config.planCalcScore().addModeParams(carCalcScoreParams);

		// PT scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams ptCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.pt);
		ptCalcScoreParams.setConstant(-0.124);
		ptCalcScoreParams.setMode("pt");
		ptCalcScoreParams.setMarginalUtilityOfTraveling(-0.18);
		config.planCalcScore().addModeParams(ptCalcScoreParams);

		// Walk scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams walkCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.walk);
		walkCalcScoreParams.setMode("walk");
		walkCalcScoreParams.setMarginalUtilityOfTraveling(-1.14);
		config.planCalcScore().addModeParams(walkCalcScoreParams);

		// TODO get values for Taxi scoring
		// Taxi scoring functions place holder (taken from car)
		// PlanCalcScoreConfigGroup.ModeParams TaxiCalcScoreParams = new
		// PlanCalcScoreConfigGroup.ModeParams(TransportMode.taxi);
		// TaxiCalcScoreParams.setConstant(-0.562);
		// TaxiCalcScoreParams.setMode("taxi");
		// TaxiCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		// config.planCalcScore().addModeParams(walkCalcScoreParams);

		// TODO add ride as network mode remove from modechoice
		// Ride scoring functions place holder taken from Berlin MATSim model -
		// monetaryDistanceRate same as car -0.0004
		PlanCalcScoreConfigGroup.ModeParams rideCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.ride);
		rideCalcScoreParams.setMode("ride");
		rideCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		config.planCalcScore().addModeParams(rideCalcScoreParams);

		// TODO check with JLM bike - bicyle
		// bike scoring functions place holder taken from Berlin MATSim model of bicyke
		PlanCalcScoreConfigGroup.ModeParams bikeCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.bike);
		bikeCalcScoreParams.setConstant(-1.9);
		bikeCalcScoreParams.setMode("bike");
		config.planCalcScore().addModeParams(bikeCalcScoreParams);

		// TODO get activities open hours
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(16 * 60 * 60);
		config.planCalcScore().addActivityParams(home);

		ActivityParams work = new ActivityParams("work");
		work.setOpeningTime(6 * 3600);
		work.setClosingTime(20 * 3600);
		work.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(work);

		ActivityParams school = new ActivityParams("school");
		school.setOpeningTime(8 * 3600);
		school.setClosingTime(14 * 3600);
		school.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(school);

		ActivityParams leisure = new ActivityParams("leisure");
		leisure.setOpeningTime(9 * 3600);
		leisure.setClosingTime(24 * 3600);
		leisure.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(leisure);

		ActivityParams other = new ActivityParams("other");
		other.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(other);

		return config;

	}
}
