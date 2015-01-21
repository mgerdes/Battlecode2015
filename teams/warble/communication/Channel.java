package warble.communication;

public class Channel {
    //--Locations (all need 2 channels)
    public static final int STRUCTURE_TO_ATTACK = 0;
    public static final int RALLY_POINT = 2;
    public static final int OUR_TOWER_WITH_LOWEST_VOID_COUNT = 4;
    public static final int NE_MAP_CORNER = 6;
    public static final int SE_MAP_CORNER = 8;
    public static final int SW_MAP_CORNER = 10;
    public static final int NW_MAP_CORNER = 12;

    public static final int ENEMY_TOWER_1 = 14;
    public static final int ENEMY_TOWER_2 = 16;
    public static final int ENEMY_TOWER_3 = 18;
    public static final int ENEMY_TOWER_4 = 20;
    public static final int ENEMY_TOWER_5 = 22;
    public static final int ENEMY_TOWER_6 = 24;
    public static final int ENEMY_HQ = 26;

    public static final int LOCATION_TO_SURVEY = 28;

    public static final int[] POI = {30,31,32,33,34,35,36};
    public static final int NUMBER_OF_POIS = 37;

    //--Path building info
    public static final int CURRENT_POI = 80;
    public static final int LAST_LOCATION_RELATIVE = 81;

    //--Misc
    public static final int MINER_DISTANCE_SQUARED_TO_HQ = 90;
    public static final int TOWER_VOID_COUNT = 91;
    public static final int PERIMETER_SURVEY_COMPLETE = 92;
    public static final int MAP_WIDTH = 93;
    public static final int MAP_HEIGHT = 94;
    public static final int MAP_SYMMETRY = 95;
    public static final int POI_TO_ATTACK = 96;
    public static final int ALL_TERRAIN_TILES_BROADCASTED = 97;
    protected static final int NEED_SUPPLY_ROBOT_ID = 98;
    protected static final int NEED_SUPPLY_CONTEXT = 99;

    //--Unit counts
    public static final int DRONE_COUNT = 100;
    public static final int MINER_COUNT = 101;
    public static final int SOLDIER_COUNT = 102;
    public static final int LAUNCHER_COUNT = 103;

    //--Building counts
    public static final int SUPPLY_DEPOT_COUNT = 200;
    protected static final int SUPPLY_DEPOT_ROUND_UPDATED = 201;

    //--Production orders
    protected static final int MORE_MINERS = 500;
    protected static final int MORE_DRONES = 501;
    protected static final int MORE_SOLDIERS = 502;
    protected static final int MORE_LAUNCHERS = 503;

    //--Building Queue
    public static final int BUILDING_QUEUE_START = 3000;
    public static final int NEXT_BUILDING_POINTER = 4000;
    public static final int QUEUE_END_POINTER = 4001;

    //--Distress!
    protected static final int DISTRESS_SIGNAL_ROUND_NUMBER = 5000;
    protected static final int DISTRESS_SIGNAL_CREATOR = 5001;
    protected static final int DISTRESS_LOCATION_X = 5002;
    protected static final int DISTRESS_LOCATION_Y = 5003;

    protected static final int ENEMY_SPOTTED_CONTEXT = 5100;
    protected static final int ENEMY_SPOTTED_LOCATION_X = 5101;
    protected static final int ENEMY_SPOTTED_LOCATION_Y = 5102;

    //--Hq orders
    protected static final int SOLDIER_PRIORITY_ORDERS = 6000;
    protected static final int DRONE_PRIORITY_ORDERS = 6020;
    protected static final int LAUNCHER_PRIORITY_ORDERS = 6030;

    protected static final int SOLDIER_DEFAULT_ORDER = 7000;
    protected static final int DRONE_DEFAULT_ORDERS = 7002;
    protected static final int LAUNCHER_DEFAULT_ORDERS = 7030;

    //--Terrain tile storage
    public static final int NW_CORNER_TERRAIN_TILE = 10000; //--24400

    //--BFS storage
    public static final int NW_CORNER_BFS_DIRECTIONS = 30000; //--44400

    //--BFS queue
    public static final int BFS_LOOP_STATE = 49997;
    public static final int BFS_QUEUE_FRONT = 49998;
    public static final int BFS_QUEUE_BACK = 49999;
    public static final int BFS_QUEUE_START = 50000;
}

