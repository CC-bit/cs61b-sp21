package byow.Core;

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
}
