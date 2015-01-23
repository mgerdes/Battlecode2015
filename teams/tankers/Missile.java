package tankers;

import battlecode.common.*;
import tankers.util.Debug;
import tankers.util.Helper;

public class Missile {
    private static RobotController rc;
    private static Team enemyTeam;

    private static int lastRoundNumber;
    private static Team myTeam;

    public static void run(RobotController rcC) {
        rc = rcC;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        lastRoundNumber = Clock.getRoundNum() + GameConstants.MISSILE_LIFESPAN - 1;

        while (true) {
            try {
                loop();
            } catch (GameActionException e) {
                e.printStackTrace();
            }

            rc.yield();
        }
    }

    private static void loop() throws GameActionException {
        int currentRound = Clock.getRoundNum();

        if (currentRound == lastRoundNumber) {
            //--This is my last turn
            RobotInfo[] friends = rc.senseNearbyRobots(GameConstants.MISSILE_RADIUS_SQUARED, myTeam);
            if (friends.length == 0) {
                Debug.setString(0, "blowing up because it's my last turn.", rc);
                rc.explode();
            }
            else {
                Debug.setString(0, "disintegrating because it's my last turn and friends are near.", rc);
                rc.disintegrate();
            }
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.MISSILE.sensorRadiusSquared, enemyTeam);
        MapLocation currentLocation = rc.getLocation();
        int numberOfNonMissileEnemies = Helper.getRobotsExcludingType(enemies, RobotType.MISSILE);
        if (numberOfNonMissileEnemies == 0) {
            if (!rc.isCoreReady()) {
                return;
            }

            //--Try to move away from friendlies
            Direction awayFromFriends = Helper.getDirectionAwayFrom(
                    rc.senseNearbyRobots(GameConstants.MISSILE_RADIUS_SQUARED, myTeam),
                    currentLocation);
            if (awayFromFriends == Direction.NONE) {
                Debug.setString(0, "no enemies, but could not move " + awayFromFriends, rc);
                return;
            }

            if (rc.canMove(awayFromFriends)) {
                Debug.setString(0, "can't see enemies. moving away from friends.", rc);
                rc.move(awayFromFriends);
                return;
            }

            if (rc.canMove(awayFromFriends.rotateLeft())) {
                Debug.setString(0, "can't see enemies. moving away from friends.", rc);
                rc.move(awayFromFriends.rotateLeft());
                return;
            }

            if (rc.canMove(awayFromFriends.rotateRight())) {
                Debug.setString(0, "can't see enemies. moving away from friends.", rc);
                rc.move(awayFromFriends.rotateRight());
                return;
            }

            Debug.setString(0, "no enemies, but could not move " + awayFromFriends, rc);
            return;
        }

        //--Find the first non-missile enemy
        int index = 0;
        int id = enemies[0].ID;

        int numberOfEnemies = enemies.length;
        for (int i = 1; i < numberOfEnemies; i++) {
            if (enemies[i].type != RobotType.MISSILE) {
                index = i;
                id = enemies[i].ID;
                break;
            }
        }

        //--If I am close enough, explode.
        if (currentLocation.distanceSquaredTo(enemies[index].location) <= GameConstants.MISSILE_RADIUS_SQUARED) {
            rc.explode();
        }

        //--If I am not close enough, try to get closer
        if (rc.isCoreReady()) {
            Direction directionToEnemy = currentLocation.directionTo(enemies[index].location);
            if (rc.canMove(directionToEnemy)) {
                Debug.setString(0, String.format("moving toward the closest enemy, ID:%d", id), rc);
                rc.move(directionToEnemy);
                return;
            }

            if (rc.canMove(directionToEnemy.rotateLeft())) {
                Debug.setString(0, String.format("moving toward the closest enemy, ID:%d", id), rc);
                rc.move(directionToEnemy.rotateLeft());
                return;
            }

            if (rc.canMove(directionToEnemy.rotateRight())) {
                Debug.setString(0, String.format("moving toward the closest enemy, ID:%d", id), rc);
                rc.move(directionToEnemy.rotateRight());
                return;
            }
        }
    }
}
