package soldiersAgainstBashers.Units;

import battlecode.common.*;
import battlecode.world.Robot;
import soldiersAgainstBashers.Navigation;
import soldiersAgainstBashers.RobotPlayer;

public class Drone {
	static RobotController rc = RobotPlayer.rc;
	static Team goodGuys;
	static Team badGuys;
	static MapLocation myHQ;

	static final int MIN_SUPPY = 300;
	static final int DRONE_MIN_SUPPLY = 1000;

	public static void init() {
		rc = RobotPlayer.rc;
		goodGuys = rc.getTeam();
		badGuys = goodGuys.opponent();
		myHQ = rc.senseHQLocation();
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
		double currentSupply = rc.getSupplyLevel();
		if (currentSupply < DRONE_MIN_SUPPLY) {
			Navigation.tryMoveTowards(myHQ);
			return;
		}

		RobotInfo[] friendlies = rc.senseNearbyRobots(1000000, goodGuys);
		MapLocation current = rc.getLocation();

		for (RobotInfo ri : friendlies) {
			if (ri.type == RobotType.TOWER) {
				continue;
			}

			if (ri.supplyLevel < MIN_SUPPY) {
				if (current.distanceSquaredTo(ri.location) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
					rc.transferSupplies((int) currentSupply / 10, ri.location);
				}
				else if (rc.isCoreReady()) {
					Navigation.tryMoveTowards(ri.location);
					break;
				}

				if (rc.getSupplyLevel() < DRONE_MIN_SUPPLY) {
					break;
				}
			}
		}

	}
}
