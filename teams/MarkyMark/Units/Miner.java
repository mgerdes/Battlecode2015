package MarkyMark.Units;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

public class Miner {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		loop();
	}

	static void loop() {
		while (true) {
			try {
				doYourThing();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	static void doYourThing() throws GameActionException {
		if(!tryToMine()) {
			Navigation.moveRandomly();
		}
	}

	static boolean tryToMine() throws GameActionException {
		if (rc.senseOre(rc.getLocation()) > 0) {
			if (rc.isCoreReady()) {
				rc.mine();
				return true;
			}
		}
		return false;
	}
}
