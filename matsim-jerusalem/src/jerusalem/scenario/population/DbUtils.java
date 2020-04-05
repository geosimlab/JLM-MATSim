package jerusalem.scenario.population;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbUtils {
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

	public static void runCommand(String commands) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commands);

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
			System.out.println(s);
		}
	}
}