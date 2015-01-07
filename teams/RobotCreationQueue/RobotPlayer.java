package RobotCreationQueue;

import battlecode.common.*;
import java.util.*;
import RobotCreationQueue.*;

public class RobotPlayer {
	public static RobotController rc;
	public static void run(RobotController rcin) {
		rc = rcin;
		RobotType type = rc.getType();

		if (type == RobotType.HQ) {
			HQ.init(rc);
			HQ.run();
		} else if (type == RobotType.BARRACKS) {
			Barracks.init(rc);
			Barracks.run();
		} else if (type == RobotType.BEAVER) {
			Beaver.init(rc);
			Beaver.run();
		} else if (type == RobotType.SOLDIER) {
			Soldier.init(rc);
			Soldier.run();
		} else if (type == RobotType.TOWER) {
			while (true) {
				rc.yield();
			}
		}
	}
}
