CREATE EXTENSION IF NOT EXISTS postgis;
SET search_path TO "$user", postgis, topology, public;
DROP TABLE IF EXISTS trips, taz_centroid, persons,households, 
households_final,taz600,bldg,poi_bldg ,inner_taz,bldg_cent,
bental_households,bental_jtmt_code_conversion,nodes,links,line_path,lines,headway,vehicle_types,
households_in_polygon,households_final,trips_final,stops;
CREATE TABLE IF NOT EXISTS trips(
	uniqueid INT NOT NULL CHECK (uniqueid > 0),
	jointTripNum INT NOT NULL CHECK (jointTripNum  >= 0),
	hhid INT NOT NULL CHECK (hhid >= 0),
	pnum INT NOT NULL CHECK (pnum > 0),
	userClass INT NOT NULL CHECK (userClass > 0),
	tourNum INT NOT NULL CHECK (tourNum > 0),
	tourSubTourNum INT NOT NULL CHECK (tourSubTourNum >= 0),
	personTripNum INT NOT NULL CHECK (personTripNum > 0),
	tourTripNum INT NOT NULL CHECK (tourTripNum > 0),
	mainTourTripNum INT NOT NULL CHECK (mainTourTripNum >= 0),
	subTourTripNum INT NOT NULL CHECK (subTourTripNum >= 0),
	jointTourNum INT NOT NULL CHECK (jointTourNum >= 0),
	jointTripRole INT NOT NULL CHECK (jointTripRole >= 0),
	direction INT NOT NULL CHECK (direction > 0),
	obBundleNum INT NOT NULL CHECK (obBundleNum >= 0),
	ibBundleNum INT NOT NULL CHECK (ibBundleNum >= 0),
	escortingRole INT NOT NULL CHECK (escortingRole >= 0),
	jointSpecEvtTour INT NOT NULL CHECK (jointSpecEvtTour >= 0),
	party VARCHAR(100) NOT NULL,
	origTaz INT NOT NULL CHECK (origTaz > 0),
	origMaz INT NOT NULL CHECK (origMaz > 0),
	destTaz INT NOT NULL CHECK (destTaz > 0),
	destMaz INT NOT NULL CHECK (destMaz > 0),
	origPurp INT NOT NULL CHECK (origPurp >= 0),
	destPurp INT NOT NULL CHECK (destPurp >= 0),
	modeCode INT NOT NULL CHECK (modeCode > 0),
	prelimDepartInterval INT NOT NULL CHECK (prelimDepartInterval > 0),
	finalDepartMinute REAL NOT NULL CHECK (finalDepartMinute > 0),
	tripDistance REAL NOT NULL CHECK (tripDistance >= 0),
	prelimArriveInterval INT NOT NULL CHECK (prelimArriveInterval > 0),
	finalArriveMinute REAL NOT NULL CHECK (finalArriveMinute > 0),
	activityMinutesAtDest REAL NOT NULL,
	pnrParkTaz INT NOT NULL CHECK (pnrParkTaz >= 0),
	knrTaz INT NOT NULL CHECK (knrTaz >= 0),
	vot REAL NOT NULL,
	weight REAL NOT NULL
);
CREATE TABLE IF NOT EXISTS persons (
  hhid INT NOT NULL CHECK (hhid >= 0),
  pnum INT NOT NULL CHECK (pnum > 0),
  pumsSerialNo real,
  persType INT NOT NULL CHECK (persType > 0),
  persTypeDetailed INT NOT NULL CHECK (persTypeDetailed > 0),
  age INT NOT NULL CHECK (age >= 0),
  gender INT NOT NULL CHECK (gender = 1 OR gender = 2),
  industry INT NOT NULL CHECK (industry >= 0),
  workPlaceType INT NOT NULL CHECK (workPlaceType >= 0),
  employmentType INT NOT NULL CHECK (employmentType >= -1),
  numJobs INT NOT NULL CHECK (numJobs >= 0),
  commuteFreq INT NOT NULL CHECK (commuteFreq >= -1),
  usualSchedule INT NOT NULL CHECK (hhid >= 0),
  workFlexibility INT NOT NULL CHECK (workFlexibility >= -1),
  workPlaceTaz INT NOT NULL CHECK (workPlaceTaz >= 0),
  workPlaceMaz INT NOT NULL CHECK (workPlaceMaz >= 0),
  schoolType INT NOT NULL CHECK (schoolType >= -1),
  schoolTaz INT NOT NULL CHECK (schoolTaz >= 0),
  schoolMaz INT NOT NULL CHECK (schoolMaz >= 0),
  driverLicense INT NOT NULL CHECK (driverLicense  >= -1),
  usualDriver INT NOT NULL CHECK (usualDriver  >= 0),
  transitPass INT NOT NULL CHECK (transitPass  >= -1),
  empParkOption INT NOT NULL CHECK (empParkOption >= 0),
  dailyActivityPattern INT NOT NULL CHECK (dailyActivityPattern >= 1),
  workActivityFreqAlt INT NOT NULL CHECK (workActivityFreqAlt >= -1),
  studentActivityFreqAlt INT NOT NULL CHECK (studentActivityFreqAlt >= -1),
  specEvtParticipant INT NOT NULL CHECK (specEvtParticipant  >= 1),
  jointTour1Role INT NOT NULL CHECK (jointTour1Role >= 0),
  jointTour2role INT NOT NULL CHECK (jointTour2role  >= 0),
  obPeChauffBund1 INT NOT NULL CHECK (obPeChauffBund1  >= 0),
  obPeChauffBund2 INT NOT NULL CHECK (obPeChauffBund2 >= 0),
  obPeChauffBund3 INT NOT NULL CHECK (obPeChauffBund3 >= 0),
  obRsChauffBund INT NOT NULL CHECK (obRsChauffBund >= 0),
  obPePassBund INT NOT NULL CHECK (obPePassBund >= 0),
  obRsPassBund INT NOT NULL CHECK (obRsPassBund >= 0),
  ibPeChauffBund1 INT NOT NULL CHECK (ibPeChauffBund1 >= 0),
  ibPeChauffBund2 INT NOT NULL CHECK (ibPeChauffBund2 >= 0),
  ibPeChauffBund3 INT NOT NULL CHECK (ibPeChauffBund3 >= 0),
  ibRsChauffBund INT NOT NULL CHECK (ibRsChauffBund >= 0),
  ibPePassBund INT NOT NULL CHECK (ibPePassBund>= 0),
  ibRsPassBund INT NOT NULL CHECK (ibRsPassBund>= 0),
  studentDorm INT NOT NULL CHECK (studentDorm >= 0),
  studentRent INT NOT NULL CHECK (studentRent >= 0),
  activityString VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS households (
  hhid INT NOT NULL CHECK (hhid >= 0),
  pumsSerialNo real NOT NULL CHECK (pumsSerialNo >= 0),
  homeTaz INT NOT NULL CHECK (homeTaz > 0),
  homeMaz INT NOT NULL CHECK (homeMaz > 0),
  hhsize INT NOT NULL CHECK (hhsize > 0),
  sector INT NOT NULL CHECK (sector > 0),
  numFtWorkers INT NOT NULL CHECK (numFtWorkers >= 0),
  numPtWorkers INT NOT NULL CHECK (numPtWorkers >= 0),
  numUnivStuds INT NOT NULL CHECK (numUnivStuds >= 0),
  numNonWorkers INT NOT NULL CHECK (numNonWorkers >= 0),
  nunmRetired INT NOT NULL CHECK (nunmRetired >= 0),
  numDrivAgeStuds INT NOT NULL CHECK (numDrivAgeStuds >= 0),
  numPreDrivStuds INT NOT NULL CHECK (numPreDrivStuds >= 0),
  numPreshcool INT NOT NULL CHECK (numPreshcool >= 0),
  hhIncomeDollars INT NOT NULL CHECK (hhIncomeDollars >= 0),
  dwellingType INT NOT NULL CHECK (dwellingType >= 0),
  numAuto INT NOT NULL CHECK (numAuto >= 0),
  modelFailureId INT NOT NULL CHECK (modelFailureId >= 0)
);

CREATE TABLE IF NOT EXISTS bental_jtmt_code_conversion (
    usg_group integer NOT NULL,
    usg_code integer NOT NULL,
    count integer NOT NULL,
    purp_1 integer,
    purp_2 integer,
    purp_3 integer,
    purp_4 integer,
    purp_5 integer,
    purp_6 integer,
    purp_7 integer,
    purp_8 integer
);

CREATE TABLE IF NOT EXISTS nodes(
    i varchar(6) NOT NULL,
    is_centroid varchar(6) NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL,
    geometry geometry, 
    PRIMARY KEY (i)
);

CREATE TABLE IF NOT EXISTS links(
    i integer,
    j integer,
    length_met double precision,
    mode varchar(50),
    num_lanes double precision,
    type double precision,
    "@at" double precision,
    "@linkcap" double precision,
    s0link_m_per_s double precision
);

CREATE TABLE IF NOT EXISTS line_path(
    line varchar(7) NOT NULL,
    i integer NOT NULL,
    j integer NOT NULL,
    length_met double precision NOT NULL,
    seq_number integer NOT NULL,
    stop integer NOT NULL
);

CREATE TABLE IF NOT EXISTS lines(
    lines varchar(7) NOT NULL,
    description varchar(100) NOT NULL,
    transport_mode varchar(2) NOT NULL,
    vehicle integer NOT NULL
);

CREATE TABLE IF NOT EXISTS headway(
    line varchar(7) NOT NULL,
    AM_period1_one double precision NOT NULL,
    AM_period_two double precision NOT NULL,
    AM_period_three double precision NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle_types(
    vehicle_code integer NOT NULL,
    vehicle_type varchar(100) NOT NULL,
    board_coef double precision NOT NULL,
    disembark_coef double precision NOT NULL
);