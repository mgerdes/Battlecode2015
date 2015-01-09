package MarkyMark;

import battlecode.common.*;

// TODO -- add attacking towers and  HQ code.
public class Micro {
    static RobotController rc;

    public static void init(RobotController rcin) {
        rc = rcin;
    }

    public static void doWhatAttackingRobotShouldDo() throws GameActionException {
        /*
            Go to enemy hq.
                if you see enemies then wait till you can kill them.
                when you can kill them, kill them
        */
        if (Info.currentHealth < 20 && Info.type != RobotType.TANK) {
            giveAwaySupplies();
        }
        if (canGoodGuysKillBadGuys(Info.goodGuysICanSee, Info.badGuysICanSee)) {
            if (Info.type.canAttack()) {
                Attack.attack();
            }
            Direction movingDirection = Navigation.directionToMoveTo(Info.enemyHQLocation);
            if (isSafeToMoveInDirection(movingDirection)) {
                Navigation.moveInDirection(movingDirection);
            }
        } else {
            Navigation.moveTo(Info.HQLocation);
        }
    }

    // TODO - base this more on who can actually shot who? i dont know if that makes sense.
    public static boolean canGoodGuysKillBadGuys(RobotInfo[] goodGuys, RobotInfo[] badGuys) {
        int goodGuyStrength = 0;
        int badGuyStrength = 0;
        for (RobotInfo goodGuy : goodGuys) {
            if (goodGuy.type != RobotType.TOWER) {
                goodGuyStrength += goodGuy.type.attackPower * goodGuy.health;
            }
        }
        for (RobotInfo badGuy : badGuys) {
            if (badGuy.type != RobotType.TOWER) {
                badGuyStrength += badGuy.type.attackPower * badGuy.health;
            }
        }
        rc.setIndicatorString(0, "gg-str=" + goodGuyStrength + "bg-str=" + badGuyStrength);
        return goodGuyStrength >= badGuyStrength;
    }

    public static boolean isSafeToMoveInDirection(Direction direction) {
        MapLocation nextLocation = Info.currentLocation.add(direction);
        RobotInfo[] badGuysAround = rc.senseNearbyRobots(nextLocation, 20, Info.badGuys);
        RobotInfo[] goodGuysAround = rc.senseNearbyRobots(nextLocation, 20, Info.goodGuys);
        return canGoodGuysKillBadGuys(goodGuysAround, badGuysAround) && !isNearEnemyTower() && !isNearEnemyHQ();
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

    public static void giveAwaySupplies() throws GameActionException {
        if (Info.goodGuysICanSee.length > 0) {
            int mostDeserving = 0;
            MapLocation mostDeservingRobotLocation = null;
            for (RobotInfo robot : Info.goodGuysICanSee) {
                MapLocation robotLocation = robot.location;
                if (robotLocation.distanceSquaredTo(Info.currentLocation) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                    int currentDeservingLevel = (int) (robot.health * robot.type.attackPower);
                    if (currentDeservingLevel > mostDeserving) {
                        mostDeserving = currentDeservingLevel;
                        mostDeservingRobotLocation = robot.location;
                    }
                }
            }
            if (mostDeservingRobotLocation != null)
                rc.transferSupplies((int) Info.supplyLevel, mostDeservingRobotLocation);
        }
    }

    public static void doWhatTowerShouldDo() throws GameActionException {
        Attack.attack();
    }
}
