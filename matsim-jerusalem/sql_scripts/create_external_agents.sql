DROP TABLE IF EXISTS external_agents;
CREATE TABLE IF NOT EXISTS external_agents AS WITH processed_headway_periods AS(
SELECT
	*, EXTRACT(EPOCH
FROM
	hp.start_time::INTERVAL)/ 60 start_minutes, EXTRACT(EPOCH
FROM
	hp.end_time::INTERVAL)/ 60 end_minutes
FROM
	headway_periods hp ),
car_trips_periods AS(
SELECT
	t.hhid, t.pnum, t.persontripnum, t.finalDepartMinute , hp.*
FROM
	trips t
LEFT JOIN processed_headway_periods hp ON
	(MOD((t.finalDepartMinute + 180)::integer, 1440)) >= hp.start_minutes
	AND (MOD((t.finalDepartMinute + 180)::integer, 1440)) < hp.end_minutes
WHERE
	t.modecode IN (1, 2, 3) ),
percent_periods AS(
SELECT
	DISTINCT headway_period, start_time, end_time, (count(*) OVER (PARTITION BY headway_period) / count(*) OVER ()::NUMERIC) perc
FROM
	car_trips_periods
ORDER BY
	start_time ),
third_period AS(
SELECT
	perc
FROM
	percent_periods
WHERE
	headway_period = 'second_period' ),
relative_periods AS(
SELECT
	percent_periods.*, percent_periods.perc / third_period.perc AS rel, ROW_NUMBER() OVER()
FROM
	percent_periods, third_period ),
external_trips AS(
SELECT
	*
FROM
	external_trips_matrix, generate_series(1, 10)
WHERE
	ext_aut > 0 ),
init_number_of_trips AS (
SELECT
	external_trips.*, external_trips.ext_aut*relative_periods.rel AS number_of_trips
FROM
	external_trips
LEFT JOIN relative_periods ON
	external_trips.generate_series = relative_periods.row_number ),
cascaded_ext AS(
SELECT
	origin, destination, generate_series AS day_period, cascade_rounding_window(number_of_trips) OVER(PARTITION BY origin, destination
ORDER BY
	number_of_trips) number_of_trips_1
FROM
	init_number_of_trips
ORDER BY
	origin, destination, generate_series ),
individualized_trips AS(
SELECT
	*, 1 AS COUNTER
FROM
	cascaded_ext, generate_series(1, number_of_trips_1) ),
individualized_trips_with_times AS(
SELECT
	individualized_trips.*, EXTRACT(EPOCH
FROM
	relative_periods.start_time::INTERVAL) start_seconds, EXTRACT(EPOCH
FROM
	relative_periods.end_time::INTERVAL) end_seconds
FROM
	individualized_trips
LEFT JOIN relative_periods ON
	individualized_trips.day_period = relative_periods.row_number
ORDER BY
	origin, destination, day_period, generate_series)
SELECT
	*,
	round(start_seconds + random()*(end_seconds - start_seconds)) mock_timestamp
FROM
	individualized_trips_with_times;