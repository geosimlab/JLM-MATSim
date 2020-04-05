SELECT t.hhid,
	   t.pnum,
	   t.personTripNum,
	   t.origTaz,
	   t.destTaz,
	   t.origPurp,
	   t.destPurp,
	   t.finalDepartMinute,
	   t.modeCode,
	   tzo.X AS origX,
	   tzo.Y AS origY,
	   tzd.X AS destX,
	   tzd.Y AS destY,
	   MAX(t.personTripNum) OVER (PARTITION BY t.hhid,t.pnum) AS lastTripNum 
	   FROM trips AS t 
	   LEFT JOIN TAZ_coordinates as tzo 
	   ON t.origTaz = tzo.taz 
	   LEFT JOIN TAZ_coordinates as tzd 
	   ON t.destTaz = tzd.taz 
	   ORDER BY t.hhid,t.pnum,t.personTripNum;