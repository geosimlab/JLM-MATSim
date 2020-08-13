package jerusalem.scenario.archive;

/**
 * @author Ido Klein
 */
public class PopUtils {

//	based on JTMT-3C ABM Main Output File Structure
	/**
	 * Returns description of activity type from integer, according to the JTMT-3C
	 * ABM Main Output File Structure (Trip/tour purposes:)
	 * 
	 * @param int <b>i</b>
	 * @return String actResult
	 */
	public static String ActivityType(int activity) {
		String actResult = null;
		switch (activity) {
		case 0:// 0. Home
			actResult = "home";
			break;
		case 1:// 1. Workplace
			actResult = "work";
			break;
		case 2:// 2. University
		case 3:// 3. School
			actResult = "school";
			break;
		case 5:// 5. Shopping
		case 7:// 7. Eating out
		case 71:// 7.1. Breakfast
		case 72:// 7.2. Lunch
		case 73:// 7.3. Dinner
		case 8:// 8. Visiting relatives or friends
			actResult = "leisure";
			break;
		default:// 4. Escorting
			// 4.1. School escort
			// 4.1.1. Pure escort (as main purpose of the tour)
			// 4.1.2. Ridesharing (as stop on commuting tours)
			// 4.2. Other escort
			// 6. Other maintenance
			// 6.3. Medical Maintenance
			// 6.4. Administrative Maintenance
			// 9. Other discretionary
			// 91. Religious
			// 15. Work-related business
			actResult = "other";
			break;
		}
		return actResult;
	}

	/**
	 * Returns description of mode from integer, according to the JTMT-3C ABM Main
	 * Output File Structure (Trip/tour modes (disaggregated files))
	 * 
	 * @param int <b>i</b>
	 * @return String actmodeResult
	 */
	public static String Mode(int i) {
		String modeResult = null;
		switch (i) {
		case 1:// 1. SOV
		case 2:// 2. HOV2/driver
		case 3:// 3. HOV3+/driver
			modeResult = "car";
			break;
		case 4:// 4. HOV/passenger (not assigned)
			modeResult = "ride";
			break;
		case 5:// 5. Bus walk access
		case 6:// 6. Bus KNR
		case 7:// 7. Bus PNR
		case 8:// 8. LRT walk access (can be further divided by transit mode)
		case 9:// 9. LRT KNR (can be further divided by mode)
		case 10:// 10. LRT PNR (can be further divided by mode)
		case 11:// 11. Rail walk access (can be further divided by transit mode)
		case 12:// 12. Rail KNR (can be further divided by mode)
		case 13:// 13. Rail PNR (can be further divided by mode)
		case 17:// 17. School bus (not assigned)
			modeResult = "pt";
			break;
		case 14:// 14. Walk (not assigned)
			modeResult = "walk";
			break;
		case 15:// 15. Bike (not assigned)
			modeResult = "bike";
			break;
		case 16:// 16. Taxi (can be added to HOV3/driver for assignment)
			modeResult = "car";
//			modeResult = "taxi";
			break;
		default:
			modeResult = "other";
			break;
		}
		return modeResult;
	}

	/**
	 * Returns description of Employed from integer, according to the JTMT-3C ABM
	 * Main Output File Structure (Table 1: Person types - disaggregate)
	 * 
	 * @param int <b>i</b>
	 * @return boolean employedResult
	 */
	public static Boolean Employed(int i) {
		boolean employedResult;
		if (i <= 6 | i == 10 | i == 11 | i == 16) {

			// 1, "ft-worker, 19+"
			// 3, "ft-worker, student"
			// 4, "pt-worker, 19+"
			// 6, "pt-worker, student"
			// 10, "univ student, Major univ, worker, on-campus"
			// 11, "univ student, Major univ, worker, off-campus"
			// 16, "driving age, worker, 14-18"
			employedResult = true;
		} else {

			// 8, "univ student, Major univ, non-worker"
			// 9, "univ student, OTHER, non-worker"
			// 13, "non-worker homemaker, 19-64"
			// 15, "retired, 65+"
			// 17, "driving age, non-worker, 14-18"
			// 19, "pre-driving age, 7-13"
			// 21, "pre-school, 0-6"
			employedResult = false;
		}
		return employedResult;
	}

	/**
	 * Returns description of sector from integer, according to the JTMT-3C ABM Main
	 * Output File Structure (households table row 6)
	 * 
	 * @param int <b>i</b>
	 * @return String actmodeResult
	 */
	public static String Sector(int i) {
		String sectorResult = null;
		switch (i) {
		case 1:
			sectorResult = "Arab";
			break;
		case 2:
			sectorResult = "Ultra-Orthodox";
			break;
		case 3:
			sectorResult = "Secular";
			break;
		case 4:
			sectorResult = "Palestine";
			break;
		}
		return sectorResult;
	}

	/**
	 * Returns description of sector from integer, according to the JTMT-3C ABM Main
	 * Output File Structure (households table, persons table)
	 * 
	 * @param numAuto
	 * @param usualDriver
	 * @return String carAvailResult
	 */
	public static String CarAvail(int numAuto, int usualDriver) {
		// numAuto is number of cars in household, usualDriver is whether person is
		// usual driver. switch statement was reduced
		String carAvailResult = null;
		switch (usualDriver * 10 + numAuto) {
		case 0:
		case 1:
		case 10:
			carAvailResult = "never";
			break;
		case 2:
		case 3:
		case 11:
			carAvailResult = "sometimes";
			break;
		case 12:
		case 13:
			carAvailResult = "always";
			break;
		}
		return carAvailResult;
	}
}
