package onTheMark.Units;

import battlecode.common.*;
import onTheMark.Navigation;
import onTheMark.RobotPlayer;

public class Miner {
	static RobotController rc = RobotPlayer.rc;

	public static void init() {
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
