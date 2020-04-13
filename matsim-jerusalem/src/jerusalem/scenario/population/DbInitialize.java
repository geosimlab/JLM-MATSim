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
	private static String taz_path = props.getProperty("db.taz_paths");

	// helper for psql commands - the end of a lot of commands is similar
	private static String helperCommandEnd = username + ":" + password + "@" + url + ":" + port + "/" + db_name;

	// psql command: refering to sql file (/sql_scirpts/load_csv) that creates the
	// db tables
	private static String loadTables = "psql -c \"\\i ./sql_scripts/load_csv.sql\" postgresql://" + helperCommandEnd;

	// psql command: copying the trips csv into db
	private static String copyTrips = "psql -c \"\\copy trips FROM '" + trip_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the persons csv into db
	private static String copyPersons = "psql -c \"\\copy persons FROM '" + person_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the households csv into db
	private static String copyHouseHolds = "psql -c \"\\copy households FROM '" + household_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: copying the TAZ_coordinated csv into db
	private static String copyTAZ = "psql -c \"\\copy TAZ_coordinates FROM '" + taz_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + helperCommandEnd;

	// psql command: refering to sql files (/sql_scirpts/create_indices) that
	// creates indices
	private static String createIndices = "psql -c \"\\i ./sql_scripts/create_indices.sql\" postgresql://"
			+ helperCommandEnd;

	public static void main(String[] args) throws IOException {

		// running psql commands
		DbUtils.runCommand(loadTables);
		DbUtils.runCommand(copyTrips);
		DbUtils.runCommand(copyPersons);
		DbUtils.runCommand(copyHouseHolds);
		DbUtils.runCommand(copyTAZ);
		DbUtils.runCommand(createIndices);
	}
}