--create bental_jtmt_code_conversion_long and insert purp_99 with all purposes for fake buildings
select
	usg_group, usg_code, unnest(array['purp_1', 'purp_2', 'purp_3', 'purp_4', 'purp_5', 'purp_6', 'purp_7', 'purp_8']) as key1, unnest(array[purp_1, purp_2, purp_3, purp_4, purp_5, purp_6, purp_7, purp_8]) as value1
from
	bental_jtmt_code_conversion )
select
	*
from
	first_table
where
	value1 is not null);