package soldiersAgainstBashers;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Strategy {
    static RobotController rc;

    static final int STRATEGY_CHANNEL = 777;
    static StrategyEnum[] strategies;

    public static void init(RobotController rcC) {
        rc =  rcC;
        strategies = StrategyEnum.values();
    }

    public static void set(StrategyEnum strategy) throws GameActionException {
        rc.broadcast(STRATEGY_CHANNEL, strategy.ordinal());
    }

    public static StrategyEnum get() throws GameActionException {
        return strategies[rc.readBroadcast(STRATEGY_CHANNEL)];
    }
}

