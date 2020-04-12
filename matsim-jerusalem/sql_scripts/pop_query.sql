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
	   tzh.X AS homeX,
	   tzh.Y AS homeY,
	   tzo.X AS origX,
	   tzo.Y AS origY,
	   tzd.X AS destX,
	   tzd.Y AS destY,
	   MAX(t.personTripNum) OVER (PARTITION BY t.hhid,t.pnum) AS lastTripNum 
	   FROM households AS h
	   LEFT JOIN persons AS p
	   ON h.hhid = p.hhid
	   LEFT JOIN trips as t
	   ON p.hhid = t.hhid AND p.pnum = t.pnum
	   LEFT JOIN TAZ_coordinates as tzh 
	   ON h.homeTaz = tzh.taz
	   LEFT JOIN TAZ_coordinates as tzo 
	   ON t.origTaz = tzo.taz 
	   LEFT JOIN TAZ_coordinates as tzd 
	   ON t.destTaz = tzd.taz 
	   ORDER BY h.hhid,p.pnum,t.personTripNum;