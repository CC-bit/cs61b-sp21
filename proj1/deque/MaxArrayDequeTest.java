package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {

    /** Make a deque with random items, number of items, type.*/
    @Test
    public void randomTest() {
        IntComparator intC = new IntComparator();
        MaxArrayDeque<Integer> intDeque = new MaxArrayDeque<>(intC);

        int N = 20;
        for (int i = 0; i < N; i += 1) {
            int item = StdRandom.uniform(-100, 100);
            intDeque.addLast(item);
        }
        System.out.print("Deque: ");
        intDeque.printDeque();
        System.out.print("Max: " + intDeque.max());
    }

    /** Compare two int items. */
    public static class IntComparator implements Comparator<Integer> {

        /**
         * Returns the value of o1 minus o2.
         */
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    }

    /** Compare two double items. */
    public static class DoubleComparator implements Comparator<Double> {

        /** Returns the value of o1 minus o2. */
        @Override
        public int compare(Double o1, Double o2) {
            int c;
            if (o1 < o2) {
                c = -1;
            } else if (o1.equals(o2)) {
                c = 0;
            } else {
                c = 1;
            }
            return c;
        }
    }

    /** Compare two boolean items. */
    public static class BooleanComparator implements Comparator<Boolean> {

        /** Returns the value of o1 minus o2.
         * Treat true values bigger than false.
         */
        @Override
        public int compare(Boolean o1, Boolean o2) {
            int bool;
            if (o1 && !o2) {
                bool = 1;
            } else if (!o1 && o2){
                bool = -1;
            } else {
                bool = 0;
            }
            return bool;
        }
    }

    /** Compare two character items. */
    public static class CharComparator implements Comparator<Character> {

        /** Returns 1 if o1 bigger than o2, -1 smaller, 0 equals.
         *  The front character in Unicode is smaller.
         */
        @Override
        public int compare(Character o1, Character o2) {
            int t1 = o1, t2 = o2;
            Comparator<Integer> c = new IntComparator();
            return c.compare(t1, t2);
        }
    }

    /** Compare two byte items. */
    public static class ByteComparator implements Comparator<Byte> {

        /** Returns 1 if o1 bigger than o2, -1 smaller, 0 equals.*/
        @Override
        public int compare(Byte o1, Byte o2) {
            int t1 = o1, t2 = o2;
            Comparator<Integer> c = new IntComparator();
            return c.compare(t1, t2);
        }
    }
}
