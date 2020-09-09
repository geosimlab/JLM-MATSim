select hhid,pnum,persontripnum, origtaz , origpurp ,desttaz ,destpurp,modecode,concat(finaldepartminute+180,' minutes')::interval  
from trips 
where hhid = 37840 and pnum = 4;
select * from taz600 t2  where taz ='5002'
select * from taz600 t where taz = 5214;
select concat(finaldepartminute,' minutes')::interval from trips t2; 
select * from persons where hhid in (select hhid from households h2 where hometaz >1000);
select * from links where length_met < 150 and type not in (9,10,66);
with first_table as(
select distinct hhid,pnum from trips where origtaz < 1000
), second_table as(
select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips 
) select second_table.* 
from first_table
left join second_table
using (hhid,pnum);

CREATE TABLE IF NOT EXISTS counts_data AS 
with initial_long as (
select cid,a,b,countdate.
unnest(array['AB0600','BA0600','AB0700','BA0700','AB0800','BA0800','AB0900','BA0900','AB1000','BA1000','AB1100','BA1100','AB1200','BA1200','AB1300','BA1300','AB1400','BA1400','AB1500','BA1500','AB1600','BA1600','AB1700','BA1700','AB1800','BA1800']) as direction_hour,
unnest(array[AB0600,BA0600,AB0700,BA0700,AB0800,BA0800,AB0900,BA0900,AB1000,BA1000,AB1100,BA1100,AB1200,BA1200,AB1300,BA1300,AB1400,BA1400,AB1500,BA1500,AB1600,BA1600,AB1700,BA1700,AB1800,BA1800]) as yaram 
from counts),
direction_hours as (
select cid,a,b,countdate,substring(direction_hour,1,2) as direction,1 + substring(direction_hour,3,6)::int/100 as hour_of_count,yaram
from initial_long 
),
directing as(
select cid, 
case 
when direction = 'AB' then a
when direction = 'BA' then b 
end as a,
case 
when direction = 'AB' then b
when direction = 'BA' then a 
end as b,
hour_of_count, yaram,countdate from direction_hours
),
with_type as(
select d.*,l."type" from directing d
left join links l 
on d.a = l.i and d.b = l.j
where l."type" is not null
)
select cid,extract(year from countdate) year, a,b,concat(a,'_',b,'_',type) link_id,hour_of_count, yaram from with_type;


select cid,linkid 
from (
select cid,linkid ,ROW_NUMBER() OVER (PARTITION BY linkid ORDER BY cid DESC) AS RowNo 
from counts) x 
where RowNo = 1
order by cid,linkid;





select distinct link_id,cid from counts_data order by link_id,cid 




with first_table as(select distinct hhid,pnum from trips where origtaz in (3,14,15,304,409)), second_table as(select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips) select count(second_table.*) from first_table left join second_table using (hhid,pnum);
select lines from lines where lines not in (select line from detailed_headway);
select line from detailed_headway where line not in (select lines from lines);
select * from readable_headway rh where start_time like '21%';





WITH bldg_rel AS (
SELECT
	*
FROM
	bldg_cent
WHERE
	st_intersects(centroid, (
	SELECT
		st_union(geometry)
	FROM
		inner_taz))
	AND (uniq_id NOT IN (
	SELECT
		bldg_id
	FROM
		poi_bldg)
	OR uniq_id IN (
	SELECT
		bldg_id
	FROM
		poi_bldg
	WHERE
		USG_CODE = 7600))
	AND (ftype = 11
	OR ftype = 31)
	OR fake),
-- bldg_cent_taz - joining taz to bldg_rel
 bldg_cent_taz AS
  (SELECT bldg_rel.*,
          taz600.taz
   FROM bldg_rel
   LEFT JOIN taz600 ON st_intersects(bldg_rel.centroid, taz600.geometry)), 
-- ht - count of households per taz
 ht AS
  (SELECT hometaz,
          COUNT(*)
   FROM households
   GROUP BY hometaz
   ORDER BY hometaz), 
