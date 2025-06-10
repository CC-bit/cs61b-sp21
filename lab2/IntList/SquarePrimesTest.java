package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void allPrimes() {
        IntList lst = IntList.of(19, 23, 29, 31, 37);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("361 -> 529 -> 841 -> 961 -> 1369", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void allComps() {
        IntList lst = IntList.of(20, 24, 32, 35, 40);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("20 -> 24 -> 32 -> 35 -> 40", lst.toString());
        assertFalse(changed);
    }

    @Test
    public void lst1To10() {
        IntList lst = IntList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 9 -> 4 -> 25 -> 6 -> 49 -> 8 -> 9 -> 10", lst.toString());
        assertTrue(changed);
    }
    @Test

    public void lst11To20() {
        IntList lst = IntList.of( 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("121 -> 12 -> 169 -> 14 -> 15 -> 16 -> 289 -> 18 -> 361 -> 20", lst.toString());
        assertTrue(changed);
    }
}
