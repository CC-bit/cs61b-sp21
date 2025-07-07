package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private final double r;
    private int front; // The index of the first item in the array.
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

    /** Returns the number of items from 0 of the array to back of deque.
     *  Returns 0 if not circular.
     */
    private int numZeroToBack() {
        return size - numFrontToEnd();
    }

    /** Returns the array index of back item. */
    private int backIndex() {
        int index;
        if (isCircular(size)) {
            index = numZeroToBack() - 1;
        } else {
            index = front + size - 1;
        }
        return index;
    }

    /** Returns the correct array index according to the deque index. */
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

    /** Resize the array to the size of capacity.
     *  The first item is in index 0.
     */
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int num = numFrontToEnd();
        System.arraycopy(items, front, a, 0, num);
        System.arraycopy(items, 0, a, num, size - num);
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
        if (items.length >= 16 && size - 1 < items.length * r) {
            resize(items.length / 2);
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
        if (items.length >= 16 && size - 1 < items.length * r) {
            resize(items.length / 2);
        }
        int index = backIndex();
        T item = items[index];
        items[index] = null;
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

    /** Returns an iterator. */
    @Override
    public Iterator<T> iterator() {
        return new arrayDequeIterator();
    }

    /** Iterator for ArrayDeque. */
    private class arrayDequeIterator implements Iterator<T> {
        private int position;

        public arrayDequeIterator() {
            position = 0;
        }

        public boolean hasNext() {
            return position < size;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more item");
            }
            int i = position;
            position++;
            return get(i);
        }
    }

    /** Returns whether the parameter o is equal to the Deque. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }

        Deque<T> that = (Deque<T>) o;
        if (that.size() != size) {
            return false;
        }
        int j = 0;
        for (T i : this) {
            if (!(that.get(j).equals(i))) {
                return false;
            }
            j++;
        }
        return true;
    }
}
