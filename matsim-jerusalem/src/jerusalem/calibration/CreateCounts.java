package jerusalem.calibration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.counts.Count;
import org.matsim.counts.Counts;
import org.matsim.counts.CountsWriter;

import jerusalem.scenario.db.DbInitialize;

public class CreateCounts
{
	private final static String COUNTS_ID = "8";
	public static void main(String[] args) throws SQLException, IOException
	{

//		create db connection
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//		read sql manipulation file 
		String own_path = System.getProperty("user.dir").replace("\\", "/");
		String path = own_path + "/sql_scripts/create_counts_v"+COUNTS_ID+".sql";
		String query = Files.readString(Paths.get(path), StandardCharsets.US_ASCII);
		ResultSet resultSet = statement.executeQuery(query);
//		create counts
		Counts counts = new Counts();
//		it is possible to input only one year to a counts file. we use 2015, the base year of the model 
		int year = 2015;
		counts.setYear(year);
		counts.setName("JTMT counts data");
//		the first row of the sql script contains the description of the counts
		counts.setDescription(query.split("\n")[0]);
		String link_id = "0";
		Count<Link> count = null;
		while (resultSet.next()) {
//			sql output is order by link and hour. if different link is found - create new link
			if(!link_id.equals(resultSet.getString("link_id"))) {
				link_id = resultSet.getString("link_id");
				Id<Link> linkId = Id.create(link_id, Link.class);
				String stationName = link_id ;
				count = counts.createAndAddCount(linkId, stationName);
			}
//			add hour and count to station
			int h  = resultSet.getInt("hour_of_count");
			double val = resultSet.getDouble("yaram");
			count.createVolume(h, val);
		}
		new CountsWriter(counts).write("D:/matsim_jlm/output/"+COUNTS_ID + ".counts.xml.gz");
	}

}
