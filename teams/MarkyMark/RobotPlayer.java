package MarkyMark;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;
import MarkyMark.Units.*;
import MarkyMark.Structures.*;

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
		} else if (type == RobotType.SOLDIER) {
			Soldier.init();
		} else if (type == RobotType.TANK) {
			Tank.init();
		} else if (type == RobotType.COMMANDER) {
			Commander.init();
		} else if (type == RobotType.DRONE) {
			Drone.init();
		} else if (type == RobotType.LAUNCHER) {
			Launcher.init();
		} else if (type == RobotType.MISSILE) {
			Missile.init();
		} else if (type == RobotType.MINER) {
			Miner.init();
		} else if (type.canSpawn()) {
			Spawner.init();
		}
	}		
}

