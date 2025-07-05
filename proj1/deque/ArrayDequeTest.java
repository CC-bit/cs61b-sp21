package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     * && is the "and" operation. */
    @Test
    public void addIsEmptySizeTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    /** Adds an item, then removes an item, and ensures that dll is empty afterward. */
    @Test
    public void addRemoveTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    /* Tests removing from an empty deque. */
    @Test
    public void removeEmptyTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    /** Check if you can create ArrayDeque with different parameterized types*/
    @Test
    public void multipleParamTest() {

        ArrayDeque<String>  lld1 = new ArrayDeque<>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
        System.out.println(s + d + b);
    }

    /** check if null is return when removing from an empty ArrayDeque. */
    @Test
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();

        assertNull("Should return null when removeFirst is called on an empty Deque,", lld1.removeFirst());
        assertNull("Should return null when removeLast is called on an empty Deque,", lld1.removeLast());

    }

    /** Add large number of elements to deque; check if order is correct. */
    @Test
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    /** Make a bunch of operations, each of them randomly.
     *  Check if both LinkedListDeque and ArrayDeque behavior the same.
     *  Finally print the deque. */
    @Test
    public void randomizedTest() {
        LinkedListDeque<Integer> linkList = new LinkedListDeque<>();
        ArrayDeque<Integer> arrayList = new ArrayDeque<>();

        int N = 600000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 6);
            if (operationNumber == 0) {
                //addFirst
                int randVal = StdRandom.uniform(0, 100);
                linkList.addFirst(randVal);
                arrayList.addFirst(randVal);
                assertEquals(linkList.get(0), arrayList.get(0));
            } else if (operationNumber == 1) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                linkList.addLast(randVal);
                arrayList.addLast(randVal);
                assertEquals(linkList.get(linkList.size() - 1), arrayList.get(linkList.size() - 1));
            } else if (operationNumber == 2) {
                //isEmpty
                assertEquals(linkList.isEmpty(), arrayList.isEmpty());
            } else if (operationNumber == 3) {
                // size
                int sizeLinkList = linkList.size();
                int sizeArray = arrayList.size();
                assertEquals(sizeLinkList, sizeArray);
            } else if (operationNumber == 4) {
                // removeFirst
                assertEquals(linkList.removeFirst(), arrayList.removeFirst());
            } else if (operationNumber == 5) {
                // removeLast
                assertEquals(linkList.removeLast(), arrayList.removeLast());
            } else if (operationNumber == 6) {
                // get
                int randIndex = StdRandom.uniform(0, linkList.size() - 1);
                assertEquals(linkList.get(randIndex), arrayList.get(randIndex));
            }
        }
        System.out.println("LinkedListDeque: ");
        linkList.printDeque();
        System.out.println("ArrayDeque: ");
        arrayList.printDeque();
    }
}
