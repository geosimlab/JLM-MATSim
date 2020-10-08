--Average of all count stations without dongles and without an hour with zero count - version 4
select link_id,hour_of_count,avg(yaram) yaram from( 
select link_id,hour_of_count, yaram, split_part(link_id,'_',3) type_r  
from counts_data
where (link_id,cid)  in (SELECT link_id,cid  
FROM counts_data 
GROUP BY 
link_id,cid 
having not (count(*) > 1 and min(yaram) = 0) 
order by link_id,cid)) q 
where type_r != '10' 
and link_id not in (select id from (
select concat(l.i, '_', l.j, '_', l.type) id, count(*) over(partition by l.j) cj, count(*) over(partition by l.i) ci
	from links l
	where l.type not in (9,10)
	) q
	where cj = 1 or ci = 1)
group by link_id,hour_of_count;





