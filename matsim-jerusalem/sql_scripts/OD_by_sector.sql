with super_zones_trips as(
select hhid,ceiling((t.origtaz/1000.0)::float) origsuper,ceiling(t.desttaz/1000.0) destsuper 
from trips t
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
finala as(
select h.sector, s.origsuper,s.destsuper, count(*)
from super_zones_trips s
left join households h  using(hhid)
where s.origsuper < 8 and s.destsuper < 8
group by s.origsuper,s.destsuper,h.sector
)
select f.*,sza.cent origcent,  szb.cent destcent,st_makeline(sza.cent, szb.cent) aline
from finala f
left join super_zones_taz_v2 sza on f.origsuper = sza.tazsuper
left join super_zones_taz_v2 szb on f.destsuper = szb.tazsuper;


select h.sector,1000 + 1000*mod(t.origtaz,1000) origsuper,
1000 + 1000*mod(t.desttaz,1000) destsuper,count(*) from trips t 
left join households h  using(hhid)
group by t.origtaz,t.desttaz,h.sector