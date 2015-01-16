package framework;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import framework.constants.ChannelList;
import framework.constants.Config;
import framework.constants.Order;

public class MessageBoard {
    private static RobotController rc;

    private static Order[] orders;

    public static void init(RobotController rcC) {
        rc = rcC;
        orders = Order.values();
    }

    //--To turn on spawn, set value = 1,
    //--To turn off spawn, set vale = 0
    public static void setSpawn(RobotType type, int value) throws GameActionException {
        switch (type) {
            case MINER:
                updateChannelIfDifferent(ChannelList.MORE_MINERS, value);
                break;
            case DRONE:
                updateChannelIfDifferent(ChannelList.MORE_DRONES, value);
                break;
            case BASHER:
                updateChannelIfDifferent(ChannelList.MORE_BASHERS, value);
                break;
            case SOLDIER:
                updateChannelIfDifferent(ChannelList.MORE_SOLDIERS, value);
                break;
        }
    }

    public static boolean shouldSpawn(RobotType type) throws GameActionException {
        switch (type) {
            case MINER:
                return rc.readBroadcast(ChannelList.MORE_MINERS) == 1;
            case SOLDIER:
                return rc.readBroadcast(ChannelList.MORE_SOLDIERS) == 1;
            case BASHER:
                return rc.readBroadcast(ChannelList.MORE_BASHERS) == 1;
            case DRONE:
                return rc.readBroadcast(ChannelList.MORE_DRONES) == 1;
            case TANK:
                return rc.readBroadcast(ChannelList.MORE_TANKS) == 1;
        }

        return false;
    }

    public static void setDefaultOrder(RobotType type, Order order) throws GameActionException {
        switch (type) {
            case SOLDIER:
                updateChannelIfDifferent(ChannelList.SOLDIER_DEFAULT_ORDER, order.ordinal());
        }
    }

    public static void setPriorityOrder(int count, RobotType type, Order order) throws GameActionException {
        switch (type) {
            case SOLDIER:
                setPriorityOrderForChannel(ChannelList. SOLDIER_PRIORITY_ORDERS, count, order);
        }
    }

    public static Order getOrder(RobotType type) throws GameActionException {
        switch (type) {
            case SOLDIER:
                return getPriorityOrDefaultOrder(
                        ChannelList.SOLDIER_PRIORITY_ORDERS,
                        ChannelList.SOLDIER_DEFAULT_ORDER);
        }

        return Order.NoOrder;
    }

    //--RRRR-AA-CC-OO
    private static void setPriorityOrderForChannel(int channel, int count, Order order) throws
            GameActionException {
        int value = Clock.getRoundNum() * 1000000 + count * 100 + order.ordinal();
        rc.broadcast(channel, value);
    }

    private static Order getPriorityOrDefaultOrder(int priorityChannel, int defaultChannel) throws GameActionException {
        Order order = getHighestPriorityOrder(priorityChannel);
        if (order != Order.NoOrder) {
            return order;
        }

        return orders[rc.readBroadcast(defaultChannel)];
    }

    private static Order getHighestPriorityOrder(int channel) throws GameActionException {
        for (int i = 0; i < Config.MAX_NUMBER_PRIORITY_ORDERS_PER_UNIT; i++) {
            Order priorityOrder = getPriorityOrderFromThisChannel(channel + i);
            if (priorityOrder != Order.NoOrder) {
                return priorityOrder;
            }
        }

        return Order.NoOrder;
    }

    private static Order getPriorityOrderFromThisChannel(int channel) throws GameActionException {
        int value = rc.readBroadcast(channel);
        int currentRound = Clock.getRoundNum();
        int roundUpdated = value / 1000000;
        if (roundUpdated != currentRound) {
            //--This order is expired
            return Order.NoOrder;
        }

        //--How many units have already taken this order?
        int unitsActive = (value / 10000) % 100;
        int unitsNeeded = (value / 100) % 100;
        if (unitsNeeded > unitsActive) {
            //--Add one to the unit count
            rc.broadcast(channel, value + 1000);
            return orders[value % 100];
        }

        return Order.NoOrder;
    }

    private static void updateChannelIfDifferent(int channel, int value) throws GameActionException {
        //--Only broadcast the value if it needs to be changed
        int currentValue = rc.readBroadcast(channel);
        if (value == currentValue) {
            return;
        }

        rc.broadcast(channel, value);
    }
}
