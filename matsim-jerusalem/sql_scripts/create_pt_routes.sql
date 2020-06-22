CREATE TABLE IF NOT EXISTS pt_routes AS 
select line_path.line,line_path.seq_number,
concat(links.i,'_',links.j,'_',links.type) as linkid,
max(seq_number) over (partition by line) as last_link,
line_path.stop,
lines.transport_mode
from line_path
left join links 
on links.i = line_path.i and links.j = line_path.j
left join lines
on line_path.line = lines.lines
order by line, seq_number;