package MarkyMark.Units;

import battlecode.common.*;
import java.util.*;
import MarkyMark.*;

// TODO -- Hardcode in type, and sensor radius to save bytecode.
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
		Micro.doWhatAttackingRobotShouldDo();
	}
}
