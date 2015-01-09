package MarkyMark;

import battlecode.common.*;

// TODO -- unsure about a lot of stuff in here.
public class Navigation {
	static RobotController rc;
	static MapLocation currentDestination;

	public static void init(RobotController rcin) throws GameActionException {
		Bug.init(rcin);
		rc = rcin;
	}

	public static Direction directionToMoveTo(MapLocation destination) throws GameActionException {
		if (!destination.equals(currentDestination)) {
			currentDestination = destination;
			Bug.beginBugTowards(currentDestination);
			//rc.setIndicatorString(0, "Changing on round: " + Clock.getRoundNum());
		}
		return Bug.getDirection();
	}

	public static void moveTo(MapLocation destination) throws GameActionException {
		if (!destination.equals(currentDestination)) {
			currentDestination = destination;
			Bug.beginBugTowards(currentDestination);
			//rc.setIndicatorString(0, "Changing on round: " + Clock.getRoundNum());
		}
		moveInDirection(Bug.getDirection());
	}

	public static void moveInDirection(Direction d) throws GameActionException {
        if (rc.isCoreReady() && rc.canMove(d)) {
            rc.move(d);
        }
	}

	public static boolean isSafeToMoveInDirection(Direction direction) {
		MapLocation nextLocation = Info.currentLocation.add(direction);
		RobotInfo[] badGuysAround = rc.senseNearbyRobots(nextLocation, 20, Info.badGuys);
		RobotInfo[] goodGuysAround = rc.senseNearbyRobots(nextLocation, 20, Info.goodGuys);
		return Micro.canGoodGuysKillBadGuys(goodGuysAround, badGuysAround) && !isNearEnemyTowerOrHQ();
	}

	public static boolean isNearEnemy(MapLocation location) {
		return Info.badGuysICanSee.length > 0 || isNearEnemyHQ(location);
	}

	public static boolean isNearEnemyTowerOrHQ() {
		return isNearEnemyTowerOrHQ(Info.currentLocation);
	}

	public static boolean isNearEnemyTowerOrHQ(MapLocation location) {
		return isNearEnemyTower(location) || isNearEnemyHQ(location);
	}

	public static boolean isNearEnemyTower() {
		return isNearEnemyTower(Info.currentLocation);
	}

	public static boolean isNearEnemyTower(MapLocation location) {
		for (MapLocation enemyTowerLocation : Info.enemyTowerLocations) {
			if (location.distanceSquaredTo(enemyTowerLocation) < RobotType.TOWER.attackRadiusSquared + 15) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNearEnemyHQ() {
		return isNearEnemyHQ(Info.currentLocation);
	}

	public static boolean isNearEnemyHQ(MapLocation location) {
		return location.distanceSquaredTo(Info.enemyHQLocation) < RobotType.HQ.attackRadiusSquared + 15;
	}

	public static void moveRandomly() throws GameActionException {
		if (rc.isCoreReady()) {
            moveInDirection(randomDirection());
		}
	}

	public static Direction randomDirection() throws GameActionException {
		return Info.directions[Info.rand.nextInt(8)];
	}

	public static boolean okToMove(Direction dir) {
		MapLocation nextLocation = Info.currentLocation.add(dir);
		return (rc.canMove(dir)) &&
				(Info.currentEngagementRules == Engagement.ENGAGE
						|| (Info.currentEngagementRules == Engagement.AVOID && !Navigation.isNearEnemyTowerOrHQ(nextLocation)));
	}

}
