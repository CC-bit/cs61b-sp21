package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Floor {
    public static final int WIDTH = 35;
    public static final int HEIGHT = 35;
    private final TETile[][] floor = new TETile[WIDTH][HEIGHT];
    private final int floorNum;

    public Floor(int floorNum) {
        this.floorNum = floorNum;

        for (int i = 0; i < WIDTH; i += 1) {
            for (int j = 0; j < HEIGHT; j += 1) {
                floor[i][j] = Tileset.NOTHING;
            }
        }
    }

    public int getFloorNumber() {
        return floorNum;
    }

    public TETile[][] getFloor() {
        return floor;
    }

    /** Returns the tile in given ordinary.
     * Returns null if ordinary out of bound. */
    public TETile getTile(int x, int y) {
        if (!(0 <= x && x < WIDTH && 0 <= y && y < HEIGHT)) {
            return null;
        }
        return floor[x][y];
    }

    /**
     * Add a tile at given ordinary.
     * @param x the x ordinary
     * @param y the y ordinary
     * @param tile the TETile instance to add
     */
    public void addTile(int x, int y, TETile tile) {
        floor[x][y] = tile;
    }

}
