SELECT DISTINCT h.hhid,
	   p.pnum,
	   t.personTripNum,
	   t.origTaz,
	   h.homeTaz,
	   t.destTaz,
	   t.origPurp,
	   t.destPurp,
	   t.finalDepartMinute,
	   t.modeCode,
	   p.age,
	   p.gender,
	   p.persTypeDetailed,
	   p.driverLicense,
	   h.sector,
	   p.usualDriver,
	   h.numAuto,
	   h.x AS homeX,
	   h.y AS homeY,
	   tzo.X AS origX,
	   tzo.Y AS origY,
	   tzd.X AS destX,
	   tzd.Y AS destY,
	   MAX(t.personTripNum) OVER (PARTITION BY t.hhid,t.pnum) AS lastTripNum 
	   FROM households_final AS h
	   LEFT JOIN persons AS p
	   ON h.hhid = p.hhid
	   LEFT JOIN trips_final as t
	   ON p.hhid = t.hhid AND p.pnum = t.pnum
	   LEFT JOIN taz_centroid as tzh 
	   ON h.homeTaz = tzh.taz
	   LEFT JOIN taz_centroid as tzo 
	   ON t.origTaz = tzo.taz 
	   LEFT JOIN taz_centroid as tzd 
	   ON t.destTaz = tzd.taz 
	   ORDER BY h.hhid,p.pnum,t.personTripNum;