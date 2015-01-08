package MarkyMark;

import battlecode.common.*;

public class Micro {
    static RobotController rc;
    static MapLocation rallyPoint;

    public static void init(RobotController rcin) {
        rc = rcin;
    }

    public static boolean inFiringRangeOf(RobotInfo robot) {
        int distToRobot = Info.currentLocation.distanceSquaredTo(robot.location);
        return robot.type.attackRadiusSquared > distToRobot;
    }

    public static boolean canGoodGuysKillBadGuys(RobotInfo[] goodGuys, RobotInfo[] badGuys) {
        int goodGuyStrength = 0;
        int badGuyStrength = 0;
        for (RobotInfo goodGuy : goodGuys) {
            goodGuyStrength += goodGuy.type.attackPower;
        }
        for (RobotInfo badGuy : badGuys) {
            badGuyStrength += badGuy.type.attackPower;
        }
        rc.setIndicatorString(0, "gg-str=" + goodGuyStrength + "bg-str=" + badGuyStrength);
        return goodGuyStrength > badGuyStrength;
    }

    public static boolean isNearEnemyTower() {
        for (MapLocation enemyTowerLocation : Info.enemyTowerLocations) {
            if (rc.getLocation().distanceSquaredTo(enemyTowerLocation) < RobotType.TOWER.attackRadiusSquared + 20) {
                return true;
            }
        }
        return false;
    }

    public static void doWhatAttackingRobotShouldDo() throws GameActionException {
        /*
            Go to enemy hq.
                if you see enemies then wait till you can kill them.
                when you can kill them, kill them
        */

        if (Info.badGuysICanSee.length == 0 && !isNearEnemyTower()) {
            Navigation.moveTo(Info.enemyHQLocation);
        } else {
            if (canGoodGuysKillBadGuys(Info.goodGuysICanSee, Info.badGuysICanSee)) {
                Attack.something();
            } else {
                Navigation.moveTo(Info.HQLocation);
            }
        }

    }

    public static void doWhatTowerShouldDo() throws GameActionException {
        Attack.something();
    }

    public static MapLocation getClosestBadGuyLocation() {
        int shortestDistance = Info.currentLocation.distanceSquaredTo(Info.badGuysICanSee[0].location);
        MapLocation closestBadGuyLocation = Info.badGuysICanSee[0].location;
        for (RobotInfo badGuy : Info.badGuysICanSee) {
            int distanceToBadGuy = Info.currentLocation.distanceSquaredTo((badGuy.location));
            if (distanceToBadGuy < shortestDistance) {
                shortestDistance = distanceToBadGuy;
                closestBadGuyLocation = badGuy.location;
            }
        }
        return closestBadGuyLocation;
    }
}
