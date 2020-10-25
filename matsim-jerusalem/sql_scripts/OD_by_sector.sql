with super_zones_trips_v1 as(
select sector,ceiling((t.origtaz/1000.0)::float) origsuper,ceiling(t.desttaz/1000.0) destsuper, 
finaldepartminute > 270.0 AND finaldepartminute < 330.0 AS morning_pick,
finaldepartminute > 990.0 AND finaldepartminute < 1050.0 AS afternoon_pick
from trips t
left join households h  using(hhid)
),
super_zones_trips_v2 as(
select sector,origsuper,destsuper,CASE WHEN morning_pick THEN 'morning' WHEN not morning_pick THEN (CASE WHEN afternoon_pick THEN 'afternoon' WHEN not afternoon_pick THEN 'no' END) END AS pick from super_zones_trips_v1 
),
super_zones_taz_v1 as(
select ceiling((taz/1000.0)::float) tazsuper,geometry 
from taz600
),
super_zones_taz_v2 as(
select tazsuper,st_union(geometry) geom, st_centroid(st_union(geometry)) cent
from super_zones_taz_v1 
group by tazsuper
),
mock as(
SELECT a.n origsuper, b.n destsuper,c.n sector,case when d.n = 1 then 'morning' else 'afternoon' end pick
from generate_series(1, 7) as a(n),generate_series(1, 7) as b(n),generate_series(1, 3) as c(n),generate_series(1, 2) as d(n)
),
finala as(
select origsuper,destsuper,sector,pick, count(*)
from super_zones_trips_v2
where pick != 'no' and origsuper < 8 and destsuper < 8 
group by origsuper,destsuper,sector,pick
),
finalb as(
select mock.*,coalesce(finala.count,0) count from mock left join finala using(origsuper,destsuper,sector,pick)
)
select f.*,sza.cent origcent,  szb.cent destcent,st_makeline(sza.cent, szb.cent) aline
from finalb f
left join super_zones_taz_v2 sza on f.origsuper = sza.tazsuper
left join super_zones_taz_v2 szb on f.destsuper = szb.tazsuper;