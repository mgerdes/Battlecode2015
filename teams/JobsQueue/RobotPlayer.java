package JobsQueue;

import battlecode.common.*;
import java.util.*;

import JobsQueue.Structures.HQ;
import JobsQueue.Structures.Tower;
import JobsQueue.Structures.Barracks;
import JobsQueue.Units.Beaver;

public class RobotPlayer {
	public static RobotController rc;
	public static RobotType type;

	public static void run(RobotController rcin) {
		rc = rcin;
		type = rc.getType();
		
		switch(type) {
			case HQ:
				HQ.init();
				break;
			case TOWER:
				Tower.init();
				break;
			case BARRACKS:
				Barracks.init();
				break;
			case BEAVER:
				Beaver.init();
				break;
		}
		
	}
}

