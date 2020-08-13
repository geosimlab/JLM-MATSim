package jerusalem.calibration;

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

import jerusalem.scenario.DbInitialize;

public class CreateCounts
{
	private final static String COUNTS_ID = "1";
	public static void main(String[] args) throws SQLException
	{
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		String query = "select * from (select cid,linkid \r\n" + 
				"from (\r\n" + 
				"select cid,linkid ,ROW_NUMBER() OVER (PARTITION BY linkid ORDER BY cid DESC) AS RowNo \r\n" + 
				"from counts) x \r\n" + 
				"where RowNo = 1) y  "
				+ "left join counts_data "
				+ "using(cid,linkid) "
				+ "where link_id is not null "
				+ "order by count_year,cid,link_id,hour_of_count;";
		ResultSet resultSet = statement.executeQuery(query);
		Counts counts = new Counts();
		int year = 2020;
		counts.setYear(year);
		counts.setName("JTMT counts data");
		String link_id = "0";
		int cid = 0;
		Count<Link> count = null;
		while (resultSet.next()) {
			if(cid != resultSet.getInt("cid") | !link_id.equals(resultSet.getString("link_id"))) {
				link_id = resultSet.getString("link_id");
				cid = resultSet.getInt("cid");
				Id<Link> linkId = Id.create(link_id, Link.class);
				String stationName = link_id + "_"+cid+"_" + resultSet.getDouble("count_year");
				count = counts.createAndAddCount(linkId, stationName);
			}
			int h  = resultSet.getInt("hour_of_count");
			double val = resultSet.getDouble("yaram");
			count.createVolume(h, val);
		}
		new CountsWriter(counts).write("D:/matsim_jlm/output/"+COUNTS_ID + ".counts.xml.gz");
	}

}
