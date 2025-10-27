package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class FloorManager {
    public static final int MIN_ROOM_NUM = 5;
    public static final int MAX_ROOM_NUM = 9;
    public static final int MIN_ROOM_SIDE = 7;
    public static final int MAX_ROOM_SIDE = 11;
    private final QuickRmList<Ordinary> spareSpace = new QuickRmList<>();
    private final QuickRmList<Room> rooms = new QuickRmList<>();
    private final Floor floor;

    public FloorManager(Floor floor) {
        this.floor = floor;
        // Initialize spareSpace, right and bottom edge can not be a valid anchor
        for (int i = 0; i < Floor.WIDTH; i += 1) {
            for (int j = 0; j < Floor.HEIGHT; j += 1) {
                if (i < Floor.WIDTH - MIN_ROOM_SIDE + 1 && j < Floor.HEIGHT - MIN_ROOM_SIDE + 1) {
                    spareSpace.add(new Ordinary(i, j));
                }
            }
        }
    }

    /** Removes all invalid anchor ordinaries in the floor.
     * Invariant: Rooms never overlap(exclude WALL tile) - Can not put new anchor at Tiles
     * that on the left/top of an exist anchor in some range. */
    public void invalidateTilesForAnchor(Room room) {
        int minX = room.getAnchorX() - MIN_ROOM_SIDE + 1;
        int minY = room.getAnchorY() - MIN_ROOM_SIDE + 1;
        int maxX = room.getAnchorX() + room.getWidth() - 1;
        int maxY = room.getAnchorY() + room.getWidth() - 1;
        for (int i = minX; i <= maxX; i += 1) {
            for (int j = minY; j <= maxY; j += 1) {
                spareSpace.remove(new Ordinary(i, j));
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
        int randomIndex = RandomUtils.uniform(Engine.random, spareSpace.size());
        Ordinary ordinary = spareSpace.get(randomIndex);
        int x = ordinary.getX();
        int y = ordinary.getY();
        int width = RandomUtils.uniform(Engine.random, MIN_ROOM_SIDE, MAX_ROOM_SIDE + 1);
        int height = RandomUtils.uniform(Engine.random, MIN_ROOM_SIDE, MAX_ROOM_SIDE + 1);

        // add tiles
        for (int i = x; i < x + width; i += 1) {
            for (int j = y; j < y + height; j += 1) {
                if (floor.getTile(i, j) != Tileset.NOTHING) {
                    continue;
                }
                if (i == x || i == x + width - 1 || j == y || j == y + height - 1) {
                    floor.addTile(i, j, Tileset.WALL);
                } else if (i == Floor.WIDTH - 1 || j == Floor.HEIGHT - 1) {
                    floor.addTile(i, j, Tileset.WALL);
                } else {
                    floor.addTile(i, j, Tileset.FLOOR);
                }
            }
        }

        Room room = new Room(floor, ordinary, width, height);
        rooms.add(room);
        return room;
    }

    /**
     * Pick a random floor tile of the given room.
     * @return the ordinary of the picked tile
     */
    private Ordinary randomRoomFloorTile(Room room) {
        int anchorX = room.getAnchorX();
        int anchorY = room.getAnchorY();
        int minX = anchorX + 1;
        int minY = anchorY + 1;
        int maxX = anchorX + room.getWidth() - 2;
        int maxY = anchorY + room.getHeight() - 2;
        int randomX = RandomUtils.uniform(Engine.random, minX, maxX + 1);
        int randomY = RandomUtils.uniform(Engine.random, minY, maxY + 1);
        return new Ordinary(randomX, randomY);
    }

    /**
     * Adds a horizontal hallway to the floor.
     * @param x1 the x ordinary of the start point
     * @param x2 the x ordinary of the end point
     * @param y the y ordinary of the hallway
     */
    private void addHorizaontalHallway(int x1, int x2, int y) {
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }

        for (int i = x1; i <= x2; i += 1) {
            TETile tile = floor.getTile(i, y);
            if (tile == Tileset.WALL || tile == Tileset.NOTHING) {
                floor.addTile(i, y, Tileset.FLOOR);

                TETile leftTile = floor.getTile(i, y + 1);
                TETile rightTile = floor.getTile(i, y - 1);

                if (leftTile == Tileset.NOTHING) {
                    floor.addTile(i, y + 1, Tileset.WALL);
                }

                if (rightTile == Tileset.NOTHING) {
                    floor.addTile(i, y - 1, Tileset.WALL);
                }
            }
        }
    }

    /**
     * Adds a vertical hallway to the floor.
     * @param y1 the y ordinary of the start point
     * @param y2 the y ordinary of the end point
     * @param x the x ordinary of the hallway
     */
    private void addVerticalHallway(int y1, int y2, int x) {
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }

        for (int i = y1; i <= y2; i += 1) {
            TETile tile = floor.getTile(x, i);
            if (tile == Tileset.WALL || tile == Tileset.NOTHING) {
                floor.addTile(x, i, Tileset.FLOOR);

                TETile leftTile = floor.getTile(x - 1, i);
                TETile rightTile = floor.getTile(x + 1, i);

                if (leftTile == Tileset.NOTHING) {
                    floor.addTile(x - 1, i, Tileset.WALL);
                }

                if (rightTile == Tileset.NOTHING) {
                    floor.addTile(x + 1, i, Tileset.WALL);
                }
            }
        }
    }

    /**
     * Adds WALL tiles around the coner tile to close the hallway.
     * @param x the x ordinary of the coner tile
     * @param y the y ordinary of the coner tile
     */
    private void addHallwayConerTile(int x, int y) {
        for (int i = x - 1; i <= x + 1; i += 1) {
            for (int j = y - 1; j <= y + 1; j += 1) {
                if (floor.getTile(i, j) == Tileset.NOTHING) {
                    floor.addTile(i, j, Tileset.WALL);
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
        Ordinary randomA = randomRoomFloorTile(roomA);
        Ordinary randomB = randomRoomFloorTile(roomB);
        int ordinaryAx = randomA.getX();
        int ordinaryBx = randomB.getX();
        int ordinaryAy = randomA.getY();
        int ordinaryBy = randomB.getY();
        int randomDirection = RandomUtils.uniform(Engine.random, 0, 2);
        if (randomDirection == 0) {
            addHorizaontalHallway(ordinaryAx, ordinaryBx, ordinaryAy);
            addVerticalHallway(ordinaryAy, ordinaryBy, ordinaryBx);
            addHallwayConerTile(ordinaryBx, ordinaryAy);
        } else {
            addVerticalHallway(ordinaryAy, ordinaryBy, ordinaryAx);
            addHorizaontalHallway(ordinaryAx, ordinaryBx, ordinaryBy);
            addHallwayConerTile(ordinaryAx, ordinaryBy);
        }
    }

    /** Returns the closest room to roomA in roomList.*/
    private Room closestRoom(Room roomA, QuickRmList<Room> roomList) {
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
    private void linkIter(QuickRmList<Room> connectedRooms,
                          QuickRmList<Room> unconnectedRooms) {
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
        Room roomA = connectedRooms.get(RandomUtils.discrete(Engine.random, probabilities));

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
        QuickRmList<Room> unconnectedRooms = new QuickRmList<>(rooms);
        QuickRmList<Room> connectedRooms = new QuickRmList<>();
        Room startRoom = unconnectedRooms.get(
                RandomUtils.uniform(Engine.random, unconnectedRooms.size())
        );

        connectedRooms.add(startRoom);
        unconnectedRooms.remove(startRoom);
        linkIter(connectedRooms, unconnectedRooms);
    }

    private static class QuickRmList<T> implements Iterable<T> {
        private final List<T> list = new ArrayList<>();
        private final Map<T, Integer> map = new HashMap<>();
        private int size;

        public QuickRmList(QuickRmList<T> otherList) {
            for (T item : otherList) {
                add(item);
            }
        }

        public QuickRmList() {}

        public boolean add(T object) {
            if (map.containsKey(object)) {
                throw new IllegalArgumentException("Item already in the list.");
            }

            list.add(object);
            map.put(object, list.size() - 1);

            size += 1;
            return true;
        }

        public boolean remove(T object) {
            if (!map.containsKey(object)) {
                return false;
            }

            int index = map.get(object);
            T lastItem = list.get(list.size() - 1);

            list.set(index, lastItem);
            map.put(lastItem, index);

            map.remove(object);
            list.remove(list.size() - 1);

            size -= 1;
            return true;
        }

        public T get(int index) {
            return list.get(index);
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }
    }

}
