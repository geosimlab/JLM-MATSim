--create bental_jtmt_code_conversion_long and insert purp_99 with all purposes for fake buildingsdrop table if exists bental_jtmt_code_conversion_long;create table if not exists bental_jtmt_code_conversion_long as ( with first_table as(
select
	usg_group, usg_code, unnest(array['purp_1', 'purp_2', 'purp_3', 'purp_4', 'purp_5', 'purp_6', 'purp_7', 'purp_8']) as key1, unnest(array[purp_1, purp_2, purp_3, purp_4, purp_5, purp_6, purp_7, purp_8]) as value1
from
	bental_jtmt_code_conversion )
select
	*
from
	first_table
where
	value1 is not null);insert into bental_jtmt_code_conversion_long select 100 as usg_group, 10000 as usg_code, 'purp_99' as key1, value1 from (select distinct value1 from bental_jtmt_code_conversion_long) q;ALTER TABLE bental_jtmt_code_conversion_longadd constraint bjccl_to_jmcc foreign key (value1) references jtmt_matsim_code_conversion (jtmt_code);	