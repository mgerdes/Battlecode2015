package MarkyMark;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

// TODO -- Clean this up.
public class Navigation {
	static RobotController rc = RobotPlayer.rc;
	static Direction[] directions = Direction.values();
    static Random rand = new Random(rc.getID());
	static MapLocation hqLocation = rc.senseHQLocation();
	static MapLocation enemyHQLocation = rc.senseEnemyHQLocation();
	static boolean inited = false;

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
		if (Clock.getRoundNum() < 600) {
			tryMoveTowards(new MapLocation(hqLocation.x + 2, hqLocation.y + 2));				
		} else if (Clock.getRoundNum() < 700) {
			int halfx, halfy;
			halfx = (hqLocation.x + enemyHQLocation.x) / 2;
			halfy = (hqLocation.y + enemyHQLocation.y) / 2;
			MapLocation half = new MapLocation(halfx, halfy);
			tryMoveTowards(half);
		} else if (rc.getLocation().distanceSquaredTo(enemyHQLocation) > 10) {
			tryMoveTowards(enemyHQLocation);
		}
	}

	public static void moveRandomly() throws GameActionException {
		if (rc.isCoreReady()) {
			if (rc.getLocation().distanceSquaredTo(hqLocation) > 81) {
				tryMoveTowards(hqLocation);
			} else {
				tryMoveInDirection(directions[rand.nextInt(8)]);			
			}
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
