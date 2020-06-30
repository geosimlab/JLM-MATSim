package jerusalem.scenario;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class DbNew {

	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
		// TODO finish ogr2ogr and sql script execution
		Properties props = DbUtils.readProperties("database.properties");
		String db_url = props.getProperty("db.url");
		String port = props.getProperty("db.port");
		String db_name = props.getProperty("db.db_name");
		String url = "jdbc:postgresql://" + db_url + ":" + port + "/" + db_name + "?loggerLevel=DEBUG";
		String own_path = System.getProperty("user.dir").replace("\\", "/");
		Connection con = DriverManager.getConnection(url, props.getProperty("db.username"),
				props.getProperty("db.password"));
		long rowsInserted = new CopyManager((BaseConnection) con).copyIn(
				"COPY headway_periods FROM STDIN (FORMAT csv, HEADER)",
				new BufferedReader(new FileReader(own_path + "/data/headway_periods.csv")));
		System.out.printf("%d row(s) inserted%n", rowsInserted);
	}

}
