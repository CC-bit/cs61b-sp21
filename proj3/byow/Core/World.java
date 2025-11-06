package byow.Core;

import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World implements Serializable {
    public static final int MIN_NUM_FLOOR = 1;
    public static final int MAX_NUM_FLOOR = 9;
    public static final int FLOOR_WIDTH = 35;
    public static final int FLOOR_HEIGHT = 35;
    public static final int MIN_NUM_ROOM = 5;
    public static final int MAX_NUM_ROOM = 9;
    public static final int MIN_SIDE_ROOM = 7;
    public static final int MAX_SIDE_ROOM = 11;

    private final Random random;
    private final List<Floor> floorList = new ArrayList<>();
    private final Floor currentFloor;
    private Ordinary avatarOrdinary;

    public World(Random random) {
        this.random = random;
        generateFloors();
        this.currentFloor = floorList.get(0);
        initAvatar();
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    /** Generates random number floors. */
    private void generateFloors() {
        int n = RandomUtils.uniform(random, MIN_NUM_FLOOR, MAX_NUM_FLOOR + 1);
        for (int i = MIN_NUM_FLOOR; i <= n; i += 1) {
            floorList.add(new Floor(random, i, FLOOR_WIDTH, FLOOR_HEIGHT));
        }
    }

    private void initAvatar() {
        Room room = currentFloor.getRandomRoom();
        avatarOrdinary = currentFloor.randomRoomFloorTile(room);
        currentFloor.addTile(avatarOrdinary.getX(), avatarOrdinary.getY(), Tileset.AVATAR);
    }

    /** Moves the avatar to the given ordinary. */
    private void moveAvatarHelper(int x, int y) {
        currentFloor.addTile(avatarOrdinary.getX(), avatarOrdinary.getY(), Tileset.FLOOR);
        currentFloor.addTile(x, y, Tileset.AVATAR);
        avatarOrdinary = new Ordinary(x, y, currentFloor.getFloorNumber());
    }

    /** Moves the avatar to the given orention if the new site is a floor tile. */
    public void moveAvatar(char key) {
        int x = avatarOrdinary.getX();
        int y = avatarOrdinary.getY();
        if (key == 'W' && currentFloor.getTile(x, y + 1).equals(Tileset.FLOOR)) {
            moveAvatarHelper(x, y + 1);
        } else if (key == 'S' && currentFloor.getTile(x, y - 1).equals(Tileset.FLOOR)) {
            moveAvatarHelper(x, y - 1);
        } else if (key == 'A' && currentFloor.getTile(x - 1, y).equals(Tileset.FLOOR)) {
            moveAvatarHelper(x - 1, y);
        } else if (key == 'D' && currentFloor.getTile(x + 1, y).equals(Tileset.FLOOR)) {
            moveAvatarHelper(x + 1, y);
        }
    }

}
