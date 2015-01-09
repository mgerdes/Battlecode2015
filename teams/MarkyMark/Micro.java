package MarkyMark;

import battlecode.common.*;

public class Micro {
    static RobotController rc;

    public static void init(RobotController rcin) {
        rc = rcin;
    }

    public static void doWhatRobotShouldDo() throws GameActionException {
        if (Info.currentTactic == Tactic.NORMAL) {
            doWhatAttackingRobotShouldDo();
        } else if (Info.currentTactic == Tactic.HARASS) {
            doWhatHarassingRobotShouldDo();
        } else if (Info.currentTactic == Tactic.PROVIDE_SUPPLIES) {
            doWhatSupplyProvidersShouldDo();
        }
    }

    // TODO -- add attacking towers and  HQ code.
    public static void doWhatAttackingRobotShouldDo() throws GameActionException {
        /*
            Go to enemy hq.
                if you see enemies then wait till you can kill them.
                when you can kill them, kill them
        */
        if (Info.currentHealth < 20 && Info.type != RobotType.TANK) {
            giveAwaySupplies();
        }
        if (canGoodGuysKillBadGuys(Info.goodGuysICanAttack, Info.badGuysICanAttack)) {
            if (Info.type.canAttack()) {
                Attack.attack();
            }
            Direction movingDirection = Navigation.directionToMoveTo(Info.enemyHQLocation);
            if (Navigation.isSafeToMoveInDirection(movingDirection)) {
                Navigation.moveInDirection(movingDirection);
            } else {
                // Tower attack code.
                if (canGoodGuysKillTower(Info.goodGuysICanAttack)) {
                    Attack.attack(RobotType.TOWER);
                    Navigation.moveInFiringRangeOf(Navigation.closestTower());
                }
            }
        } else {
            Navigation.moveTo(Info.HQLocation);
        }
    }

    // TODO -- prioritize beavers, miners, and buildings when destroying shit.
    public static void doWhatHarassingRobotShouldDo() throws GameActionException {
        // Sneak over to enemy, destroy some shit.
        Attack.attack();
        Navigation.moveTo(Info.enemyHQLocation);
    }

    // TODO -- this is not great.
    public static void doWhatSupplyProvidersShouldDo() throws GameActionException {
        if (rc.getSupplyLevel() < 1000) {
            Navigation.moveTo(Info.HQLocation);
        } else {
            giveAwaySupplies();
            Navigation.moveTo(Info.enemyHQLocation);
        }
    }

    public static boolean canGoodGuysKillTower(RobotInfo[] goodGuys) {
        int rank = 0;
        for (RobotInfo goodGuy : goodGuys) {
            rank += goodGuy.health * goodGuy.type.attackPower + (goodGuy.supplyLevel > 0 ? 500 : 0);
        }
        return rank > 8000;
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

    // TODO -- maybe dont put all your eggs in one basket
    public static void giveAwaySupplies() throws GameActionException {
        if (Info.goodGuysICanSee.length > 0) {
            int mostDeserving = 0;
            MapLocation mostDeservingRobotLocation = null;
            for (RobotInfo robot : Info.goodGuysICanSee) {
                MapLocation robotLocation = robot.location;
                if (!robot.type.isBuilding) {
                    if (robotLocation.distanceSquaredTo(Info.currentLocation) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                    int currentDeservingLevel = (int) (robot.health * robot.type.attackPower);
                    if (currentDeservingLevel > mostDeserving) {
                        mostDeserving = currentDeservingLevel;
                        mostDeservingRobotLocation = robot.location;
                    }
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
