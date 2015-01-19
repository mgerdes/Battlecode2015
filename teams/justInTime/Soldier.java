package justInTime;

import battlecode.common.*;
import justInTime.constants.ChannelList;
import justInTime.constants.Order;
import justInTime.navigation.SafeBug;
import justInTime.util.Debug;

public class Soldier {
    private static RobotController rc;

    private static Team enemyTeam;
    private static MapLocation enemyHqLocation;
    private static Team myTeam;
    private static MapLocation myHqLocation;

    public static void run(RobotController rcC) {
        rc = rcC;

        myHqLocation = rc.senseHQLocation();
        enemyHqLocation = rc.senseEnemyHQLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        SafeBug.init(rcC);
        SupplySharing.init(rcC);
        Communication.init(rcC);
        MessageBoard.init(rcC);

        loop();
    }

    private static void loop() {
        while (true) {
            try {
                doYourThing();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    private static void doYourThing() throws GameActionException {
        SupplySharing.share();

        Order order = MessageBoard.getOrder(rc.getType());

        switch (order) {
            case DefendMiners:
                defendMiners();
                break;
        }
    }

    private static void defendMiners() throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        RobotInfo[] enemiesInAttackRange = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, enemyTeam);
        if (enemiesInAttackRange.length > 0) {
            Communication.setDistressLocation(currentLocation);
        }

        if (rc.isWeaponReady()) {
            if (enemiesInAttackRange.length > 0) {
                rc.attackLocation(enemiesInAttackRange[0].location);
                return;
            }
        }

        if (!rc.isCoreReady()) {
            return;
        }

        MapLocation distressLocation = Communication.getDistressLocation();
        if (distressLocation != null) {
            SafeBug.setDestination(distressLocation);
        }
        else {
            RobotInfo[] enemiesInSensorRange = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);

            int minerDistance = rc.readBroadcast(ChannelList.MINER_DISTANCE_SQUARED_TO_HQ);
            int myDistanceToHq = currentLocation.distanceSquaredTo(myHqLocation);
            //--Go to nearby enemy unless I'm too far from base
            if (enemiesInSensorRange.length > 0
                    && myDistanceToHq < minerDistance + 4) {
                SafeBug.setDestination(enemiesInSensorRange[0].location);
            }
            //--If there are no nearby enemies, make sure that I'm outside the miner circle
            else if (myDistanceToHq < minerDistance + 2) {
                Debug.setString(1, "my distance is " + myDistanceToHq, rc);
                SafeBug.setDestination(enemyHqLocation);
            }
            else {
                return;
            }
        }

        Direction direction = SafeBug.getDirection(currentLocation);
        if (direction != Direction.NONE) {
            rc.move(direction);
        }
    }
}
