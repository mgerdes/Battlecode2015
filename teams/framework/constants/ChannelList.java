package framework.constants;

public class ChannelList {
    //--Locations
    public static final int STRUCTURE_TO_ATTACK = 0; //--1
    public static final int RALLY_POINT = 2; //--3
    public static final int OUR_TOWER_WITH_LOWEST_VOID_COUNT = 4; //--5

    public static final int MINER_RADIUS_FROM_HQ = 90;
    public static final int TOWER_VOID_COUNT = 91;

    //--Unit counts
    public static final int DRONE_COUNT = 100;
    public static final int MINER_COUNT = 101;
    public static final int BASHER_COUNT = 102;
    public static final int SOLDIER_COUNT = 103;
    public static final int TANK_COUNT = 104;

    //--Building counts
    public static final int SUPPLY_DEPOT_COUNT = 200;
    public static final int SUPPLY_DEPOT_ROUND_UPDATED = 201;

    //--Production orders
    public static final int MORE_MINERS = 500;
    public static final int MORE_DRONES = 501;
    public static final int MORE_BASHERS = 502;
    public static final int MORE_SOLDIERS = 503;
    public static final int MORE_TANKS = 504;

    //--Job reporting
    public static final int SUPPLY_MINERS_JOB_IS_NEEDED = 1000;
    public static final int SUPPLY_MINERS_JOB_REPORTING = 1001;

    //--Building Queue
    public static final int BUILDING_QUEUE_START = 3000;
    public static final int NEXT_BUILDING_POINTER = 4000;
    public static final int QUEUE_END_POINTER = 4001;

    //--Distress!
    public static final int DISTRESS_SIGNAL_ROUND_NUMBER = 5000;
    public static final int DISTRESS_SIGNAL_CREATOR = 5001;
    public static final int DISTRESS_LOCATION_X = 5002;
    public static final int DISTRESS_LOCATION_Y = 5003;

    //--Message board

    public static final int SOLDIER_PRIORITY_ORDERS = 6000;
    public static final int BASHER_PRIORITY_ORDERS = 6010;
    public static final int DRONE_PRIORITY_ORDERS = 6020;
    public static final int TANK_PRIORITY_ORDERS = 6030;

    public static final int SOLDIER_DEFAULT_ORDER = 7000;
    public static final int BASHER_DEFAULT_ORDERS = 7001;
    public static final int DRONE_DEFAULT_ORDERS = 7002;
    public static final int TANK_DEFAULT_ORDERS = 7030;
}

