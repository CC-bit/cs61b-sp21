package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private final Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public LinkedListDeque(T x) {
        this();
        addFirst(x);
    }

    /** Adds an item of type T to the front of the deque. */
    @Override
    public void addFirst(T item) {
        size += 1;
        new Node(sentinel, item, sentinel.next);
    }

    /** Adds an item of type T to the end of the deque. */
    @Override
    public void addLast(T item) {
        size += 1;
        new Node(sentinel.prev, item, sentinel);
    }

    /** Returns true if deque is empty, false otherwise. */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Return the number of items in the deque. */
    @Override
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line.
     */
    @Override
    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque. If no such item exists,
     *  returns null.
     */
    @Override
    public T removeFirst() {
        if (sentinel.next == sentinel) {
            System.out.println("LinkedListDeque removeFirst: No such item exists.");
            return null;
        }
        size -= 1;
        T item = sentinel.next.item;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        return item;
    }

    /** Removes and returns the item at the back of the deque. If no such item exists,
     *  returns null.
     */
    @Override
    public T removeLast() {
        if (sentinel.prev == sentinel) {
            System.out.println("LinkedListDeque removeLast: No such item exists.");
            return null;
        }
        size -= 1;
        T item = sentinel.prev.item;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        return item;
    }

    /** Gets the item at the given index, where 0 is the front, 1 is the next item,
     *  and so forth. If no such item exists, returns null.
     */
    @Override
    public T get(int index) {
        if (index + 1 > size) {
            if (size == 0) {
                System.out.println("LinkedListDeque get: The deque has no item.");
            } else {
                System.out.println("LinkedListDeque get: Index out of bound.");
            }
            return null;
        }

        Node p = sentinel.next;
        for (int i = 0; i != index; i += 1) {
            p = p.next;
        }
        return p.item; //p may be the node at index or sentinel. Both works.
    }

    /** Same as get, but uses recursion. */
    public T getRecursive(int index) {
        if (index + 1 > size) {
            if (size == 0) {
                System.out.println("LinkedListDeque get: The deque has no item.");
            } else {
                System.out.println("LinkedListDeque get: Index out of bound.");
            }
            return null;
        }
        Node p = sentinel.next;
        return this.getHelper(index, p);
    }

    /** Return the kth item from pointer p. */
    private T getHelper(int k, Node p) {
        if (k == 0) {
            return p.item;
        }
        return getHelper(k - 1, p.next);
    }

    /** Returns an iterator. */
    @Override
    public Iterator<T> iterator() {
        return new LinkDequeIterator();
    }

    /** Iterator for LinkedListDeque. */
    private class LinkDequeIterator implements Iterator<T> {
        private Node position;

        public LinkDequeIterator() {
            position = sentinel;
        }
        public boolean hasNext() {
            return !(position.next == sentinel);
        }

        public T next() {
            if (!hasNext()) {
                return null;
            }
            position = position.next;
            return position.item;
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
        if (!(o instanceof LinkedListDeque)) {
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

    /** Double ended node with prev node, item, next node in it. */
    public class Node {
        public Node prev;
        public T item;
        public Node next;

        public Node(Node p, T i, Node n) {
            prev = p;
            item = i;
            next = n;
            if (n != null) {
                next.prev = this;
            }
            if (p != null) {
                prev.next = this;
            }
        }
    }
}
