package jerusalem.scenario.population;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ido Klein
 */

public class DbInitialize {

	// getting parameters for commands
	private static Properties props = DbUtils.readProperties("database.properties");
	private static String username = props.getProperty("db.username");
	private static String password = props.getProperty("db.password");
	private static String url = props.getProperty("db.url");
	private static String port = props.getProperty("db.port");
	private static String db_name = props.getProperty("db.db_name");
	private static String trip_path = props.getProperty("db.trip_path");
	private static String person_path = props.getProperty("db.person_path");
	private static String household_path = props.getProperty("db.household_path");
	private static String TAZShp_path = props.getProperty("db.input_taz_shp");
	private static String BLDGShp_path = props.getProperty("db.input_bldg_shp");
	private static String psql_path = props.getProperty("db.psql_path");
	private static String ogr2ogr_path = props.getProperty("db.ogr2ogr_path");

	// helper for psql commands - the end of a lot of commands is similar
	private static String helperCommandEnd = username + ":" + password + "@" + url + ":" + port + "/" + db_name;

	// psql command: refering to sql file (/sql_scirpts/load_csv) that creates the
	// db tables
	private static String loadTables = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/load_csv.sql\" postgresql://" + helperCommandEnd;

	// psql command: copying the trips csv into db
	private static String copyTrips = "psql -c \"\\copy trips FROM '" + trip_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the persons csv into db
	private static String copyPersons = "psql -c \"\\copy persons FROM '" + person_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the households csv into db
	private static String copyHouseHolds = "psql -c \"\\copy households FROM '" + household_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the TAZShp into db
	private static String copyTAZShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" " + TAZShp_path
			+ " -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

	// psql command: copying the BLDGShp into db
	private static String copyBLDGShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" " + BLDGShp_path
			+ " BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

	// psql command: copying the BLDGShp into db
	private static String copyBLDGPOIShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" " + BLDGShp_path
			+ " POI_BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

	// psql command: referring to sql files (/sql_scirpts/cascade_rounding.sql) that
	// creates an aggregate function that performs cascade rounding in sql
	private static String cascade_rounding = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/cascade_rounding.sql\" postgresql://" + helperCommandEnd;

	// psql command: referring to sql files
	// (/sql_scirpts/create_households_coordinates.sql) that
	// creates a table with home coordinates for each household by taz
	private static String create_households_coordinates = "psql -c \"\\i "
			+ System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/create_households_coordinates.sql\" postgresql://" + helperCommandEnd;

	// psql command: crating partial tables, creating indices with file
	// (/sql_scirpts/wrapup.sql)
	private static String wrapup = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/wrapup.sql\" postgresql://" + helperCommandEnd;

	public static void main(String[] args) throws IOException {

		// running psql commands
		DbUtils.runCommand(loadTables, psql_path);
		DbUtils.runCommand(copyTrips, psql_path);
		DbUtils.runCommand(copyPersons, psql_path);
		DbUtils.runCommand(copyHouseHolds, psql_path);
		DbUtils.runCommand(copyTAZShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGPOIShp, ogr2ogr_path);
		DbUtils.runCommand(cascade_rounding, psql_path);
		DbUtils.runCommand(wrapup, psql_path);
		DbUtils.runCommand(create_households_coordinates, psql_path);
	}
}