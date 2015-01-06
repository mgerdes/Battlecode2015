package JobsQueue.Units;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class Basher {
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

	static void doYourThing() {

	}
}
