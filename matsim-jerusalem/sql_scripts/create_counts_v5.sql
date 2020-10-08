--Average of all count stations without dongles and without an hour with zero count in the jlm inner area - v5
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
and link_id in(
SELECT
	concat(l.i, '_', l.j, '_', l.type)
FROM
	links l
LEFT JOIN nodes na ON
	l.i = na.i
LEFT JOIN nodes nb ON
	l.j = nb.i
LEFT JOIN taz600 t2 ON
	ST_Within(na.geometry, t2.geometry)
LEFT JOIN taz600 t3 ON
	ST_Within(nb.geometry, t3.geometry)
WHERE
	l.type != 9
	AND (t2.taz IN (
	SELECT
		taz
	FROM
		taz600
	WHERE
		taz < 1000
		OR (taz > 3000
		AND taz < 4700)
		OR (taz > 4900
		AND taz < 5000)
		OR (taz > 5000
		AND taz < 5600)
		OR (taz > 5700
		AND taz < 6400))
	OR t3.taz IN (
	SELECT
		taz
	FROM
		taz600
	WHERE
		taz < 1000
		OR (taz > 3000
		AND taz < 4700)
		OR (taz > 4900
		AND taz < 5000)
		OR (taz > 5000
		AND taz < 5600)
		OR (taz > 5700
		AND taz < 6400)))
)
group by link_id,hour_of_count;


