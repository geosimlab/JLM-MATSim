create table if not exists readable_headway as
with headway_long as (select line,unnest(array['first_period','second_period','third_period','fourth_period','fifth_period','sixth_period','seventh_period','eighth_period','ninth_period','tenth_period'
]) as period_of_day,
unnest(array[first_period,second_period,third_period,fourth_period,fifth_period,sixth_period,seventh_period,eighth_period,ninth_period,tenth_period]) as headway
from headway)
select headway_long.line,
headway_long.headway, 
headway_periods.start_time,
headway_periods.end_time,
lines.description,
vehicle_types.vehicle_type
from headway_long
left join headway_periods
on headway_long.period_of_day = headway_periods.headway_period
left join lines 
on headway_long.line = lines.lines
left join vehicle_types 
on lines.vehicle = vehicle_types.vehicle_code;