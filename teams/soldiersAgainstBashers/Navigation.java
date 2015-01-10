package soldiersAgainstBashers;

import battlecode.common.*;

import java.util.Random;

public class Navigation {
	static RobotController rc = RobotPlayer.rc;
	static Direction[] directions = Direction.values();
    static Random rand = new Random(rc.getID());

	public static void tryMoveTowards(MapLocation destination) throws GameActionException {
		MapLocation currentLocation = rc.getLocation();
		if (currentLocation.equals(destination)) {
			return;
		}
		if (rc.isCoreReady()) {
			Direction dir = currentLocation.directionTo(destination);
			tryMoveInDirection(dir);
		}
	}

	public static void move() throws GameActionException {
		if (Clock.getRoundNum() < 1200) {
			moveRandomly();				
		} else {
			MapLocation curLoc = rc.getLocation();
			MapLocation[] towers = rc.senseEnemyTowerLocations();
			int shortest = Integer.MAX_VALUE;
			MapLocation shortestloc = curLoc;
			for (MapLocation towerloc : towers) {
				int distance = curLoc.distanceSquaredTo(towerloc);
				if (distance < shortest) {
					shortestloc = towerloc;
				}
			}
			tryMoveTowards(shortestloc);
		}
	}

	public static Direction getDirectionAwayFromTeam(Team team) {
		MapLocation currentLocation = rc.getLocation();
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

	public static void moveRandomly() throws GameActionException {
		if (rc.isCoreReady()) {
			tryMoveInDirection(directions[rand.nextInt(8)]);
		}
	}

	public static Direction randomDirection() throws GameActionException {
		return directions[rand.nextInt(8)];
	}

	public static void tryMoveInDirection(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}

	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
}
