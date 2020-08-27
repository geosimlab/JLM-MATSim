drop table if exists counts_data;
CREATE TABLE IF NOT EXISTS counts_data AS 
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