-- bldg_taz_avg_ht - calculation of average building height per taz
 bldg_taz_avg_ht AS
  (SELECT bldg_cent_taz.taz,
          AVG(COALESCE(bldg_cent_taz.bldg_ht, 1)) AS bldg_ht_taz_avg
   FROM bldg_cent_taz
   LEFT JOIN ht ON bldg_cent_taz.taz = ht.hometaz
   GROUP BY bldg_cent_taz.taz), 
-- bldg_ht_final_tbl - big table, where the building height is completed for bluilding without height,
-- based on the average building height in the taz.
-- tazs without households are exculded
 bldg_ht_final_tbl AS
  (SELECT bldg_cent_taz.*,
          ht.*,
          CASE
              WHEN bldg_cent_taz.bldg_ht IS NOT NULL THEN bldg_cent_taz.bldg_ht
              ELSE bldg_taz_avg_ht.bldg_ht_taz_avg
          END AS bldg_ht_final
   FROM bldg_cent_taz
   LEFT JOIN ht ON bldg_cent_taz.taz = ht.hometaz
   LEFT JOIN bldg_taz_avg_ht ON bldg_taz_avg_ht.taz = ht.hometaz
   WHERE ht.count IS NOT null or bldg_cent_taz.taz > 9000), 
-- first_calc - calculating the volume of each building, the sum of all volumes in the taz,
-- the relative volume for each building,
-- and the multiplication of the number of households in taz by the relative volume of each building
 first_calc AS
  (SELECT *,
          area * bldg_ht_final AS volume,
          sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS taz_volume,
                                         area * bldg_ht_final / sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS rel_taz_volume,
                                                                                               COUNT *area * bldg_ht_final / sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS float_households_at_bldg
   FROM bldg_ht_final_tbl), 
-- add_rn - adding row numbers for each row, ordered by float_households_at_bldg
 add_rn AS
  (SELECT *,
          row_number() OVER (PARTITION BY taz
                             ORDER BY float_households_at_bldg) AS rn
   FROM first_calc) 
-- bental_households - rounding the float_households_at_bldg using cascade_rounding_window
--select * from ht;
SELECT *,
       cascade_rounding_window(float_households_at_bldg) OVER (PARTITION BY taz
                                                               ORDER BY rn) AS households_at_bldg
FROM add_rn
ORDER BY taz,
         float_households_at_bldg DESC;
         
        
select origtaz,desttaz,count(*) from trips group by origtaz, desttaz having desttaz = 8641;



WITH processed_headway_periods AS(
SELECT
	*, EXTRACT(EPOCH
FROM
	hp.start_time::INTERVAL)/ 60 start_minutes, EXTRACT(EPOCH
FROM
	hp.end_time::INTERVAL)/ 60 end_minutes
FROM
	headway_periods hp ),
car_trips_periods AS(
SELECT
	t.hhid, t.pnum, t.persontripnum, t.finalDepartMinute , hp.*
FROM
	trips t
LEFT JOIN processed_headway_periods hp ON
	(MOD((t.finalDepartMinute + 180)::integer, 1440)) >= hp.start_minutes
	AND (MOD((t.finalDepartMinute + 180)::integer, 1440)) < hp.end_minutes
WHERE
	t.modecode IN (1, 2, 3) ),
percent_periods AS(
SELECT
	DISTINCT headway_period, start_time, end_time, (count(*) OVER (PARTITION BY headway_period) / count(*) OVER ()::NUMERIC) perc
FROM
	car_trips_periods
ORDER BY
	start_time ),
third_period AS(
SELECT
	perc
FROM
	percent_periods
WHERE
	headway_period = 'second_period' ),
relative_periods AS(
SELECT
	percent_periods.*, percent_periods.perc / third_period.perc AS rel, ROW_NUMBER() OVER()
FROM
	percent_periods, third_period ),
external_trips AS(
SELECT
	*
FROM
	external_trips_matrix, generate_series(1, 10)
WHERE
	ext_aut > 0 ),
