package MarkyMark;

import battlecode.common.*;
import java.util.*;

enum Tactic {
    NOENGAGE, ENGAGE
}

public class Info {
    public static RobotController rc;
    public static RobotType type;
    public static Random rand;
    public static int sensorRadiusSquared;
    public static int attackRadiusSquared;
    public static Team goodGuys;
    public static Team badGuys;
    public static MapLocation currentLocation;
    public static MapLocation HQLocation;
    public static MapLocation enemyHQLocation;
    public static MapLocation startRallyPoint;
    public static MapLocation[] enemyTowerLocations;
    public static int numberOfEnemyTowers;
    public static RobotInfo[] goodGuysICanAttack;
    public static RobotInfo[] badGuysICanAttack;
    public static RobotInfo[] goodGuysICanSee;
    public static RobotInfo[] badGuysICanSee;
    public static Direction[] directions;
    public static Tactic tactic;

    public static void init(RobotController rcin) {
        rc = rcin;
        type = rc.getType();
        rand = new Random(rc.getID());
        sensorRadiusSquared = type.sensorRadiusSquared;
        attackRadiusSquared = type.attackRadiusSquared;
        goodGuys = rc.getTeam();
        badGuys = goodGuys.opponent();
        HQLocation = rc.senseHQLocation();
        enemyHQLocation = rc.senseEnemyHQLocation();

        int halfx = ((HQLocation.x + enemyHQLocation.x) / 2);
        int halfy = ((HQLocation.y + enemyHQLocation.y) / 2) ;
        startRallyPoint = new MapLocation(halfx, halfy);

        enemyTowerLocations = rc.senseEnemyTowerLocations();
        numberOfEnemyTowers = enemyTowerLocations.length;

        directions = Direction.values();
        tactic = Tactic.NOENGAGE;
    }

    public static void getRoundInfo() {
        currentLocation = rc.getLocation();
        goodGuysICanSee = rc.senseNearbyRobots(sensorRadiusSquared, goodGuys);
        badGuysICanSee = rc.senseNearbyRobots(sensorRadiusSquared, badGuys);
        goodGuysICanAttack = rc.senseNearbyRobots(attackRadiusSquared, goodGuys);
        badGuysICanAttack = rc.senseNearbyRobots(attackRadiusSquared, badGuys);
    }
}
