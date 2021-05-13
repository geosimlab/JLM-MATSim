package jerusalem.scenario.network;

import java.util.HashSet;
import java.util.Set;

public class JerusalemLink {
	protected int fromId;
	protected int toId;
	protected double length;
	protected Set<String> mode;
	protected double laneNum;
	protected double roadType;
	protected int capacity;
	protected double freeSpeed;

	public JerusalemLink() {

	}

	public int getFromId() {
		return fromId;
	}

	public void setFromId(int i) {
		this.fromId = i;
	}

	public int getToId() {
		return toId;
	}

	public void setToId(int i) {
		this.toId = i;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public Set<String> getMode() {
		return mode;
	}

	public void setMode(Set<String> mode) {
		this.mode = mode;
	}

	public double getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(double laneNum) {
		this.laneNum = laneNum;
	}

	public double getRoadType() {
		return roadType;
	}

	public void setRoadType(double d) {
		this.roadType = d;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public double getFreeSpeed() {
		return freeSpeed;
	}

	public void setFreeSpeed(double freeSpeed) {
		this.freeSpeed = freeSpeed;
	}

	/**
	 * Reads a string of <b>mode</b> and parse the modes and return a set with
	 * correct names
	 * <p>
	 * mode:
	 * <li>case 0: [Mode(b), Mode(w)] = "pt, bus, walk"
	 * <li>case 1: [Mode(b)] = "pt, bus"
	 * <li>case 2: [Mode(c), Mode(b), Mode(w)] = "pt, bus, car, walk"
	 * <li>case 3: [Mode(c), Mode(b)] = pt, bus, car
	 * <li>case 4: [Mode(c), Mode(w)] = "car,walk"
	 * <li>case 5: [Mode(c)] = "car"
	 * <li>case 6: [Mode(l)] = "pt, lrt"
	 * <li>case 7: [Mode(r)] = "pt, train"
	 * <li><br>
	 * <br>
	 * 
	 * @param mode from an unparsed String"
	 * @return set of modes for a link
	 **/
	public static Set<String> parseMode(String mode) {

		Set modeSet = new HashSet<>();
		if (mode.equals("[Mode(b), Mode(w)]")||
				mode.equals("[Mode(w), Mode(b)]")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("walk");
		}
		if (mode.equals("[Mode(b)]")) {
			modeSet.add("pt");
			modeSet.add("bus");
		}
		if (mode.equals("[Mode(c), Mode(b), Mode(w)]") || 
				mode.equals("[Mode(c), Mode(w), Mode(b)]")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("car");
			modeSet.add("walk");
		}
		if (mode.equals("[Mode(c), Mode(b)]")) {
			modeSet.add("pt");
			modeSet.add("bus");
			modeSet.add("car");
		}
		if (mode.equals("[Mode(c), Mode(w)]")) {
			modeSet.add("car");
			modeSet.add("walk");
		}
		if (mode.equals("[Mode(c)]")) {
			modeSet.add("car");
		}
		if (mode.equals("[Mode(l)]")) {
			modeSet.add("pt");
			modeSet.add("lrt");
		}
		if (mode.equals("[Mode(r)]")) {
			modeSet.add("pt");
			modeSet.add("train");
		}
		if (mode.equals("[Mode(w)]")) {
			modeSet.add("walk");
		}
		return modeSet;

	}

}
