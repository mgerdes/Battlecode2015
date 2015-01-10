package soldiersAgainstBashers;

import battlecode.common.*;

import java.util.Random;

public class Navigation {
	static RobotController rc;
	static Direction[] directions;
	static Random random;

	public static void init(RobotController rcC) {
		rc = rcC;

		directions = Direction.values();
		random = new Random(rc.getID());
	}

	public static Direction getRandomDirection() {
		return directions[random.nextInt(8)];
	}

	public static Direction getDirectionAwayFromTeam(Team team, MapLocation currentLocation) {
		RobotInfo[] teammates = rc.senseNearbyRobots(4, team);
		boolean[] directionArray = new boolean[8];
		for (RobotInfo teammate : teammates) {
			directionArray[currentLocation.directionTo(teammate.location).ordinal()] = true;
		}

		int[] range = findLongestSequenceOf(false, directionArray);
		if (range[1] == 0) {
			return Direction.NONE;
		}
		else if (range[1] == 8) {
			return Direction.OMNI;
		}

		//--Return middle of Range
		return directions[(range[0] + range[1] / 2) % 8];
	}

	//--Returns ["begin","length"]
	private static int[] findLongestSequenceOf(boolean value, boolean[] range) {
		int maxStart = 0;
		int maxLength = 0;
		for (int i = 0; i < range.length; i++) {
			if (range[i] == value) {
				int start = i;
				int length = 1;
				while (range[++i % range.length] == value
						&& i < range.length * 2) {
					length++;
				}

				if (length > maxLength) {
					maxLength = length;
					maxStart = start;
				}
			}
		}

		if (maxLength > range.length) {
			maxLength = range.length;
		}

		return new int[] {maxStart, maxLength};
	}
}
