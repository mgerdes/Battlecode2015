package MarkyMark.Units;

import MarkyMark.Info;
import MarkyMark.Micro;
import battlecode.common.*;

public class Basher {
	static RobotController rc;

	public static void init(RobotController rcin) {
		rc = rcin;
		loop();
	}

	static void loop() {
		while (true) {
			try {
				Info.getRoundInfo();
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
