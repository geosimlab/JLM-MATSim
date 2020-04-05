DROP TABLE IF EXISTS trips, TAZ_coordinates;
CREATE TABLE IF NOT EXISTS trips(
	uniqueid INT NOT NULL CHECK (uniqueid > 0),
	jointTripNum INT NOT NULL CHECK (uniqueid >= 0),
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
CREATE TABLE IF NOT EXISTS TAZ_coordinates(
	X real NOT NULL,
	Y real NOT NULL,
	taz INT NOT NULL CHECK (taz > 0)
);
 

 
 