package jerusalem.scenario.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ido Klein
 */

/**
 * @author User
 *
 */
public class DbUtils {
	/**
	 * Returns properties of DB and files paths from .properties file
	 * 
	 * @param String <b>path</b>
	 * @return Properties props
	 */
	public static Properties readProperties(String path) {

		Properties props = new Properties();
		Path myPath = Paths.get(path);

		try {
			BufferedReader bf = Files.newBufferedReader(myPath, StandardCharsets.UTF_8);

			props.load(bf);
		} catch (IOException ex) {
			Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
		}

		return props;
	}

	/**
	 * Runs commands on local machine. the relevant software should be installed and configured
	 * on local machine. Commands require valid syntax. manily used for ogr2ogr
	 * 
	 * @param String <b>commands</b>
	 */
	public static void runCommand(String command, String dirStr) throws IOException {
		System.out.println("*********************************************************");
		System.out.println("command:" + command);
		String[] commands = { "cmd", "/C", command };// the string is on order to handle with pipes,
		// https://stackoverflow.com/questions/5928225/how-to-make-pipes-work-with-runtime-exec
		File dir = new File(dirStr);
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commands, null, dir);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		// Read the output from the command
		System.out.println("Here is the standard output of the command:\n");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}

		// Read any errors from the attempted command
		System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
			System.out.println("\u001B[31m" + s);

		}
	}
}
