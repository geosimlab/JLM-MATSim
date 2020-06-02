CREATE TABLE IF NOT EXISTS taz_centroid AS 
	SELECT taz::integer,
	 st_x(st_centroid(geometry))::int AS x,
	 st_y(st_centroid(geometry))::int AS y 
	 FROM taz600;
ALTER TABLE taz600 
	ALTER COLUMN taz TYPE INT USING taz::integer;
CREATE TABLE IF NOT EXISTS inner_taz AS 
	SELECT *  
	FROM taz600 
	WHERE taz < 9000;
CREATE TABLE IF NOT EXISTS bldg_cent AS 
	SELECT uniq_id, 
		   st_area(geometry) as area,
		   st_centroid(geometry)::geometry(Point,2039) as centroid,
		   ftype,
		   hi_pnt_z,
		   ht_land, 
		   bldg_ht 
    FROM bldg; 
CREATE INDEX idx_hhid_t ON trips(hhid);
CREATE INDEX idx_hhid_p ON persons(hhid);
CREATE INDEX idx_hhid_h ON households(hhid);
CREATE INDEX idx_bldg_cent ON bldg_cent USING GIST (centroid);
CREATE INDEX idx_inner_taz ON inner_taz USING GIST (geometry);
SELECT Populate_Geometry_Columns('public.bldg_cent'::regclass);
SELECT Populate_Geometry_Columns('public.inner_taz'::regclass);