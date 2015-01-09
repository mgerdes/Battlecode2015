package MarkyMark.Units;

import MarkyMark.Engagement;
import MarkyMark.Info;
import MarkyMark.Micro;
import MarkyMark.Tactic;
import battlecode.common.*;

// TODO -- Maybe only drones on larger maps.
public class Drone {
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

	// TODO -- make this more dynamic? maybe?
	static void doYourThing() throws GameActionException {
		if (Clock.getRoundNum() < 500) {
			Info.currentTactic = Tactic.HARASS;
			Info.currentEngagementRules = Engagement.AVOID;
		} else {
			Info.currentTactic = Tactic.PROVIDE_SUPPLIES;
			Info.currentEngagementRules = Engagement.AVOID;
		}
		Info.getRoundInfo();
		Micro.doWhatRobotShouldDo();
	}
}
