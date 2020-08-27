create table if not exists pt_routes as with orig_pt_routes as (
select
	line_path.line, line_path.seq_number, concat(links.i, '_', links.j, '_', links.type) as linkid, max(seq_number) over (partition by line) as last_link, line_path.stop, lines.transport_mode, lines.description, links.length_met, sum(line_path.stop) over(partition by line_path.line
order by
	line_path.seq_number) cumsum_stop
from
	line_path
left join links on
	links.i = line_path.i
	and links.j = line_path.j
left join lines on
	line_path.line = lines.lines
order by
	line, seq_number),
summed as(
select
	line, cumsum_stop, sum (length_met) distance
from
	orig_pt_routes
group by
	line, cumsum_stop
order by
	line, cumsum_stop),
final_t as(
select
	orig_pt_routes.*, summed.distance
from
	orig_pt_routes
left join summed on
	orig_pt_routes.line = summed.line
	and orig_pt_routes.cumsum_stop = summed.cumsum_stop)
select
	line,
	seq_number,
	linkid,
	last_link,
	stop,
	description,
	distance,
	case
		when transport_mode = 'l' then distance / 7.7
		when transport_mode = 'r' then distance / 10
		when transport_mode = 'b' then distance / 5.5
	end as passing_time,
	case
		when transport_mode = 'l' then 20
		when transport_mode = 'r' then 45
		when transport_mode = 'b' then 15
	end as stalling_time,
	case
		when transport_mode = 'l' then 'light rail'
		when transport_mode = 'r' then 'train'
		when transport_mode = 'b' then 'bus'
	end as transport_mode_string
from
	final_t
order by
	line,
	seq_number;
ALTER TABLE pt_routes 
add primary key (line,seq_number);
ALTER TABLE pt_routes
ADD CONSTRAINT pt_routes_to_lines FOREIGN KEY (line) REFERENCES lines (lines);