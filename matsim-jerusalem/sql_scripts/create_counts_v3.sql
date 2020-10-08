--Average of all count stations without an hour with zero count of 2015 - version 3
select link_id,hour_of_count,avg(yaram) yaram from( 
select link_id,hour_of_count, yaram, split_part(link_id,'_',3) type_r  
from counts_data
where (link_id,cid)  in (SELECT link_id,cid  
FROM counts_data 
GROUP BY 
link_id,cid 
having not (count(*) > 1 and min(yaram) = 0) 
order by link_id,cid)  
and count_year =2015) q 
where type_r != '10' 
group by link_id,hour_of_count;