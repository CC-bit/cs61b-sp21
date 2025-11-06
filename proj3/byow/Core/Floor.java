package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Floor implements Serializable {
    private final Random random;
    private final int floorNum;
    private final int floorWidth;
    private final int floorHeight;
    private final TETile[][] floor;
    private final List<Ordinary> spareSpace = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();

    public Floor(Random random, int floorNum, int width, int height) {
        this.random = random;
        this.floorNum = floorNum;
        this.floorWidth = width;
        this.floorHeight = height;
        this.floor = new TETile[width][height];
        generateFloor();
    }

    public int getFloorNumber() {
        return floorNum;
    }

    public int getFloorWidth() {
        return floorWidth;
    }

    public int getFloorHeight() {
        return floorHeight;
    }

    public Room getRandomRoom() {
        int index = RandomUtils.uniform(random, rooms.size());
        return rooms.get(index);
    }

    public TETile[][] getFloorTiles() {
        return floor;
    }

    /** Returns the tile in given ordinary.
     * Returns null if ordinary out of bound. */
    public TETile getTile(int x, int y) {
        if (!(0 <= x && x < floorWidth && 0 <= y && y < floorHeight)) {
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

    private void generateFloor() {
        // init floor tiles with NOTHING
        for (int i = 0; i < floorWidth; i += 1) {
            for (int j = 0; j < floorHeight; j += 1) {
                floor[i][j] = Tileset.NOTHING;
            }
        }

        // Initialize spareSpace, right and bottom edge can not be a valid anchor
        for (int i = 0; i <= floorWidth - World.MIN_SIDE_ROOM; i += 1) {
            for (int j = 0; j < floorHeight - World.MIN_SIDE_ROOM; j += 1) {
                spareSpace.add(new Ordinary(i, j, floorNum));
            }
        }

        int roomNumber = RandomUtils.uniform(
                random, World.MIN_NUM_ROOM, World.MAX_NUM_ROOM);
        for (int i = 0; i < roomNumber; i += 1) {
            Room room = addRoom();
            invalidateTilesForAnchor(room);
        }

        linkAllRooms();
    }

    /** Removes all invalid anchor ordinaries in the floor.
     * Invariant: Rooms never overlap(exclude WALL tile) - Can not put new anchor at Tiles
     * that on the left/top of an exist anchor in some range. */
    public void invalidateTilesForAnchor(Room room) {
        int minX = room.getAnchorX() - World.MIN_SIDE_ROOM + 1;
        int minY = room.getAnchorY() - World.MIN_SIDE_ROOM + 1;
        int maxX = room.getAnchorX() + room.getWidth() - 1;
        int maxY = room.getAnchorY() + room.getWidth() - 1;
        for (int i = minX; i <= maxX; i += 1) {
            for (int j = minY; j <= maxY; j += 1) {
                spareSpace.remove(new Ordinary(i, j, floorNum));
            }
        }
    }

    /**
     * Add a room to the floor.
     * @return the Room instance
     * It will change the tile in the floor.
     */
    public Room addRoom() {
        // generate anchor and room size
        int randomIndex = RandomUtils.uniform(random, spareSpace.size());
        Ordinary ordinary = spareSpace.get(randomIndex);
        int x = ordinary.getX();
        int y = ordinary.getY();
        int width = RandomUtils.uniform(random, World.MIN_SIDE_ROOM, World.MAX_SIDE_ROOM + 1);
        int height = RandomUtils.uniform(random, World.MIN_SIDE_ROOM, World.MAX_SIDE_ROOM + 1);

        // add tiles
        for (int i = x; i < x + width; i += 1) {
            for (int j = y; j < y + height; j += 1) {
                if (i == floorWidth - 1) {
                    addTile(i, j, Tileset.WALL);
                    width = i - x + 1;
                    continue;
                } else if (j == floorHeight - 1) {
                    addTile(i, j, Tileset.WALL);
                    height = j - y + 1;
                    continue;
                } else if (getTile(i, j) != Tileset.NOTHING) {
                    continue;
                }

                if (i == x || i == x + width - 1 || j == y || j == y + height - 1) {
                    addTile(i, j, Tileset.WALL);
                } else {
                    addTile(i, j, Tileset.FLOOR);
                }
            }
        }

        Room room = new Room(ordinary, width, height);
        rooms.add(room);
        return room;
    }

    /**
     * Pick a random floor tile of the given room.
     * @return the ordinary of the picked tile
     */
    public Ordinary randomRoomFloorTile(Room room) {
        int anchorX = room.getAnchorX();
        int anchorY = room.getAnchorY();
        int minX = anchorX + 1;
        int minY = anchorY + 1;
        int maxX = anchorX + room.getWidth() - 2;
        int maxY = anchorY + room.getHeight() - 2;
        int randomX = RandomUtils.uniform(random, minX, maxX + 1);
        int randomY = RandomUtils.uniform(random, minY, maxY + 1);
        return new Ordinary(randomX, randomY, floorNum);
    }

    /**
     * Adds a horizontal hallway between two points.
     * The start point must be in the hallway.
     * @param start the ordinary of the start point
     * @param end the ordinary of the end point
     */
    private void addHorizaontalHallway(Ordinary start, Ordinary end) {
        int x1 = start.getX();
        int y = start.getY();
        int x2 = end.getX();
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }

        for (int i = x1; i <= x2; i += 1) {
            TETile tile = getTile(i, y);
            if (tile == Tileset.WALL || tile == Tileset.NOTHING) {
                addTile(i, y, Tileset.FLOOR);

                TETile leftTile = getTile(i, y + 1);
                TETile rightTile = getTile(i, y - 1);

                if (leftTile == Tileset.NOTHING) {
                    addTile(i, y + 1, Tileset.WALL);
                }

                if (rightTile == Tileset.NOTHING) {
                    addTile(i, y - 1, Tileset.WALL);
                }
            }
        }
    }

    /**
     * Adds a vertical hallway between two points.
     * The start point must be in the hallway.
     * @param start the ordinary of the start point
     * @param end the ordinary of the end point
     */
    private void addVerticalHallway(Ordinary start, Ordinary end) {
        int x = start.getX();
        int y1 = start.getY();
        int y2 = end.getY();
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }

        for (int i = y1; i <= y2; i += 1) {
            TETile tile = getTile(x, i);
            if (tile == Tileset.WALL || tile == Tileset.NOTHING) {
                addTile(x, i, Tileset.FLOOR);

                TETile leftTile = getTile(x - 1, i);
                TETile rightTile = getTile(x + 1, i);

                if (leftTile == Tileset.NOTHING) {
                    addTile(x - 1, i, Tileset.WALL);
                }

                if (rightTile == Tileset.NOTHING) {
                    addTile(x + 1, i, Tileset.WALL);
                }
            }
        }
    }

    /**
     * Adds WALL tiles around the corner tile in order to close the hallway.
     * @param corner the ordinary of the corner tile
     */
    private void addHallwayConerTile(Ordinary corner) {
        int x = corner.getX();
        int y = corner.getY();
        for (int i = x - 1; i <= x + 1; i += 1) {
            for (int j = y - 1; j <= y + 1; j += 1) {
                if (getTile(i, j) == Tileset.NOTHING) {
                    addTile(i, j, Tileset.WALL);
                }
            }
        }
    }

    /**
     * Adds hallway between two rooms.
     * @param roomA the first room
     * @param roomB the second room
     */
    public void linkTwoRooms(Room roomA, Room roomB) {
        Ordinary ordinaryA = randomRoomFloorTile(roomA);
        Ordinary ordinaryB = randomRoomFloorTile(roomB);
        int randomDirection = RandomUtils.uniform(random, 0, 2);
        if (randomDirection == 0) {
            addHorizaontalHallway(ordinaryA, ordinaryB);
            addVerticalHallway(ordinaryB, ordinaryA);
            addHallwayConerTile(new Ordinary(ordinaryB.getX(), ordinaryA.getY(), floorNum));
        } else {
            addVerticalHallway(ordinaryA, ordinaryB);
            addHorizaontalHallway(ordinaryB, ordinaryA);
            addHallwayConerTile(new Ordinary(ordinaryA.getX(), ordinaryB.getY(), floorNum));
        }
    }

    /** Returns the closest room to roomA in roomList.*/
    private Room closestRoom(Room roomA, List<Room> roomList) {
        double x1 = roomA.getAnchorX();
        double y1 = roomA.getAnchorY();
        double closest = Double.POSITIVE_INFINITY;
        Room roomB = null;
        for (Room room : roomList) {
            double x2 = room.getAnchorX();
            double y2 = room.getAnchorY();
            double distance = Math.hypot(x2 - x1, y2 - y1);
            if (distance < closest) {
                closest = distance;
                roomB = room;
            }
        }
        return roomB;
    }

    /**
     * Iterabally link two rooms in different room list.
     * @param connectedRooms the list in which the first room was picked
     * @param unconnectedRooms the list in which the second room was picked
     */
    private void linkIter(List<Room> connectedRooms, List<Room> unconnectedRooms) {
        if (unconnectedRooms.isEmpty()) {
            return;
        }

        int connectedRoomsNum = connectedRooms.size();
        double[] probabilities = new double[connectedRoomsNum];
        for (int i = 0; i < connectedRoomsNum; i += 1) {
            if (connectedRoomsNum == 1) {
                probabilities[i] = 1;
                continue;
            }
            double pOfNewRoom = 0.6;
            double pOfOtherRoom = (1 - pOfNewRoom) / (connectedRoomsNum - 1);
            if (i == connectedRoomsNum - 1) {
                probabilities[i] = pOfNewRoom;
            } else {
                probabilities[i] = pOfOtherRoom;
            }
        }
        Room roomA = connectedRooms.get(RandomUtils.discrete(random, probabilities));

        Room roomB = closestRoom(roomA, unconnectedRooms);

        linkTwoRooms(roomA, roomB);

        connectedRooms.add(roomB);
        unconnectedRooms.remove(roomB);

        linkIter(connectedRooms, unconnectedRooms);
    }

    /**
     * Link all rooms in the floor.
     */
    public void linkAllRooms() {
        List<Room> unconnectedRooms = new ArrayList<>(rooms);
        List<Room> connectedRooms = new ArrayList<>();
        Room startRoom = unconnectedRooms.get(
                RandomUtils.uniform(random, unconnectedRooms.size())
        );

        connectedRooms.add(startRoom);
        unconnectedRooms.remove(startRoom);
        linkIter(connectedRooms, unconnectedRooms);
    }

}
