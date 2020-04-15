CREATE TABLE IF NOT EXISTS taz_centroid AS SELECT taz::integer, st_x(st_centroid(geometry))::int AS x, st_y(st_centroid(geometry))::int AS y FROM taz600;
CREATE INDEX idx_hhid_t ON trips(hhid);
CREATE INDEX idx_hhid_p ON persons(hhid);
CREATE INDEX idx_hhid_h ON households(hhid);