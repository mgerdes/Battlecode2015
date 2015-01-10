package soldiersAgainstBashers;

import battlecode.common.*;
import soldiersAgainstBashers.Structures.*;
import soldiersAgainstBashers.Units.*;

public class RobotPlayer {
	public static RobotController rc;
	public static RobotType type;

	public static void run(RobotController rcin) {
		rc = rcin;
		type = rc.getType();

		if (type == RobotType.HQ) {
			HQ.init();			
		} else if (type == RobotType.TOWER) {
			Tower.init();
		} else if (type == RobotType.BEAVER) {
			Beaver.init();
		} else if (type == RobotType.DRONE) {
			Drone.init();
		} else if (type == RobotType.MINER) {
			Miner.init();
		} else if (type == RobotType.BASHER) {
			Basher.init();
		} else if (type == RobotType.SOLDIER) {
			Soldier.init();
		} else {
			Spawner.init();
		}
	}		
}

