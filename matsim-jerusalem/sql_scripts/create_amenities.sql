select * into amenities from (with poi_blds_coverted_taz as (
	select uniq_id,poi_bldg.usg_group,poi_bldg.usg_code, 
usg_sp_name,bldg_id,e_ord x,n_ord y,fake,
taz600.taz,
jtmt_matsim_code_conversion.MATSim_activity
from poi_bldg 
left join bental_jtmt_code_conversion_long 
on poi_bldg.usg_group = bental_jtmt_code_conversion_long.usg_group 
and poi_bldg.usg_code = bental_jtmt_code_conversion_long.usg_code
left join jtmt_matsim_code_conversion
on jtmt_matsim_code_conversion.jtmt_code = bental_jtmt_code_conversion_long.value1
left join taz600 
on st_intersects(poi_bldg.geometry,taz600.geometry)
where poi_bldg.usg_code != 7600 and (taz < 9000 or fake)),
taz_opening as(
select desttaz,MATSim_activity, min(trips.finalarriveminute) opening_time_dest
from trips 
left join jtmt_matsim_code_conversion
on jtmt_matsim_code_conversion.jtmt_code = trips.destpurp
group by desttaz,MATSim_activity
order by desttaz,MATSim_activity
	
),
taz_closing as(
select origtaz,MATSim_activity, max(finaldepartminute) closing_time_orig
from trips 
left join jtmt_matsim_code_conversion
on jtmt_matsim_code_conversion.jtmt_code = trips.origpurp
group by origtaz,MATSim_activity
order by origtaz,MATSim_activity
)
select distinct poi_blds_coverted_taz.*, 
COALESCE(taz_opening.opening_time_dest,-3*60) opening_time,
COALESCE(taz_closing.closing_time_orig,30*60 - 3*60) closing_time
from poi_blds_coverted_taz
left join taz_opening 
on taz_opening.desttaz = poi_blds_coverted_taz.taz
and taz_opening.MATSim_activity = poi_blds_coverted_taz.MATSim_activity
left join taz_closing 
on taz_closing.origtaz = poi_blds_coverted_taz.taz
and taz_closing.MATSim_activity = poi_blds_coverted_taz.MATSim_activity)q;