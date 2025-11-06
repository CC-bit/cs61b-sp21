package byow.Core;

import java.io.Serializable;
import java.util.Objects;

public class Ordinary implements Serializable {
    private final int x;
    private final int y;
    private final int z;

    public Ordinary(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
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
        return ordinary.getX() == x && ordinary.getY() == y && ordinary.getZ() == z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
