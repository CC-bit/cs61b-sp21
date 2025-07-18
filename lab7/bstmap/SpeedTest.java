package bstmap;

import org.antlr.v4.runtime.misc.Utils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class SpeedTest {

    /** Make a bunch of speed tests and write the results in file. */
    @Test
    public void test() throws IOException {
        int L = 2;
        int N = 100000;
        File result = new File("speedTestResults.txt");
        StringBuilder sb = new StringBuilder();
        sb.append("Time needed to put N random strings of length 2 into map.\n\n")
                .append("N       BSTMap    TreeMap    HashMap\n");

        for (int i = 0; i < 8; i++) {
            BSTMap<String, Integer> bst = new BSTMap<>();
            TreeMap<String, Integer> tree = new TreeMap<>();
            HashMap<String, Integer> hash = new HashMap<>();
            double bstTime = InsertRandomSpeedTest.insertRandom(bst, N, L);
            double treeTime = InsertRandomSpeedTest.insertRandom(tree, N, L);
            double hashTime = InsertRandomSpeedTest.insertRandom(hash, N, L);
            sb.append(N).append("    ")
                    .append(bstTime).append("    ")
                    .append(treeTime).append("    ")
                    .append(hashTime).append("\n");

            N *= 2;
        }

        Utils.writeFile(result.getPath(), sb.toString());
    }
}
