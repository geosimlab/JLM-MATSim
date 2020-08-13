CREATE EXTENSION IF NOT EXISTS postgis;
SET search_path TO "$user", postgis, topology, public;
DROP TABLE IF EXISTS trips, taz_centroid, persons,households, 
households_final,taz600,bldg,poi_bldg ,inner_taz,bldg_cent,
bental_households,bental_jtmt_code_conversion,nodes,links,line_path,lines,headway,headway_periods,
vehicle_types,households_in_polygon,households_final,trips_final,stops,pt_routes,amenities,readable_headway,jtmt_matsim_code_conversion,detailed_headway,counts CASCADE;
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
  modelFailureId INT NOT NULL CHECK (modelFailureId >= 0),
  primary key (hhid)
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
  activityString VARCHAR(100) NOT null,
  primary key (hhid,pnum),
  foreign key (hhid) references households(hhid)
);
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
	weight REAL NOT null,
	primary key (hhid,pnum,personTripNum),
	foreign key (hhid) references households(hhid),
	foreign key (hhid,pnum) references persons(hhid,pnum)
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
    purp_8 integer,
    primary key (usg_group,usg_code)
);

CREATE TABLE IF NOT EXISTS jtmt_matsim_code_conversion (
    jtmt_code integer NOT NULL,
    jtmt_activity VARCHAR(50) NOT NULL,
	matsim_activity VARCHAR(20) NOT null,
	primary key (jtmt_code)
);

CREATE TABLE IF NOT EXISTS nodes(
    i integer NOT NULL,
    is_centroid varchar(6) NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL,
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
    s0link_m_per_s double precision,
    primary key (i,j),
    foreign key (i) references nodes(i),
    foreign key (j) references nodes(i)
);

CREATE TABLE IF NOT EXISTS vehicle_types(
    vehicle_code integer NOT NULL,
    name varchar(15) NOT NULL,
    TAU_name varchar(100) NOT NULL,
    auto_equ double precision NOT NULL,
    capsitting integer,
    captotal integer,
    vehicle_type varchar(100) NOT NULL,
    board_coef double precision NOT NULL,
    disembark_coef double precision NOT null,
    primary key (vehicle_code)
);

CREATE TABLE IF NOT EXISTS lines(
    lines varchar(7) NOT NULL,
    description varchar(100) NOT NULL,
    transport_mode varchar(2) NOT NULL,
    vehicle integer NOT null,
    primary key (lines),
    foreign key (vehicle) references vehicle_types(vehicle_code) 
);

CREATE TABLE IF NOT EXISTS line_path(
    line varchar(7) NOT NULL,
    i integer NOT NULL,
    j integer NOT NULL,
    length_met double precision NOT NULL,
    seq_number integer NOT NULL,
    stop integer NOT null,
    primary key (line,seq_number),
    foreign key (line) references lines(lines),
    foreign key (i,j) references links(i,j)
);

CREATE TABLE IF NOT EXISTS headway(
    line varchar(7) NOT NULL,
    first_period double precision NOT NULL,
    second_period double precision NOT NULL,
    third_period double precision NOT NULL,
    fourth_period double precision NOT NULL,
    fifth_period double precision NOT NULL,
    sixth_period double precision NOT NULL,
    seventh_period double precision NOT NULL,
    eighth_period double precision NOT NULL,
    ninth_period double precision NOT NULL,
    tenth_period double precision NOT null,
    primary key (line),
    foreign key (line) references lines(lines)
);

CREATE TABLE IF NOT EXISTS detailed_headway(
    line varchar(7) NOT NULL,
    mode varchar(1) NOT NULL,
    headway double precision NOT NULL,
    direction integer NOT null,
    LineWoDirection varchar(7) NOT NULL,
    HeadwayDir1 double precision,
	HeadwayDir2 double precision,
	HeadwayDir3 double precision,
    first_period double precision NOT NULL,
    second_period double precision NOT NULL,
    third_period double precision NOT NULL,
    fourth_period double precision NOT NULL,
    fifth_period double precision NOT NULL,
    sixth_period double precision NOT NULL,
    seventh_period double precision NOT NULL,
    eighth_period double precision NOT NULL,
    ninth_period double precision NOT NULL,
    tenth_period double precision NOT null,
    primary key (line),
    foreign key (line) references lines(lines)
);


CREATE TABLE IF NOT EXISTS headway_periods(
	headway_period varchar(20) not null,
	start_time varchar(20) not null,
	end_time varchar(20) not null,
	primary key (headway_period)
);
--I deleted "type" column from the original csv, in order to prevent parsing errors
create table if not exists counts(
	linkID integer not null,
	CID integer not null, 
	A integer not null,
	B integer not null,
	COUNTDATE date not null,
	factor double precision not null,
	AB0600 double precision NOT NULL,
	BA0600 double precision NOT NULL,
	AB0700 double precision NOT NULL,
	BA0700 double precision NOT NULL,
	AB0800 double precision NOT NULL,
	BA0800 double precision NOT NULL,
	AB0900 double precision NOT NULL,
	BA0900 double precision NOT NULL,
	AB1000 double precision NOT NULL,
	BA1000 double precision NOT NULL,
	AB1100 double precision NOT NULL,
	BA1100 double precision NOT NULL,
	AB1200 double precision NOT NULL,
	BA1200 double precision NOT NULL,
	AB1300 double precision NOT NULL,
	BA1300 double precision NOT NULL,
	AB1400 double precision NOT NULL,
	BA1400 double precision NOT NULL,
	AB1500 double precision NOT NULL,
	BA1500 double precision NOT NULL,
	AB1600 double precision NOT NULL,
	BA1600 double precision NOT NULL,
	AB1700 double precision NOT NULL,
	BA1700 double precision NOT NULL,
	AB1800 double precision NOT NULL,
	BA1800 double precision NOT NULL
);