init_number_of_trips AS (
SELECT
	external_trips.*, external_trips.ext_aut*relative_periods.rel AS number_of_trips
FROM
	external_trips
LEFT JOIN relative_periods ON
	external_trips.generate_series = relative_periods.row_number ),
cascaded_ext AS(
SELECT
	origin, destination, generate_series AS day_period, cascade_rounding_window(number_of_trips) OVER(PARTITION BY origin, destination
ORDER BY
	number_of_trips) number_of_trips_1
FROM
	init_number_of_trips
ORDER BY
	origin, destination, generate_series ),
individualized_trips AS(
SELECT
	*, 1 AS COUNTER
FROM
	cascaded_ext, generate_series(1, number_of_trips_1) ),
individualized_trips_with_times AS(
SELECT
	individualized_trips.*, EXTRACT(EPOCH
FROM
	relative_periods.start_time::INTERVAL) start_seconds, EXTRACT(EPOCH
FROM
	relative_periods.end_time::INTERVAL) end_seconds
FROM
	individualized_trips
LEFT JOIN relative_periods ON
	individualized_trips.day_period = relative_periods.row_number
ORDER BY
	origin, destination, day_period, generate_series)
SELECT
	*
FROM
	external_trips
	order by origin,destination,generate_series;
	
select sum(ext_aut) over() from external_trips_matrix where destination not in (select taz from taz600) or origin not in (select taz from taz600) order by origin, destination ;
select * from external_agents where destination in (select taz from taz600) and origin in (select taz from taz600);


with initial_long as (
select cid,a,b,countdate,linkid,
unnest(array['AB0600','BA0600','AB0700','BA0700','AB0800','BA0800','AB0900','BA0900','AB1000','BA1000','AB1100','BA1100','AB1200','BA1200','AB1300','BA1300','AB1400','BA1400','AB1500','BA1500','AB1600','BA1600','AB1700','BA1700','AB1800','BA1800']) as direction_hour,
unnest(array[AB0600,BA0600,AB0700,BA0700,AB0800,BA0800,AB0900,BA0900,AB1000,BA1000,AB1100,BA1100,AB1200,BA1200,AB1300,BA1300,AB1400,BA1400,AB1500,BA1500,AB1600,BA1600,AB1700,BA1700,AB1800,BA1800]) as yaram 
from counts),
direction_hours as (
select cid,a,b,countdate,substring(direction_hour,1,2) as direction,1 + substring(direction_hour,3,6)::int/100 as hour_of_count,linkid,yaram
from initial_long 
),
directing as(
select cid, linkid,
case 
when direction = 'AB' then a
when direction = 'BA' then b 
end as a,
case 
when direction = 'AB' then b
when direction = 'BA' then a 
end as b,
hour_of_count, yaram,countdate from direction_hours
),
with_type as(
select d.*,l."type" from directing d
left join links l 
on d.a = l.i and d.b = l.j
where l."type" is not null
)
select cid,extract(year from countdate) count_year, a,b,concat(a,'_',b,'_',type) link_id,linkid,hour_of_count, yaram 
from with_type
order by count_year,cid,link_id,hour_of_count;


select * from counts_data;

select distinct link_id,cid from counts_data; --2964

SELECT link_id,cid 
FROM counts_data
GROUP BY
    link_id,cid
   having sum(yaram) = 0
  order by link_id,cid; -- 75

SELECT link_id,cid 
FROM counts_data
GROUP BY
    link_id,cid
   having count(*) > 1 and min(yaram) = 0 and sum(yaram) > 0
  order by link_id,cid; -- 53

SELECT link_id,cid 
FROM counts_data
GROUP BY
    link_id,cid
   having sum(yaram) > 0 and not (count(*) > 1 and min(yaram) = 0)
  order by link_id,cid; -- 2836  

select link_id,hour_of_count,avg(yaram) yaram
from counts_data
where (link_id,cid)  in (SELECT link_id,cid 
FROM counts_data
GROUP BY
    link_id,cid
   having sum(yaram) > 0 and not (count(*) > 1 and min(yaram) = 0)
  order by link_id,cid)
  group by link_id,hour_of_count;