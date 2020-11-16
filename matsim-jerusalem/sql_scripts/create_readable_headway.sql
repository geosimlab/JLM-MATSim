--readible headwy for all transit lines. 
--headway_long - unnest headway
create table if not exists readable_headway as with headway_long as (
select
	line, unnest(array['first_period', 'second_period', 'third_period', 'fourth_period', 'fifth_period', 'sixth_period', 'seventh_period', 'eighth_period', 'ninth_period', 'tenth_period' ]) as period_of_day, unnest(array[first_period, second_period, third_period, fourth_period, fifth_period, sixth_period, seventh_period, eighth_period, ninth_period, tenth_period]) as headway
from
	detailed_headway)
select
	headway_long.line,
	headway_long.headway,
	headway_periods.start_time,
	headway_periods.end_time,
	lines.description,
	vehicle_types.vehicle_type,
	vehicle_types.tau_name
from
	headway_long
left join headway_periods on
	headway_long.period_of_day = headway_periods.headway_period
left join lines on
	headway_long.line = lines.lines
left join vehicle_types on
	lines.vehicle = vehicle_types.vehicle_code;
ALTER TABLE readable_headway 
ADD PRIMARY KEY (line,start_time);
ALTER TABLE readable_headway
ADD CONSTRAINT rh_to_lines FOREIGN KEY (line) REFERENCES lines (lines);