--adding a geometry column to nodes, while offseting nodes by 70 meters west and 50 meters south
ALTER TABLE nodes
ADD COLUMN geometry geometry(Geometry,2039);
update nodes
set geometry = ST_translate(ST_SetSRID(ST_Point(x, y), 2039),-70,-50);
update nodes
set x = st_x(geometry),
	y = st_y(geometry); 
--changing taz600 
ALTER TABLE taz600 
	ALTER COLUMN taz TYPE INT USING taz::integer;
CREATE UNIQUE INDEX taz_600_taz 
ON taz600 (taz);
ALTER TABLE taz600 
ADD CONSTRAINT unique_taz 
UNIQUE USING INDEX taz_600_taz;
--creating a table for taz centroids - might be redundant
CREATE TABLE IF NOT EXISTS taz_centroid AS 
	SELECT taz,
	 st_x(st_centroid(geometry))::int AS x,
	 st_y(st_centroid(geometry))::int AS y 
	 FROM taz600;
ALTER TABLE taz_centroid
add PRIMARY KEY (taz);
ALTER TABLE taz_centroid
ADD CONSTRAINT taz_cent_to_taz600 FOREIGN KEY (taz) REFERENCES taz600 (taz);

--creating a table for inner taz (taz in the model)
CREATE TABLE IF NOT EXISTS inner_taz AS 
	SELECT *  
	FROM taz600 
	WHERE taz < 9000;
ALTER TABLE inner_taz
add PRIMARY KEY (taz);
ALTER TABLE inner_taz
ADD CONSTRAINT inner_taz_to_taz600 FOREIGN KEY (taz) REFERENCES taz600 (taz);
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
ALTER TABLE bldg_cent
add PRIMARY KEY (uniq_id);
--TODO maybe fake buildings should be added to BLDG
--ALTER TABLE bldg_cent
--ADD CONSTRAINT bldg_cent_to_bldg FOREIGN KEY (uniq_id) REFERENCES bldg(uniq_id);

--adding nodes into building centroids and bldg_poi
--stage 1: adding a field for real or fake for both tables, and populating exisitng rows with false
alter table bldg_cent
add column fake boolean DEFAULT FALSE;
ALTER TABLE poi_bldg ADD COLUMN fake boolean DEFAULT FALSE;

--stage 2: adding an altered nodes table into bldg_cent
 INSERT
	INTO
	bldg_cent(uniq_id, area, centroid, ftype, fake)
SELECT
	i::integer + 100000000 AS uniq_id,
	--fake uniq_id 
 0 AS area,
	--fake area
 geometry AS centroid,
	11 AS ftype,
	TRUE AS fake
FROM
	nodes;
--stage 3: adding all fake values from bldg_cent to poi_bldg
insert into poi_bldg(uniq_id,bldg_id,fcode,usg_group,usg_code,geometry,e_ord,n_ord,fake)
select uniq_id,
		uniq_id + 1000000 as bldg_id, 
		1000 as fcode,--fake fcode
		100 as usg_group, --fake usg_group
		10000 as usg_code, --fake usg_code
		st_multi(centroid) as geometry,
		st_x(centroid) as e_ord,
		st_y(centroid) as n_ord,
		TRUE as fake
from bldg_cent
where fake;


--query to load links to spatial network
--select l.*,st_makeline(fa.geometry,fb.geometry) line 
--from links l 
--left join nodes fa 
--on l.i = fa.i::integer 
--left join nodes fb 
--on l.j = fb.i::integer
 
--creating indices
CREATE INDEX idx_bldg_cent ON bldg_cent USING GIST (centroid);
CREATE INDEX idx_inner_taz ON inner_taz USING GIST (geometry);
CREATE INDEX idx_nodes ON nodes USING GIST (geometry);
--populating geometry columns
SELECT Populate_Geometry_Columns('public.bldg_cent'::regclass);
SELECT Populate_Geometry_Columns('public.inner_taz'::regclass);
SELECT Populate_Geometry_Columns('public.nodes'::regclass);