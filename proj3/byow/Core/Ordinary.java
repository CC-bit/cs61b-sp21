package byow.Core;

import java.util.Objects;

public class Ordinary {
    private final int x;
    private final int y;

    public Ordinary(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ordinary)) {
            return false;
        }
        Ordinary ordinary = (Ordinary) obj;
        return ordinary.getX() == x && ordinary.getY() == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
