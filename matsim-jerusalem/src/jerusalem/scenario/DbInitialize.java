package jerusalem.scenario;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ido Klein
 */

public class DbInitialize {

	// getting parameters for commands
	public static Properties props = DbUtils.readProperties("database.properties");
	public final static String username = props.getProperty("db.username");
	public final static String password = props.getProperty("db.password");
	public final static String db_url = props.getProperty("db.url");
	public final static String port = props.getProperty("db.port");
	public final static String db_name = props.getProperty("db.db_name");
	public final static String psql_path = props.getProperty("db.psql_path");
	public final static String ogr2ogr_path = props.getProperty("db.ogr2ogr_path");
	public final static String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name + "?loggerLevel=DEBUG";

	// helper for psql commands - the end of a lot of commands is similar
	private static String helperCommandEnd = username + ":" + password + "@" + db_url + ":" + port + "/" + db_name;

	// psql command: refering to sql file (/sql_scirpts/load_csv) that creates the
	// db tables
	private static String loadTables = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/load_csv.sql\" postgresql://" + helperCommandEnd;

	// psql command: copying the trips csv into db
	private static String copyTrips = "psql -c \"\\copy trips FROM '" + props.getProperty("db.trip_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the persons csv into db
	private static String copyPersons = "psql -c \"\\copy persons FROM '" + props.getProperty("db.person_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the households csv into db
	private static String copyHouseHolds = "psql -c \"\\copy households FROM '" + props.getProperty("db.household_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the nodes csv into db
	private static String copyNodes = "psql -c \"\\copy nodes (i,is_centroid,x,y) FROM '"
			+ props.getProperty("db.nodes_path") + "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the links csv into db
	private static String copyLinks = "psql -c \"\\copy links FROM '" + props.getProperty("db.links_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the bental_jtmt_code_conversion csv into db
	private static String copybental_jtmt_code_conversion = "psql -c \"\\copy bental_jtmt_code_conversion FROM '"
			+ props.getProperty("db.bental_jtmt_code_conversion_path") + "' DELIMITER ',' CSV HEADER;\" postgresql://"
			+ helperCommandEnd;

	// psql command: copying the line_path csv into db
	private static String copyLine_path = "psql -c \"\\copy line_path FROM '" + props.getProperty("db.line_path_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the lines csv into db
	private static String copyLines = "psql -c \"\\copy lines FROM '" + props.getProperty("db.lines_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the headway csv into db
	private static String copyHeadway = "psql -c \"\\copy headway FROM '" + props.getProperty("db.headway_path")
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the vehicle_types csv into db
	private static String copyVehicle_types = "psql -c \"\\copy vehicle_types FROM '"
			+ props.getProperty("db.vehicle_types_path") + "' DELIMITER ',' CSV HEADER;\" postgresql://"
			+ helperCommandEnd;

	// psql command: copying the TAZShp into db
	private static String copyTAZShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ db_url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
			+ props.getProperty("db.input_taz_shp") + " -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

	// psql command: copying the BLDGShp into db
	private static String copyBLDGShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ db_url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
			+ props.getProperty("db.input_bldg_shp") + " BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

	// psql command: copying the POI_BLDGshp into db
	private static String copyBLDGPOIShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
			+ db_url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
			+ props.getProperty("db.input_bldg_shp") + " POI_BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";

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

	// psql command: referring to sql files
	// (/sql_scirpts/create_households_coordinates.sql) that
	// creates a table with home coordinates for each household by taz
	private static String create_other_activities_coordinates = "psql -c \"\\i "
			+ System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/create_other_activities_coordinates.sql\" postgresql://" + helperCommandEnd;

	// psql command: referring to sql files
	// (/sql_scirpts/create_stops.sql) that
	// creates a table with pt stops
	private static String create_stops = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/stops.sql\" postgresql://" + helperCommandEnd;

	// psql command: referring to sql files
	// (/sql_scirpts/create_pt_routes.sql) that
	// creates a table with routes of pt lines
	private static String create_pt_routes = "psql -c \"\\i " + System.getProperty("user.dir").replace("\\", "/")
			+ "/sql_scripts/pt_routes.sql\" postgresql://" + helperCommandEnd;

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
		DbUtils.runCommand(copyNodes, psql_path);
		DbUtils.runCommand(copyLinks, psql_path);
		DbUtils.runCommand(copyLine_path, psql_path);
		DbUtils.runCommand(copyLines, psql_path);
		DbUtils.runCommand(copyHeadway, psql_path);
		DbUtils.runCommand(copyVehicle_types, psql_path);
		DbUtils.runCommand(copybental_jtmt_code_conversion, psql_path);
		DbUtils.runCommand(copyTAZShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGPOIShp, ogr2ogr_path);
		DbUtils.runCommand(cascade_rounding, psql_path);
		DbUtils.runCommand(wrapup, psql_path);
		DbUtils.runCommand(create_households_coordinates, psql_path);
		DbUtils.runCommand(create_other_activities_coordinates, psql_path);
		DbUtils.runCommand(create_stops, psql_path);
		DbUtils.runCommand(create_pt_routes, psql_path);
	}
}