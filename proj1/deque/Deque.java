package deque;

import java.util.Iterator;

public interface Deque<T> extends Iterable<T> {
    void addFirst(T item);
    void addLast(T item);

    /** Returns true if deque is empty, false otherwise. */
    default boolean isEmpty() {
        return size() == 0;
    }

    int size();
    void printDeque();
    T removeFirst();
    T removeLast();
    T get(int index);
    Iterator<T> iterator();
    boolean equals(Object o);
}
