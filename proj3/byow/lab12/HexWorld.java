package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 * Fully filled with a big hexagon consist of 19 small hexagons.
 */
public class HexWorld {
    private final Random RANDOM;
    private final int smallHexSize = 4;
    private final int bigHexSize = 3;
    private final int WORLD_SIZE;
    TETile[][] tiles;

    public HexWorld(long seed) {
        RANDOM = new Random(seed);
        WORLD_SIZE = 10 * smallHexSize;
        tiles = new TETile[WORLD_SIZE][WORLD_SIZE];
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        return switch (tileNum) {
            case 0 -> Tileset.AVATAR;
            case 1 -> Tileset.WATER;
            case 2 -> Tileset.FLOOR;
            case 3 -> Tileset.FLOWER;
            default -> Tileset.TREE;
        };
    }

    /** Adds a hexagon to the given position in the world.
     * The anchor is the leftmost tile of the bottom line.
     * @param anchorX the x coordinate of the anchor
     * @param anchorY the y coordinate of the anchor
     */
    private void addHexagon(int anchorX, int anchorY) {
        TETile tile = randomTile();
        // bottom half
        for (int j = 0; j < smallHexSize; j += 1) {
            int n = smallHexSize + 2 * j;
            for (int i = 0; i < n; i += 1) {
                int x = anchorX - j + i;
                int y = anchorY + j;
                tiles[x][y] = tile;
            }
        }

        // top half
        anchorX -= smallHexSize - 1;
        anchorY += smallHexSize;
        for (int j = 0; j < smallHexSize; j += 1) {
            int n = smallHexSize * 3 - 2 - 2 * j;
            for (int i = 0; i < n; i += 1) {
                int x = anchorX + j + i;
                int y = anchorY + j;
                tiles[x][y] = tile;
            }
        }
    }

    /** Fills a column of the world with small hexagons if there not already be a hexagon.
     * @param anchorX the x coordinate of the bottom hexagon's anchor
     * @param anchorY the y coordinate of the bottom hexagon's anchor
     * @param n the number of hexagons to fill
     */
    private void fillEmptyStrip(int anchorX, int anchorY, int n) {
        for (int i = 0; i < n; i += 1) {
            int x = anchorX;
            int y = anchorY + 2 * smallHexSize * i;
            addHexagon(x, y);
        }
    }

    /** Fill the world with small hexagons from center to region. */
    private void fillWithHexagons() {
        int x = 5 * smallHexSize - 3; // observe the world pattern
        int y = 0;
        for (int i = 0; i < bigHexSize; i += 1) {
            int x1 = x - (2 * smallHexSize - 1) * i;
            int y1 = y + smallHexSize * i;
            int x2 = x + (2 * smallHexSize - 1) * i;
            int y2 = y + smallHexSize * i;
            int hexNumber = 2 * bigHexSize - 1 - i;
            fillEmptyStrip(x1, y1, hexNumber);
            fillEmptyStrip(x2, y2, hexNumber);
        }
    }

    private void initialize() {
        for (int x = 0; x < WORLD_SIZE; x += 1) {
            for (int y = 0; y < WORLD_SIZE; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void execute() {
        initialize();
        fillWithHexagons();
    }

    public static void main(String[] args) {
        HexWorld world = new HexWorld(1024);
        world.execute();

        TERenderer ter = new TERenderer();
        ter.initialize(world.WORLD_SIZE, world.WORLD_SIZE);
        ter.renderWorld(world.tiles);
    }

}
