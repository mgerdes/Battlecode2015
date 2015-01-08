package onTheMark;

import battlecode.common.*;
import onTheMark.Structures.*;
import onTheMark.Units.*;

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
		} else if (type == RobotType.TANK) {
			Tank.init();
		} else if (type == RobotType.DRONE) {
			Drone.init();
		} else if (type == RobotType.MINER) {
			Miner.init();
		} else if (type == RobotType.BASHER) {
			Basher.init();
		} else {
			Spawner.init();
		}
	}		
}

