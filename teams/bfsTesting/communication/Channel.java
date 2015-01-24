package bfsTesting.communication;

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
    public static final int CURRENT_POI = 50;
    public static final int LAST_LOCATION_RELATIVE = 51;

    //--Misc
    public static final int MINER_DISTANCE_SQUARED_TO_HQ = 60;
    public static final int TOWER_VOID_COUNT = 61;
    public static final int PERIMETER_SURVEY_COMPLETE = 62;
    public static final int MAP_WIDTH = 63;
    public static final int MAP_HEIGHT = 64;
    public static final int MAP_SYMMETRY = 65;
    public static final int POI_TO_ATTACK = 66;
    public static final int ALL_TERRAIN_TILES_BROADCASTED = 67;
    protected static final int NEED_SUPPLY_ROBOT_ID = 68;
    protected static final int NEED_SUPPLY_CONTEXT = 69;

    //--Unit counts
    public static final int DRONE_COUNT = 80;
    public static final int MINER_COUNT = 81;
    public static final int SOLDIER_COUNT = 82;
    public static final int LAUNCHER_COUNT = 83;

    //--Building counts
    public static final int SUPPLY_DEPOT_COUNT = 90;
    protected static final int SUPPLY_DEPOT_ROUND_UPDATED = 91;

    //--Production orders
    public static final int MORE_MINERS = 100;
    public static final int MORE_DRONES = 101;
    public static final int MORE_SOLDIERS = 102;
    public static final int MORE_LAUNCHERS = 103;

    //--Building Queue
    public static final int NEXT_BUILDING_POINTER = 198;
    public static final int QUEUE_END_POINTER = 199;
    public static final int BUILDING_QUEUE_START = 200; //--349

    //--Distress!
    protected static final int DISTRESS_SIGNAL_ROUND_NUMBER = 350;
    protected static final int DISTRESS_SIGNAL_CREATOR = 351;
    protected static final int DISTRESS_LOCATION_X = 352;
    protected static final int DISTRESS_LOCATION_Y = 353;

    protected static final int ENEMY_SPOTTED_CONTEXT = 360;
    protected static final int ENEMY_SPOTTED_LOCATION_X = 361;
    protected static final int ENEMY_SPOTTED_LOCATION_Y = 362;

    //--Message board

    public static final int SOLDIER_PRIORITY_ORDERS = 400;
    public static final int DRONE_PRIORITY_ORDERS = 420;
    public static final int LAUNCHER_PRIORITY_ORDERS = 430;

    public static final int SOLDIER_DEFAULT_ORDER = 500;
    public static final int DRONE_DEFAULT_ORDERS = 501;
    public static final int LAUNCHER_DEFAULT_ORDERS = 502;

    //--Survey location queue
    public static final int SURVEY_LOCATION_QUEUE_HEAD = 6198;
    public static final int SURVEY_LOCATION_QUEUE_TAIL = 6199;
    public static final int SURVEY_LOCATION_QUEUE_START = 6200; //--20599

    //--Terrain tile storage
    public static final int NW_CORNER_TERRAIN_TILE = 20600; //--34999

    //--BFS storage
    public static final int NW_CORNER_BFS_DIRECTIONS = 35000; //--49399

    //--BFS queue
    public static final int BFS_LOOP_STATE = 49997;
    public static final int BFS_QUEUE_FRONT = 49998;
    public static final int BFS_QUEUE_BACK = 49999;
    public static final int BFS_QUEUE_START = 50000;
}

