package jerusalem.scenario.population;

import java.util.ArrayList;

import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacility;

public class TazFacilities
{
	protected ArrayList<Id<ActivityFacility>> householdsEmpty = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> householdsFull = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesWork = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesSchool = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesUniversity = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesKindergarden = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesElementary = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesJuniorHigh = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesHighSchool = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesReligionJewish = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesReligionArab = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesleisure = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesOther = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesTjlm = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesFjlm = new ArrayList<Id<ActivityFacility>>();

	public TazFacilities()
	{

	}

	/**
	 * Method to get all facilities from a certain type (beside housing) in a taz  
	 * @param listName
	 * @return ArrayList<Id<ActivityFacility>>
	 */
	public ArrayList<Id<ActivityFacility>> getAList(String listName)
	{
		ArrayList<Id<ActivityFacility>> result = null;
		switch (listName)
		{
		case "work":
			result = amenitiesWork;
			break;
		case "university":
			result = amenitiesUniversity;
			break;
		case "school":
			result = amenitiesSchool;
			break;
		case "high_school":
			result = amenitiesHighSchool;
			break;
		case "junior_high":
			result = amenitiesJuniorHigh;
			break;
		case "elementary":
			result = amenitiesElementary;
			break;
		case "kindergarden":
			result = amenitiesKindergarden;
			break;
		case "leisure":
			result = amenitiesleisure;
			break;
		case "religion_jewish":
			result = amenitiesReligionJewish;
			break;
		case "religion_arab":
			result = amenitiesReligionArab;
			break;
		case "other":
			result = amenitiesOther;
			break;
		case "tjlm":
			result = amenitiesTjlm;
			break;
		case "fjlm":
			result = amenitiesFjlm;
			break;
		default:
			break;
		}
		return result;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesUniversity()
	{
		return amenitiesUniversity;
	}

	public void setAmenitiesUniversity(ArrayList<Id<ActivityFacility>> amenitiesUniversity)
	{
		this.amenitiesUniversity = amenitiesUniversity;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesKindergarden()
	{
		return amenitiesKindergarden;
	}

	public void setAmenitiesKindergarden(ArrayList<Id<ActivityFacility>> amenitiesKindergarden)
	{
		this.amenitiesKindergarden = amenitiesKindergarden;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesElementary()
	{
		return amenitiesElementary;
	}

	public void setAmenitiesElementary(ArrayList<Id<ActivityFacility>> amenitiesElementary)
	{
		this.amenitiesElementary = amenitiesElementary;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesJuniorHigh()
	{
		return amenitiesJuniorHigh;
	}

	public void setAmenitiesJuniorHigh(ArrayList<Id<ActivityFacility>> amenitiesJuniorHigh)
	{
		this.amenitiesJuniorHigh = amenitiesJuniorHigh;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesHighSchool()
	{
		return amenitiesHighSchool;
	}

	public void setAmenitiesHighSchool(ArrayList<Id<ActivityFacility>> amenitiesHighSchool)
	{
		this.amenitiesHighSchool = amenitiesHighSchool;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesReligionJewish()
	{
		return amenitiesReligionJewish;
	}

	public void setAmenitiesReligionJewish(ArrayList<Id<ActivityFacility>> amenitiesReligionJewish)
	{
		this.amenitiesReligionJewish = amenitiesReligionJewish;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesReligionArab()
	{
		return amenitiesReligionArab;
	}

	public void setAmenitiesReligionArab(ArrayList<Id<ActivityFacility>> amenitiesReligionArab)
	{
		this.amenitiesReligionArab = amenitiesReligionArab;
	}

	public ArrayList<Id<ActivityFacility>> getHouseholdsEmpty()
	{
		return householdsEmpty;
	}

	public void setHouseholdsEmpty(ArrayList<Id<ActivityFacility>> householdsEmpty)
	{
		this.householdsEmpty = householdsEmpty;
	}

	public ArrayList<Id<ActivityFacility>> getHouseholdsFull()
	{
		return householdsFull;
	}

	public void setHouseholdsFull(ArrayList<Id<ActivityFacility>> householdsFull)
	{
		this.householdsFull = householdsFull;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesWork()
	{
		return amenitiesWork;
	}

	public void setAmenitiesWork(ArrayList<Id<ActivityFacility>> amenitiesWork)
	{
		this.amenitiesWork = amenitiesWork;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesSchool()
	{
		return amenitiesSchool;
	}

	public void setAmenitiesSchool(ArrayList<Id<ActivityFacility>> amenitiesSchool)
	{
		this.amenitiesSchool = amenitiesSchool;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesleisure()
	{
		return amenitiesleisure;
	}

	public void setAmenitiesleisure(ArrayList<Id<ActivityFacility>> amenitiesleisure)
	{
		this.amenitiesleisure = amenitiesleisure;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesOther()
	{
		return amenitiesOther;
	}

	public void setAmenitiesOther(ArrayList<Id<ActivityFacility>> amenitiesOther)
	{
		this.amenitiesOther = amenitiesOther;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesTjlm()
	{
		return amenitiesTjlm;
	}

	public void setAmenitiesTjlm(ArrayList<Id<ActivityFacility>> amenitiesTjlm)
	{
		this.amenitiesTjlm = amenitiesTjlm;
	}

	public ArrayList<Id<ActivityFacility>> getAmenitiesFjlm()
	{
		return amenitiesFjlm;
	}

	public void setAmenitiesFjlm(ArrayList<Id<ActivityFacility>> amenitiesFjlm)
	{
		this.amenitiesFjlm = amenitiesFjlm;
	}

	/**
	 * Method to add a facility to a specific taz group of activity facilities
	 * @param facilityId
	 * @param array
	 */
	public void addToList(Id<ActivityFacility> facilityId, String array)
	{
		switch (array)
		{
		case "empty":
			this.householdsEmpty.add(facilityId);
			break;
		case "full":
			this.householdsFull.add(facilityId);
			break;
		case "work":
			this.amenitiesWork.add(facilityId);
			break;
		case "school":
			this.amenitiesSchool.add(facilityId);
			break;
		case "university":
			this.amenitiesUniversity.add(facilityId);
			break;
		case "high_school":
			this.amenitiesHighSchool.add(facilityId);
			break;
		case "junior_high":
			this.amenitiesJuniorHigh.add(facilityId);
			break;
		case "elementary":
			this.amenitiesElementary.add(facilityId);
			break;
		case "kindergarden":
			this.amenitiesKindergarden.add(facilityId);
			break;
		case "leisure":
			this.amenitiesleisure.add(facilityId);
			break;
		case "religion_jewish":
			this.amenitiesReligionJewish.add(facilityId);
			break;
		case "religion_arab":
			this.amenitiesReligionArab.add(facilityId);
			break;
		case "other":
			this.amenitiesOther.add(facilityId);
			break;
		case "tjlm":
			this.amenitiesTjlm.add(facilityId);
			break;
		case "fjlm":
			this.amenitiesFjlm.add(facilityId);
			break;
		default:
			break;
		}
	}

	/**
	 * Method to remove a facility to a specific taz group of activity facilities
	 * @param facilityId
	 * @param array
	 */
	public void removeFromList(Id<ActivityFacility> facilityId, String array)
	{
		switch (array)
		{
		case "empty":
			this.householdsEmpty.remove(facilityId);
			break;
		case "full":
			this.householdsFull.remove(facilityId);
			break;
		case "work":
			this.amenitiesWork.remove(facilityId);
			break;
		case "school":
			this.amenitiesSchool.remove(facilityId);
			break;
		case "university":
			this.amenitiesUniversity.remove(facilityId);
			break;
		case "high_school":
			this.amenitiesHighSchool.remove(facilityId);
			break;
		case "junior_high":
			this.amenitiesJuniorHigh.remove(facilityId);
			break;
		case "elementary":
			this.amenitiesElementary.remove(facilityId);
			break;
		case "kindergarden":
			this.amenitiesKindergarden.remove(facilityId);
			break;
		case "leisure":
			this.amenitiesleisure.remove(facilityId);
			break;
		case "religion_jewish":
			this.amenitiesReligionJewish.remove(facilityId);
			break;
		case "religion_arab":
			this.amenitiesReligionArab.remove(facilityId);
		case "other":
			this.amenitiesOther.remove(facilityId);
			break;
		case "tjlm":
			this.amenitiesTjlm.remove(facilityId);
			break;
		case "fjlm":
			this.amenitiesFjlm.remove(facilityId);
			break;
		default:
			break;
		}
	}

}
