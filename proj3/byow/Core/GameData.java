package byow.Core;

import java.io.Serializable;

public class GameData implements Serializable {
    private final String seed;
    private final World world;

    public GameData(String seed, World world) {
        this.seed = seed;
        this.world = world;
    }

    public String getSeed() {
        return seed;
    }
    public World getWorld() {
        return world;
    }
}
