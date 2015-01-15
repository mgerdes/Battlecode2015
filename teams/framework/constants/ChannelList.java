package framework.constants;

//--Enum class
public class ChannelList {
    public static final int TACTIC = 0;
    public static final int STRUCTURE_TO_ATTACK_X = 1;
    public static final int STRUCTURE_TO_ATTACK_Y = 2;
    public static final int MINER_RADIUS_FROM_HQ = 3;

    //--Unit counts
    public static final int DRONE_COUNT = 100;
    public static final int MINER_COUNT = 101;
    public static final int BASHER_COUNT = 102;
    public static final int SOLDIER_COUNT = 103;
    public static final int TANK_COUNT = 104;

    //--HQ orders
    public static final int MORE_MINERS = 500;
    public static final int MORE_DRONES = 501;
    public static final int MORE_BASHERS = 502;
    public static final int MORE_SOLDIERS = 503;
    public static final int MORE_TANKS = 504;

    public static final int DRONE_SWARM = 600;
    public static final int DRONE_DEFEND = 601;
    public static final int DRONE_ATTACK = 602;

    public static final int SOLDIER_ATTACK_ENEMY_MINERS = 700;

    //--Job reporting
    public static final int SUPPLY_MINERS_JOB_IS_NEEDED = 1000;
    public static final int SUPPLY_MINERS_JOB_REPORTING = 1001;

    //--Building Queue
    public static final int BUILDING_QUEUE_START = 3000;
    public static final int NEXT_BUILDING_POINTER = 4000;
    public static final int QUEUE_END_POINTER = 4001;
}
