package jerusalem.scenario.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import jerusalem.scenario.network.CreateNetwork;
import jerusalem.turnrestrictionsparser.Turns2Network;

/**
 * @author User
 *
 */
public class DbInitialize {
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	public static Properties props = DbUtils.readProperties("database2040.properties");
	public final static String username = props.getProperty("db.username");
	public final static String password = props.getProperty("db.password");
	public final static String port = props.getProperty("db.port");
	public final static String db_name = props.getProperty("db.db_name");
	public final static String db_url = props.getProperty("db.url");
	public final static String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name + "?loggerLevel=DEBUG";

	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
		Properties props = DbUtils.readProperties("database2040.properties");
		String own_path = System.getProperty("user.dir").replace("\\", "/");
		String ogr2ogr_path = props.getProperty("folder.ogr2ogr_path");
		DriverManager.setLogWriter(new PrintWriter(System.out));
		Connection con = DriverManager.getConnection(url, props.getProperty("db.username"),
				props.getProperty("db.password"));
//		create tables and functions
		executeSQLFromFile(own_path + "/sql_scripts/load_csv.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/cascade_rounding.sql", con);
//		copy tables from csv
		copyTableFromCSV(props.getProperty("population.household_path"), "households", con);
		copyTableFromCSV(props.getProperty("population.person_path"), "persons", con);
		copyTableFromCSV(props.getProperty("population.trip_path"), "trips", con);
		copyTableFromCSV(own_path + "/data/BENTAL_JTMT_CODE_CONVERSION.csv", "bental_jtmt_code_conversion", con);
		copyTableFromCSV(own_path + "/data/jtmt_matsim_code_conversion.csv", "jtmt_matsim_code_conversion", con);
		copyTableFromCSV(props.getProperty("network.nodes_path"), "nodes", con);
		copyTableFromCSV(props.getProperty("network.links_path"), "links", con);
		copyTableFromCSV(own_path + "/data/vehicle_types.csv", "vehicle_types", con);
		copyTableFromCSV(props.getProperty("transit.lines_path"), "lines", con);
		copyTableFromCSV(props.getProperty("transit.line_path_path"), "line_path", con);
		copyTableFromCSV(props.getProperty("transit.headway_path"), "headway", con);
		copyTableFromCSV(props.getProperty("transit.headway_path2"), "detailed_headway", con);
		copyTableFromCSV(own_path + "/data/headway_periods.csv", "headway_periods", con);
		copyTableFromCSV(props.getProperty("counts.counts_path"), "counts", con);
		copyTableFromCSV(props.getProperty("external.matrix_path"), "external_trips_matrix", con);

		// copy tables from shp/gdb
		String copyTAZShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host=" + db_url
				+ " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
				+ props.getProperty("shp.taz_path") + " -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";
		String copyBLDGShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host=" + db_url
				+ " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
				+ props.getProperty("shp.bldg_path") + " BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";
		String copyBLDGPOIShp = "ogr2ogr -progress -overwrite -f \"PostgreSQL\" -a_srs \"EPSG:2039\" PG:\"host="
				+ db_url + " user=" + username + " dbname=" + db_name + " password=" + password + "\" "
				+ props.getProperty("shp.bldg_path") + " POI_BLDG -nlt PROMOTE_TO_MULTI -lco geometry_name=geometry";
		DbUtils.runCommand(copyTAZShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGShp, ogr2ogr_path);
		DbUtils.runCommand(copyBLDGPOIShp, ogr2ogr_path);
//		polish-up tables - sql
		executeSQLFromFile(own_path + "/sql_scripts/wrapup.sql", con);
//		prepare data for model
		executeSQLFromFile(own_path + "/sql_scripts/create_households_coordinates.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_bental_jtmt_conversion.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_amenities.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_stops.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_pt_routes.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_readable_headway.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_counts_data.sql", con);
		executeSQLFromFile(own_path + "/sql_scripts/create_external_agents.sql", con);
	}

	/**
	 * Function to execute sql scripts that does manipulations over tables. 
	 * used for local files from the sql_scripts folder.  
	 * @param path
	 * @param con
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void executeSQLFromFile(String path, Connection con) {
		log.info("exectuting " + path);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			Object[] lines = br.lines().toArray();
			String result = "";
			for (Object i : lines) {
				result = result + (String) i + "\n";
			}
			Statement st = con.createStatement();
			st.execute(result);
		} catch (SQLException | FileNotFoundException e) {
			log.info(e.getClass().getTypeName() + ": " + e.getMessage());
		}

	}

	/**
	 * Function to copy tables into sql from csv files.
	 * @param path path to csv
	 * @param con  Connection to database
	 * @throws SQLException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyTableFromCSV(String path, String table, Connection con) {
		log.info("copying " + table + " into database");
		long rowsInserted;
		try {
			rowsInserted = new CopyManager((BaseConnection) con).copyIn(
					"COPY " + table + " FROM STDIN (FORMAT csv, HEADER)", new BufferedReader(new FileReader(path)));

			log.info(rowsInserted + " rows inserted from " + path + " into database");
		} catch (SQLException | IOException e) {
			log.info(e.getClass().getTypeName() + ": " + e.getMessage());
		}

	}
	/**
	 * @param path
	 * @return
	 */
	public static String getPathToConvFile(String path) {
		File f = new File(path);
		String filename = f.getParent() + "/t_" + f.getName();
		return filename;
	}

}
