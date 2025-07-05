package deque;

public class ArrayDeque<T> implements Deque<T>{
    private T[] items;
    private final double r;
    private int front;
    private int size;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        r = 0.25;
        front = 0;
        size = 0;
    }

    /** Returns true if the array with size items is circular - the index of back item isn't after that of front. */
    private boolean isCircular(int size) {
        return front + size > items.length; // front + size - 1 > items.length - 1
    }

    /** Returns the number of items from front of deque to the end of array. */
    private int numFrontToEnd() {
        int number;
        if (isCircular(size)) {
            number = items.length - front; //(items.length - 1 - front) + 1
        } else {
            number = size;
        }
        return number;
    }

    /** Returns the number of items from 0 of array to back of deque.
     *  Returns 0 if not circular.
     */
    private int numZeroToBack() {
        return size - numFrontToEnd();
    }

    /** Returns the array index of back item of deque. */
    private int backIndex() {
        int index;
        if (isCircular(size)) {
            index = numZeroToBack() - 1;
        } else {
            index = front + size - 1;
        }
        return index;
    }

    /** Returns the correct array index weather the given index is correct. */
    private int arrayIndex(int index) {
        int newIndex;
        if (index < 0) {
            newIndex = items.length + index; //items.length - 1 - (-index) + 1
        } else if (index > items.length - 1) {
            newIndex = index - items.length; //(index - (items.length - 1)) - 1
        } else {
            newIndex = index;
        }
        return newIndex;
    }

    /** Resize array to the size of capacity.
     *  first item in index 0.
     */
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        System.arraycopy(items, front, a, 0, numFrontToEnd());
        System.arraycopy(items, 0, a, numFrontToEnd(), numZeroToBack());
        front = 0;
        items = a;
    }

    /** Adds an item of type T to the front of the deque.
     * Assume that item is never null. */
    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        int newIndex = arrayIndex(front - 1);
        items[newIndex] = item;
        front = newIndex;
        size += 1;
    }

    /** Adds an item of type T to the end of the deque. */
    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        int newIndex = arrayIndex(backIndex() + 1);
        items[newIndex] = item;
        size += 1;
    }

    /** Returns the number of items in the deque. */
    @Override
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last,
     *  separated by a space. Then print out a new line.
     */
    @Override
    public void printDeque() {
        int fronIndex = front;
        int backIndex = 0;
        for (int i = 1; i <= numFrontToEnd(); i += 1) {
            System.out.print(items[fronIndex] + " ");
            fronIndex += 1;
        }
        for (int i = 1; i <= numZeroToBack(); i += 1) {
            System.out.print(items[backIndex] + " ");
            backIndex += 1;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     *  If no such item exists, returns null.
     */
    @Override
    public T removeFirst() {
        if (size == 0) {
            System.out.println("ArrayDeque removeFirst: No such item exists.");
            return null;
        }
        if (size - 1 < items.length * r) {
            resize(size * 2);
        }
        T item = items[front];
        items[front] = null;
        front = arrayIndex(front + 1);
        size -= 1;
        return item;
    }

    /** Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
      */
    @Override
    public T removeLast() {
        if (size == 0) {
            System.out.println("ArrayDeque removeLast: No such item exists.");
            return null;
        }
        if (size - 1 < items.length * r) {
            resize(size * 2);
        }
        T item = items[backIndex()];
        items[backIndex()] = null;
        size -= 1;
        return item;
    }

    /** Gets the item at the given index, where 0 is the front, 1 is the next item,
     *  and so forth. If no such item exists, returns null.
     */
    @Override
    public T get(int index) {
        if (index + 1 > size) {
            if (size == 0) {
                System.out.println("ArrayDeque get: The deque has no item.");
            } else {
                System.out.println("ArrayDeque get: Index out of bound.");
            }
            return null;
        }
        return items[arrayIndex(front + index)];
    }
}
