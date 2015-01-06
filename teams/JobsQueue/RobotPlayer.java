package JobsQueue;

import battlecode.common.*;
import java.util.*;

import JobsQueue.Structures.HQ;
import JobsQueue.Structures.Tower;
import JobsQueue.Structures.SupplyDepot;
import JobsQueue.Structures.TechnologyInstitute;
import JobsQueue.Structures.TrainingField;
import JobsQueue.Structures.Barracks;
import JobsQueue.Structures.TankFactory;
import JobsQueue.Structures.Helipad;
import JobsQueue.Structures.AerospaceLab;
import JobsQueue.Structures.HandwashStation;
import JobsQueue.Structures.MinerFactory;

import JobsQueue.Units.Beaver;
import JobsQueue.Units.Computer;
import JobsQueue.Units.Commander;
import JobsQueue.Units.Soldier;
import JobsQueue.Units.Basher;
import JobsQueue.Units.Tank;
import JobsQueue.Units.Drone;
import JobsQueue.Units.Launcher;
import JobsQueue.Units.Miner;

public class RobotPlayer {
	public static RobotController rc;
	public static RobotType type;

	public static void run(RobotController rcin) {
		rc = rcin;
		type = rc.getType();
		
		switch(type) {
			case HQ:
				HQ.init();
				break;
			case TOWER:
				Tower.init();
				break;
			case SUPPLYDEPOT: 
				SupplyDepot.init();
				break;
			case TECHNOLOGYINSTITUTE:
				TechnologyInstitute.init();
				break;
			case TRAININGFIELD:
				TrainingField.init();
				break;
			case BARRACKS:
				Barracks.init();
				break;
			case TANKFACTORY:
				TankFactory.init();
				break;
			case HELIPAD:
				Helipad.init();
				break;
			case AEROSPACELAB:
				AerospaceLab.init();
				break;
			case MINERFACTORY:
				MinerFactory.init();
				break;
			case BEAVER:
				Beaver.init();
				break;
			case COMPUTER:
				Computer.init();
				break;
			case COMMANDER:
				Commander.init();
				break;
			case SOLDIER:
				Soldier.init();
				break;
			case BASHER:
				Basher.init();
				break;
			case TANK:
				Tank.init();
				break;
			case DRONE:
				Drone.init();
				break;
			case LAUNCHER:
				Launcher.init();
				break;
			case MINER:
				Miner.init();
				break;
		}
		
	}
}

