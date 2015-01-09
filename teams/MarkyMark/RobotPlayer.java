package MarkyMark;

import battlecode.common.*;

import MarkyMark.Units.*;
import MarkyMark.Structures.*;

public class RobotPlayer {
	public static RobotController rc;
	public static RobotType type;

	public static void run(RobotController rcin) {
		rc = rcin;
		type = rc.getType();

		try {
			Info.init(rc);
			Micro.init(rc);
			Attack.init(rc);
			Navigation.init(rc);
			RobotCreationQueue.init(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (type == RobotType.HQ) {
			HQ.init(rc);
		} else if (type == RobotType.TOWER) {
			Tower.init(rc);
		} else if (type == RobotType.BEAVER) {
			Beaver.init(rc);
		} else if (type == RobotType.BASHER) {
			Basher.init(rc);
		} else if (type == RobotType.SOLDIER) {
			Soldier.init(rc);
		} else if (type == RobotType.TANK) {
			Tank.init(rc);
		} else if (type == RobotType.COMMANDER) {
			Commander.init(rc);
		} else if (type == RobotType.DRONE) {
			Drone.init(rc);
		} else if (type == RobotType.LAUNCHER) {
			Launcher.init(rc);
		} else if (type == RobotType.MISSILE) {
			Missile.init(rc);
		} else if (type == RobotType.MINER) {
			Miner.init(rc);
		} else {
			Spawner.init(rc);
		}
	}

}

