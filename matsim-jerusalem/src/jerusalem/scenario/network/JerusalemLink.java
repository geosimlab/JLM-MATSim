package jerusalem.scenario.network;

import java.util.HashSet;
import java.util.Set;

public class JerusalemLink
{
	protected String fromId;
	protected String toId;
	protected double length;
	protected Set<String> mode;
	protected double laneNum;
	protected int roadType;
	protected double capacity;
	protected double freeSpeed;
	
	
	public JerusalemLink(String[] lineArr)
	{
		super();
		this.fromId = lineArr[0];;
		this.toId = lineArr[1];
		this.length = Double.parseDouble(lineArr[2]);
		
		//	All If cases below will be removed in the future there is a bug with the input files
		
		//	case only 1 modes
		if(lineArr.length == 9) {
		
			String unparsedMode = lineArr[3];
			this.mode = parseMode(unparsedMode);
			this.laneNum = Double.parseDouble(lineArr[4]);
			this.roadType = Integer.parseInt(lineArr[5]);
			this.capacity = Double.parseDouble(lineArr[7]);
			this.freeSpeed = Double.parseDouble(lineArr[8]);
		}
		
		//	case 2 modes
		if(lineArr.length == 10) {
			String unparsedMode = lineArr[3]+","+lineArr[4];
			this.mode = parseMode(unparsedMode);
			this.laneNum = Double.parseDouble(lineArr[5]);
			this.roadType = Integer.parseInt(lineArr[6]);
			this.capacity = Double.parseDouble(lineArr[8]);
			this.freeSpeed = Double.parseDouble(lineArr[9]);
		}
		
		//	case 2 modes
		if(lineArr.length == 11) {
			String unparsedMode = lineArr[3]+","+lineArr[4]+","+lineArr[5];
			this.mode = parseMode(unparsedMode);
			this.laneNum = Double.parseDouble(lineArr[6]);
			this.roadType = Integer.parseInt(lineArr[7]);
			this.capacity = Double.parseDouble(lineArr[9]);
			this.freeSpeed = Double.parseDouble(lineArr[10]);
		}

	}
	public String getFromId()
	{
		return fromId;
	}
	public void setFromId(String fromId)
	{
		this.fromId = fromId;
	}
	public String getToId()
	{
		return toId;
	}
	public void setToId(String toId)
	{
		this.toId = toId;
	}
	public double getLength()
	{
		return length;
	}
	public void setLength(double length)
	{
		this.length = length;
	}
	public Set<String> getMode()
	{
		return mode;
	}
	public void setMode(Set<String> mode)
	{
		this.mode = mode;
	}
	public double getLaneNum()
	{
		return laneNum;
	}
	public void setLaneNum(double laneNum)
	{
		this.laneNum = laneNum;
	}
	public int getRoadType()
	{
		return roadType;
	}
	public void setRoadType(int roadType)
	{
		this.roadType = roadType;
	}
	public double getCapacity()
	{
		return capacity;
	}
	public void setCapacity(double capacity)
	{
		this.capacity = capacity;
	}
	public double getFreeSpeed()
	{
		return freeSpeed;
	}
	public void setFreeSpeed(double freeSpeed)
	{
		this.freeSpeed = freeSpeed;
	}
	
	/**
	 * Reads a string of <b>mode</b> and parse the modes and return a set with correct names
	 * <p>
	 * mode: 
	 * <li>
	 *  case 0: [Mode(b), Mode(w)] = "pt, bus, walk"
	 *	<li>
	 *  case 1: [Mode(b)] = "pt, bus"
	 *	<li>
	 *  case 2: [Mode(c), Mode(b), Mode(w)] = "pt, bus, car, walk"
		<li>
	 *  case 3: [Mode(c), Mode(b)] = pt, bus, car
	 *  <li>
	 *  case 4: [Mode(c), Mode(w)] = "car,walk"
	 *  <li>
	 *  case 5: [Mode(c)] = "car"
	 *  <li>
	 *  case 6: [Mode(l)] = "pt, lrt"
	 *  <li>
	 *  case 7: [Mode(r)] = "pt, train"
	 *  <li>
	 	<br>
	 *	<br>
	 * @param Mode from an unparsed String"
	 * @return set <String>
	 **/
	public static Set <String> parseMode(String mode){
		
		Set modeSet = new HashSet<>();
		if(mode.equals("\"[Mode(b), Mode(w)]\"")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("walk");
		}
		if(mode.equals("[Mode(b)]")) {
			modeSet.add("pt");
			modeSet.add("bus");
		}
		if(mode.equals("\"[Mode(c), Mode(b), Mode(w)]\"")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("car");
			modeSet.add("walk");
		}
		if(mode.equals("\"[Mode(c), Mode(b)]\"")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("car");
		}
		if(mode.equals("\"[Mode(c), Mode(w)]\"")) {
			modeSet.add("car");
			modeSet.add("walk");
		}
		if(mode.equals("[Mode(c)]")) {
			modeSet.add("car");
		}
		if(mode.equals("[Mode(l)]")) {
			modeSet.add("pt");
			modeSet.add("lrt");
		}
		if(mode.equals("[Mode(r)]")) {
			modeSet.add("pt");
			modeSet.add("train");
		}
		if(mode.equals("[Mode(w)]")) {
			modeSet.add("walk");
		}
		return modeSet;
		
	}

}
