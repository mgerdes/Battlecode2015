package oreo.communication;

public class Encoding {
    public static int getHashedLocation(int x, int y) {
        return 120 * x + y;
    }

    public static int getXCoordinate(int hashedMapLocation) {
        return hashedMapLocation / 120;
    }

    public static int getYCoordinate(int hashedMapLocation) {
        return hashedMapLocation % 120;
    }
}
