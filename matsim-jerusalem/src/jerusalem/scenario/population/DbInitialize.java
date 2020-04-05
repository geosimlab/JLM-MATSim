package jerusalem.scenario.population;

import java.io.IOException;
import java.util.Properties;

public class DbInitialize {
	private static String path = "src/database.properties";
	private static Properties props = DbUtils.readProperties(path);
	private static String username = props.getProperty("db.username");
	private static String password = props.getProperty("db.password");
	private static String url = props.getProperty("db.url");
	private static String port = props.getProperty("db.port");
	private static String db_name = props.getProperty("db.db_name");
	private static String trip_path = props.getProperty("db.trip_path");
	private static String taz_path = props.getProperty("db.taz_paths");
	private static String end = username + ":" + password + "@" + url + ":" + port + "/" + db_name;
	private static String sqlCommand = "psql -c \"\\i ./sql_scripts/load_csv.sql\" postgresql://" + end;
	private static String copyTrips = "psql -c \"\\copy trips FROM '" + trip_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + end;
	private static String copyTAZ = "psql -c \"\\copy TAZ_coordinates FROM '" + taz_path
			+ "' DELIMITER ',' CSV HEADER;\" postgresql://" + end;

	public static void main(String[] args) throws IOException {
		DbUtils.runCommand(sqlCommand);
		DbUtils.runCommand(copyTrips);
		DbUtils.runCommand(copyTAZ);
//		DbUtils.runCommand("psql -c \"\\i ./sql_scripts/load_csv.sql\" postgresql://postgres:matsim@localhost:5432/postgres");
//		DbUtils.runCommand(
//				"psql -c \"\\copy trips FROM 'D:/Users/User/Dropbox/matsim_begin/TripList.csv' DELIMITER ',' CSV HEADER;\" postgresql://postgres:matsim@localhost:5432/postgres");
//		DbUtils.runCommand(
//				"psql -c \"\\copy TAZ_coordinates FROM 'D:/Users/User/Dropbox/matsim_begin/TAZ_coordinates.csv' DELIMITER ',' CSV HEADER;\" postgresql://postgres:matsim@localhost:5432/postgres");
	}

}
