package MarkyMark.Units;

import MarkyMark.Info;
import MarkyMark.Navigation;
import battlecode.common.*;

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

	// TODO -- make mining more intelligent
	static void doYourThing() throws GameActionException {
		Info.getRoundInfo();
		if(!tryToMine()) {
			Navigation.moveRandomly();
		}
	}

	static boolean tryToMine() throws GameActionException {
		if (rc.senseOre(rc.getLocation()) > 5) {
			if (rc.isCoreReady()) {
				rc.mine();
				return true;
			}
		}
		return false;
	}
}
