package oreo;

import battlecode.common.*;
import oreo.util.Debug;

public class SupplySharing {
    private static RobotController rc;
    private static Team myTeam;

    private static final int MIN_SHARE_AMOUNT = 150;

    public static void init(RobotController rcC) {
        rc = rcC;
        myTeam = rcC.getTeam();
    }

    public static void share() throws GameActionException {
        int initialBytecode = Clock.getBytecodeNum();
        RobotInfo[] teamInTransferRange = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        double mySupply = rc.getSupplyLevel();

        //--Check if any friends have no supply
        for (RobotInfo robot : teamInTransferRange) {
            if (robot.type.isBuilding) {
                continue;
            }

            if (robot.supplyLevel == 0) {
                rc.transferSupplies((int) (mySupply / 2), robot.location);
                return;
            }
        }

        //--Otherwise, try to balance supply
        if (mySupply < MIN_SHARE_AMOUNT * 2) {
            return;
        }
        for (RobotInfo robot : teamInTransferRange) {
            if (robot.type.isBuilding) {
                continue;
            }

            if (mySupply > robot.supplyLevel) {
                int halfTheDifference = (int) ((mySupply - robot.supplyLevel) / 2);
                if (halfTheDifference < MIN_SHARE_AMOUNT) {
                    continue;
                }

                rc.transferSupplies(halfTheDifference, robot.location);
                break;
            }
        }

        int bytecodesUsed = Clock.getBytecodeNum() - initialBytecode;
        Debug.setString(2, String.format("%d bytecodes for supply sharing", bytecodesUsed), rc);
    }

    public static void shareOnlyWithType(RobotType type) throws GameActionException {
        int initialBytecode = Clock.getBytecodeNum();
        RobotInfo[] teamInTransferRange = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        double mySupply = rc.getSupplyLevel();
        if (mySupply < MIN_SHARE_AMOUNT * 2) {
            return;
        }

        for (RobotInfo robot : teamInTransferRange) {
            if (robot.type != type) {
                continue;
            }

            if (mySupply > robot.supplyLevel) {
                int halfTheDifference = (int) ((mySupply - robot.supplyLevel) / 2);
                if (halfTheDifference < MIN_SHARE_AMOUNT) {
                    continue;
                }

                rc.transferSupplies(halfTheDifference, robot.location);
                break;
            }
        }

        int bytecodesUsed = Clock.getBytecodeNum() - initialBytecode;
        Debug.setString(2, String.format("%d bytecodes for supply sharing", bytecodesUsed), rc);
    }

    public static void shareMore() throws GameActionException {
        int initialBytecode = Clock.getBytecodeNum();
        RobotInfo[] teamInTransferRange = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        double mySupply = rc.getSupplyLevel();
        if (mySupply < MIN_SHARE_AMOUNT * 2) {
            return;
        }

        for (RobotInfo robot : teamInTransferRange) {
            if (robot.type.isBuilding
                    || robot.type == RobotType.BEAVER) {
                continue;
            }

            if (mySupply > robot.supplyLevel) {
                //--Give away all but 1000 supply
                int allSaveOneThousand = (int) (mySupply - 1000);
                int toGive = allSaveOneThousand > 0 ? allSaveOneThousand : (int) mySupply;
                rc.transferSupplies(toGive, robot.location);
                break;
            }
        }

        int bytecodesUsed = Clock.getBytecodeNum() - initialBytecode;
        rc.setIndicatorString(2, String.format("%d bytecodes for supply sharing", bytecodesUsed));
    }
}
