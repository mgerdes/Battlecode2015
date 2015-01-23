package tankers;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        RobotType type = rc.getType();
        switch (type) {
            case HQ:
                HQ.run(rc);
                break;
            case TOWER:
                Tower.run(rc);
                break;
            case SUPPLYDEPOT:
                SupplyDepot.run(rc);
                break;
            case BEAVER:
                Beaver.run(rc);
                break;
            case MINER:
                Miner.run(rc);
                break;
            case DRONE:
                Drone.run(rc);
                break;
            case SOLDIER:
                Soldier.run(rc);
                break;
            case LAUNCHER:
                Launcher.run(rc);
                break;
            case MISSILE:
                Missile.run(rc);
                break;
            default: //--All buildings other than towers and HQ
                Spawner.init(rc);
                break;
        }
    }
}
