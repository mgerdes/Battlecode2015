package MarkyMark.Units;

import MarkyMark.Engagement;
import MarkyMark.Info;
import MarkyMark.Micro;
import MarkyMark.Tactic;
import battlecode.common.*;

// TODO -- have drones harass at beginning of game, have them transport supplies later in game.
// Maybe only use them on larger maps.
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

	static void doYourThing() throws GameActionException {
		if (Clock.getRoundNum() < 1000) {
			Info.currentTactic = Tactic.HARASS;
			Info.currentEngagementRules = Engagement.AVOID;
		} else {
			Info.currentTactic = Tactic.PROVIDE_SUPPLIES;
			Info.currentEngagementRules = Engagement.ENGAGE;
		}
		Info.getRoundInfo();
		Micro.doWhatRobotShouldDo();
	}
}
