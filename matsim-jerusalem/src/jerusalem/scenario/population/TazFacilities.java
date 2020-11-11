package jerusalem.scenario.population;

import java.util.ArrayList;

import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacility;

public class TazFacilities
{
//	TODO add javadoc
//	TODO maybe write nicer code (public protected none and such)
	protected ArrayList<Id<ActivityFacility>> householdsEmpty = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> householdsFull = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesWork = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesSchool = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesleisure = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesOther = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesTjlm = new ArrayList<Id<ActivityFacility>>();
	protected ArrayList<Id<ActivityFacility>> amenitiesFjlm = new ArrayList<Id<ActivityFacility>>();

	public TazFacilities()
	{

	}

	public ArrayList<Id<ActivityFacility>> getAList(String array)
	{
		ArrayList<Id<ActivityFacility>> result = null;
		switch (array)
		{
		case "work":
			result = amenitiesWork;
			break;
		case "school":
			result = amenitiesSchool;
			break;
		case "leisure":
			result = amenitiesleisure;
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
		case "leisure":
			this.amenitiesleisure.add(facilityId);
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
		case "leisure":
			this.amenitiesleisure.remove(facilityId);
			break;
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
