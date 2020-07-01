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
		String path = "D:/Users/User/git/JLM-MATSim/matsim-jerusalem/sql_scripts/cascade_rounding.sql";
		Connection con = DriverManager.getConnection(url, props.getProperty("db.username"),
				props.getProperty("db.password"));
//		copyTableFromCSV(path, con);
//		executeSQLFromFile(path, con);
//use old oge2oge approach
	}

	/**
	 * @param path
	 * @param con
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void executeSQLFromFile(String path, Connection con) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			Object[] lines = br.lines().toArray();
			String result = "";
			for (Object i : lines) {
				result = result + (String) i + "\n";
			}
//			System.out.println(result);
			con.createStatement().execute(result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

	}

	/**
	 * @param path path to csv
	 * @param con  Connection to database
	 * @throws SQLException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyTableFromCSV(String path, Connection con)
			throws SQLException, IOException, FileNotFoundException {
		long rowsInserted = new CopyManager((BaseConnection) con).copyIn(
				"COPY headway_periods FROM STDIN (FORMAT csv, HEADER)", new BufferedReader(new FileReader(path)));
		String tableName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
		System.out.printf("%d row(s) inserted from " + tableName, rowsInserted);
	}

}
