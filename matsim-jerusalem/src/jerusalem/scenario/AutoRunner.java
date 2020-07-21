package jerusalem.scenario;

import java.sql.SQLException;

import jerusalem.scenario.network.CreateNetwork;
import jerusalem.scenario.test.HouseholdPlayground;

public class AutoRunner
{

	public static void main(String[] args) throws SQLException
	{
//		add arguments
		
		String[] min_storage_length = {"80.0","100.0","120.0","140.0","160.0","180.0","200.0"};
		String[] storage_length_decay = {"1.0","1.5","2.0","2.5","3"};
		String min_flow_capacity = "500";
		String[] input = new String[3];
		for(int i = 0; i < min_storage_length.length;i++) {
			for(int j = 0; j< storage_length_decay.length; j++) {
				input[0] = min_storage_length[i];
				input[1] = storage_length_decay[j];
				input[2] = min_flow_capacity;
				HouseholdPlayground.main(input);
				RunJerusalem.main(null);
			}
		}

	}

}
