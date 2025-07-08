package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    /** Make a bunch of random calls to StudentArrayDeque and ArraySolution,
    * check if they behave the same. */
    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> stuArray = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solArray = new ArrayDequeSolution<>();
        StringBuilder msg = new StringBuilder();

        int N = 400000;
        for (int i = 0; i < N; i++) {
            int operation = StdRandom.uniform(0, 3);

            if (operation == 0) {
                //addFirst
                int randVal = StdRandom.uniform(0, 100);
                stuArray.addFirst(randVal);
                solArray.addFirst(randVal);
                msg.append("addFirst(").append(randVal).append(")").append("\n");
                String errorMsg = msg.toString();
                Integer stuFirst = stuArray.get(0);
                Integer solFirst = solArray.get(0);
                assertEquals(errorMsg, solFirst, stuFirst);
            } else if (operation == 1) {
                //addLast
                int randVal = StdRandom.uniform(0, 100);
                stuArray.addLast(randVal);
                solArray.addLast(randVal);
                msg.append("addLast(").append(randVal).append(")").append("\n");
                String errorMsg = msg.toString();
                int lastIndex = solArray.size() - 1;
                Integer stuLast= stuArray.get(lastIndex);
                Integer solLast= solArray.get(lastIndex);
                assertEquals(errorMsg, solLast, stuLast);
            } else if (operation == 2) {
                //removeFirst
                if (solArray.isEmpty()) {
                    assertTrue(stuArray.isEmpty());
                    continue;
                }
                Integer stuFirst = stuArray.removeFirst();
                Integer solFirst = solArray.removeFirst();
                msg.append("removeFirst()").append("\n");
                String errorMsg = msg.toString();
                assertEquals(errorMsg, solFirst, stuFirst);
            } else if (operation == 3) {
                //removeLast
                if (solArray.isEmpty()) {
                    assertTrue(stuArray.isEmpty());
                    continue;
                }
                Integer stuLast = stuArray.removeLast();
                Integer solLast = solArray.removeLast();
                msg.append("removeLast()").append("\n");
                String errorMsg = msg.toString();
                assertEquals(errorMsg, solLast, stuLast);
            }
        }
    }

}
