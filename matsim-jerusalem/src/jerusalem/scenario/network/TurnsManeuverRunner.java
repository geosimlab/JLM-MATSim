package jerusalem.scenario.network;

import java.sql.SQLException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;

public class TurnsManeuverRunner
{
	public static void main(String[] args) throws Exception {
//		not usable yet
//		https://github.com/idshklein/Storage/blob/master/java/playgrounds/toronto/src/main/java/playground/toronto/maneuvers/ManeuverCreation.java
		String emmeFilePath = "D:/matsim_jlm/input/misc/EmmeManeuverRestrictions.csv";
		String networkFilePath ="D:/matsim_jlm/output/11.network.xml.gz";
		NetworkAddEmmeManeuverRestrictions test = new NetworkAddEmmeManeuverRestrictions(emmeFilePath);
		Network network = NetworkUtils.readNetwork(networkFilePath);
		test.run(network);
		new NetworkWriter(network).write("D:/matsim_jlm/output/12.network.xml.gz");
	}
}
