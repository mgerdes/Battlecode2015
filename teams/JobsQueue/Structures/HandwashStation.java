package JobsQueue.Structures;

import battlecode.common.*;
import java.util.*;
import JobsQueue.*;

public class HandwashStation {
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