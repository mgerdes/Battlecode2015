package soldiersAgainstBashers.Units;

import battlecode.common.*;
import soldiersAgainstBashers.Move;
import soldiersAgainstBashers.Navigation;
import soldiersAgainstBashers.RobotPlayer;

public class Miner {
	static RobotController rc = RobotPlayer.rc;

	public static void init() {
		Move.init(rc);
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
		if (!rc.isCoreReady()) {
			return;
		}

		if (rc.senseOre(rc.getLocation()) > 0) {
			rc.mine();
		}
		else {
			Move.inRandomDirection();
		}
	}
}
