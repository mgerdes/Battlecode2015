package justInTime.constants;

public class ChannelList {
    //--Locations (all need 2 channels)
    public static final int STRUCTURE_TO_ATTACK = 0;
    public static final int RALLY_POINT = 2;
    public static final int OUR_TOWER_WITH_LOWEST_VOID_COUNT = 4;
    public static final int NE_MAP_CORNER = 6;
    public static final int SE_MAP_CORNER = 8;
    public static final int SW_MAP_CORNER = 10;
    public static final int NW_MAP_CORNER = 12;

    public static final int MINER_DISTANCE_SQUARED_TO_HQ = 90;
    public static final int TOWER_VOID_COUNT = 91;
    public static final int SURVEY_COMPLETE = 92;
    public static final int MAP_WIDTH = 93;
    public static final int MAP_HEIGHT = 94;
    public static final int MAP_SYMMETRY = 95;

    //--Unit counts
    public static final int DRONE_COUNT = 100;
    public static final int MINER_COUNT = 101;
    public static final int SOLDIER_COUNT = 102;
    public static final int LAUNCHER_COUNT = 103;

    //--Building counts
    public static final int SUPPLY_DEPOT_COUNT = 200;
    public static final int SUPPLY_DEPOT_ROUND_UPDATED = 201;

    //--Production orders
    public static final int MORE_MINERS = 500;
    public static final int MORE_DRONES = 501;
    public static final int MORE_SOLDIERS = 502;
    public static final int MORE_LAUNCHERS = 503;

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

