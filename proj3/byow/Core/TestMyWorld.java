package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;

import java.util.Random;

public class TestMyWorld {

    @org.junit.Test
    public void randomUtilsTest() {
        int seed = 1024;
        Random random = new Random(seed);
        double d = RandomUtils.uniform(random);
        System.out.println(d);
        double m = RandomUtils.uniform(random);
        System.out.println(m);

    }

    @Test
    public void testInputString() {
        Engine engine = new Engine();
        String input = "n9087swsdawasd";
        TETile[][] result = engine.interactWithInputString(input);
    }
}
