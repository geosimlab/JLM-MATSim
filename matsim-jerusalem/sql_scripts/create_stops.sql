CREATE TABLE IF NOT EXISTS stops as 
with unique_links as(
	select distinct i,j  
	from line_path 
	where stop = 1
	)
select row_number() over(), 
concat(unique_links.i,'_',unique_links.j,'_',links.type) as linkid,
st_x(nodes.geometry) as x,
st_x(nodes.geometry) as y
from unique_links
Left join links
on unique_links.i = links.i and unique_links.j = links.j
left join nodes
on nodes.i::integer = links.i;