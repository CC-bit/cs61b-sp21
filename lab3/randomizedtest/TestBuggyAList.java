package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        int[] test = {2, 7, 6};
        AListNoResizing<Integer> noResizing = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();
        for (int value : test) {
            noResizing.addLast(value);
            buggy.addLast(value);
        }
        for (int i = 0; i < test.length; i += 1) {
            assertEquals(noResizing.removeLast(), buggy.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> noResizing = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                noResizing.addLast(randVal);
                buggy.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeNoResizing = noResizing.size();
                int sizeBuggy = buggy.size();
                assertEquals(sizeNoResizing, sizeBuggy);

            } else if (operationNumber == 2) {
                // getLast
                if (noResizing.size() == 0 || buggy.size() == 0) continue;
                int lastNoResizing = noResizing.getLast();
                int lastBuggy = buggy.getLast();
                assertEquals(lastNoResizing, lastBuggy);
            } else if (operationNumber == 3) {
                // removeLast
                if (noResizing.size() == 0 || buggy.size() == 0) continue;
                int lastNoResizing = noResizing.removeLast();
                int lastBuggy = buggy.removeLast();
                assertEquals(lastNoResizing, lastBuggy);
            }
        }
    }
 }
