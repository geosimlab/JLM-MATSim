--script to spread housing units into explicit spatial location-- bldg_rel - selecting all centroids of buildings where building is in the inner tazs,-- residintial buildings and ftype == 11(building) or ftype == 31 (building under construction)CREATE TABLE IF NOT EXISTS bental_households AS WITH bldg_rel AS (SELECT	*FROM	bldg_centWHERE	st_intersects(centroid, (	SELECT		st_union(geometry)	FROM		inner_taz))	AND (uniq_id NOT IN (	SELECT		bldg_id	FROM		poi_bldg)	OR uniq_id IN (	SELECT		bldg_id	FROM		poi_bldg	WHERE		USG_CODE = 7600))	AND (ftype = 11	OR ftype = 31)	OR fake),-- bldg_cent_taz - joining taz to bldg_rel bldg_cent_taz AS  (SELECT bldg_rel.*,          taz600.taz   FROM bldg_rel   LEFT JOIN taz600 ON st_intersects(bldg_rel.centroid, taz600.geometry)), -- ht - count of households per taz ht AS  (SELECT hometaz,          COUNT(*)   FROM households   GROUP BY hometaz   ORDER BY hometaz), -- bldg_taz_avg_ht - calculation of average building height per taz bldg_taz_avg_ht AS  (SELECT bldg_cent_taz.taz,          AVG(COALESCE(bldg_cent_taz.bldg_ht, 1)) AS bldg_ht_taz_avg   FROM bldg_cent_taz   LEFT JOIN ht ON bldg_cent_taz.taz = ht.hometaz   GROUP BY bldg_cent_taz.taz), -- bldg_ht_final_tbl - big table, where the building height is completed for building without height,-- based on the average building height in the taz.-- tazs without households are exculded bldg_ht_final_tbl AS  (SELECT bldg_cent_taz.*,          ht.*,          CASE              WHEN bldg_cent_taz.bldg_ht IS NOT NULL THEN bldg_cent_taz.bldg_ht              ELSE bldg_taz_avg_ht.bldg_ht_taz_avg          END AS bldg_ht_final   FROM bldg_cent_taz   LEFT JOIN ht ON bldg_cent_taz.taz = ht.hometaz   LEFT JOIN bldg_taz_avg_ht ON bldg_taz_avg_ht.taz = ht.hometaz   WHERE ht.count IS NOT null or bldg_cent_taz.taz > 9000), -- first_calc - calculating the volume of each building, the sum of all volumes in the taz,-- the relative volume for each building,-- and the multiplication of the number of households in taz by the relative volume of each building first_calc AS  (SELECT *,          area * bldg_ht_final AS volume,          sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS taz_volume,                                         area * bldg_ht_final / sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS rel_taz_volume,                                                                                               COUNT *area * bldg_ht_final / sum(area * bldg_ht_final) OVER (PARTITION BY taz) AS float_households_at_bldg   FROM bldg_ht_final_tbl), -- add_rn - adding row numbers for each row, ordered by float_households_at_bldg add_rn AS  (SELECT *,          row_number() OVER (PARTITION BY taz                             ORDER BY float_households_at_bldg) AS rn   FROM first_calc) -- bental_households - rounding the float_households_at_bldg using cascade_rounding_window--select * from ht;SELECT *,       cascade_rounding_window(float_households_at_bldg) OVER (PARTITION BY taz                                                               ORDER BY rn) AS households_at_bldgFROM add_rnORDER BY taz,         float_households_at_bldg DESC;-- assert bental_households goes into geometry columnsSELECT Populate_Geometry_Columns('public.bental_households'::regclass);ALTER TABLE bental_householdsADD PRIMARY KEY (uniq_id);ALTER TABLE bental_householdsADD CONSTRAINT bental_households_to_bldg FOREIGN KEY (uniq_id) REFERENCES bldg_cent (uniq_id);