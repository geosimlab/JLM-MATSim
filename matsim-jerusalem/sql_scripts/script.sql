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

