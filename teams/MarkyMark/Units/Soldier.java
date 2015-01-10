package MarkyMark.Units;

import MarkyMark.Info;
import MarkyMark.Micro;
import battlecode.common.*;

public class Soldier {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		Info.init(rc);
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
		Info.getRoundInfo();
		Micro.doWhatRobotShouldDo();
	}
}

