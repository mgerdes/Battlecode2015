package RobotCreationQueue;

import battlecode.common.*;
import java.util.*;

public class Soldier {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		RobotCreationQueue.init(rc);
		run();
	}

	public static void run() {
		while (true) {
			try {
				if (rc.isCoreReady() && rc.canMove(Direction.NORTH)) {
					rc.move(Direction.NORTH);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}
}
