package byow.Core;

public class Room {
    private final Floor floor;
    private final Ordinary anchor;
    private final int width;
    private final int height;

    /** Generate a room.
     * The anchor is the leftmost bottom tile of the room.
     * The width or height of the rectangle room should between 7 and 11(include walls).
     * @param anchor the ordinary of the anchor */
    public Room(Floor floor, Ordinary anchor, int width, int height) {
        this.floor = floor;
        this.anchor = anchor;
        this.width = width;
        this.height = height;
    }

    public Floor getFloor() {
        return floor;
    }
    public Ordinary getAnchor() {
        return anchor;
    }
    public int getAnchorX() {
        return anchor.getX();
    }
    public int getAnchorY() {
        return anchor.getY();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}
