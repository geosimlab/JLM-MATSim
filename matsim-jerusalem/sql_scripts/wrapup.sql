--adding a geometry column to nodes, while offseting nodes by 70 meters west and 50 meters south
update nodes
set geometry = ST_translate(ST_SetSRID(ST_Point(x, y), 2039),-70,-50);
--creating a table for taz centroids - might by redundant
CREATE TABLE IF NOT EXISTS taz_centroid AS 
	SELECT taz::integer,
	 st_x(st_centroid(geometry))::int AS x,
	 st_y(st_centroid(geometry))::int AS y 
	 FROM taz600;
--changing taz600 
ALTER TABLE taz600 
	ALTER COLUMN taz TYPE INT USING taz::integer;
--creating a table for inner taz (taz in the model)
CREATE TABLE IF NOT EXISTS inner_taz AS 
	SELECT *  
	FROM taz600 
	WHERE taz < 9000;
--creating a table for building centriods 
CREATE TABLE IF NOT EXISTS bldg_cent AS 
	SELECT uniq_id, 
		   st_area(geometry) as area,
		   st_centroid(geometry)::geometry(Point,2039) as centroid,
		   ftype,
		   hi_pnt_z,
		   ht_land, 
		   bldg_ht 
    FROM bldg;

--adding the nodes into building centriods and bldg_poi
--stage 1: adding a field for real or fake for both tables, and populating exisitng rows with false
alter table bldg_cent
add column fake boolean DEFAULT FALSE;
alter table poi_bldg
add column fake boolean DEFAULT FALSE;
--stage 2: adding an altered nodes table into bldg_cent
insert into bldg_cent(uniq_id,area,centroid,ftype,fake)
select i + 100000000 as uniq_id, --fake uniq_id 
		1 as area, --fake area
		geometry as centroid, 
		11 as ftype,
		TRUE as fake
from nodes;
--stage 3: adding all fake values from bldg_cent to poi_bldg
insert into poi_bldg(uniq_id,bldg_id,fcode,usg_group,usg_code,geometry,e_ord,n_ord,fake)
select uniq_id,
		uniq_id as bldg_id, 
		1000 as fcode,--fake fcode
		100 as usg_group, --fake usg_group
		10000 as usg_code, --fake usg_code
		st_multi(centroid) as geometry,
		st_x(centroid) as e_ord,
		st_y(centroid) as n_ord,
		TRUE as fake
from bldg_cent
where fake;


--query to load links to network
--(SELECT row_number() over () AS _uid_,* FROM (with f as (select *, st_translate(geometry,-70,-50) geom from nodes)
 --select l.i,fa.geom geoma,l.j,fb.geom geomb, st_makeline(fa.geom,fb.geom) linea, st_makeline(fa.geometry,fb.geometry) lineb, l.mode
 --from links l
 --left join f fa
 --on l.i = fa.i
 --left join f fb
 --on l.j = fb.i ) AS _subq_1_ )
 
 --creating indices
CREATE INDEX idx_hhid_t ON trips(hhid);
CREATE INDEX idx_hhid_p ON persons(hhid);
CREATE INDEX idx_hhid_h ON households(hhid);
CREATE INDEX idx_bldg_cent ON bldg_cent USING GIST (centroid);
CREATE INDEX idx_inner_taz ON inner_taz USING GIST (geometry);
--populating geometry columns
SELECT Populate_Geometry_Columns('public.bldg_cent'::regclass);
SELECT Populate_Geometry_Columns('public.inner_taz'::regclass);
SELECT Populate_Geometry_Columns('public.nodes'::regclass);