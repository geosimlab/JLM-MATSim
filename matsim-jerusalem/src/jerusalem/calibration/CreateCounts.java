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
	private final static String COUNTS_ID = "3";
	public static void main(String[] args) throws SQLException
	{
		Connection con = DriverManager.getConnection(DbInitialize.url, DbInitialize.username, DbInitialize.password);
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		String query = "select link_id,hour_of_count,avg(yaram) yaram from(  \r\n" + 
				"select link_id,hour_of_count, yaram, split_part(link_id,'_',3) type_r \r\n" + 
				"from counts_data\r\n" + 
				"where (link_id,cid)  in (SELECT link_id,cid \r\n" + 
				"FROM counts_data\r\n" + 
				"GROUP BY\r\n" + 
				"    link_id,cid\r\n" + 
				"   having not (count(*) > 1 and min(yaram) = 0)\r\n" + 
				"  order by link_id,cid) \r\n" + 
				" and count_year =2015) q\r\n" + 
				" where type_r != '10' \r\n" + 
				" group by link_id,hour_of_count;";
		ResultSet resultSet = statement.executeQuery(query);
		Counts counts = new Counts();
		int year = 2015;
		counts.setYear(year);
		counts.setName("JTMT counts data");
		counts.setDescription("Average of all count stations without an hour with zero count of 2015");
		String link_id = "0";
		int cid = 0;
		Count<Link> count = null;
		while (resultSet.next()) {
			if(!link_id.equals(resultSet.getString("link_id"))) {
//				if(cid != resultSet.getInt("cid") | !link_id.equals(resultSet.getString("link_id"))) {
				link_id = resultSet.getString("link_id");
//				cid = resultSet.getInt("cid");
				Id<Link> linkId = Id.create(link_id, Link.class);
//				String stationName = link_id + "_"+cid+"_" + resultSet.getDouble("count_year");
				String stationName = link_id ;
				count = counts.createAndAddCount(linkId, stationName);
			}
			int h  = resultSet.getInt("hour_of_count");
			double val = resultSet.getDouble("yaram");
			count.createVolume(h, val);
		}
		new CountsWriter(counts).write("D:/matsim_jlm/output/"+COUNTS_ID + ".counts.xml.gz");
	}

}
