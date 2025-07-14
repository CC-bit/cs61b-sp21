package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> givenC;

    public MaxArrayDeque(Comparator<T> c) {
        givenC = c;
    }

    /** Returns the maximum element in the deque as governed by the
     * previously given Comparator. If the deque is empty, returns null.
     */
    public T max() {
        return max(givenC);
    }

    /** Returns the maximum element in the deque as governed by the
     * parameter Comparator. If the deque is empty, returns null.
     */
    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T max = get(0);
        for (int i = 0; i < size(); i++) {
            T item = get(i);
            if (c.compare(item, max) > 0) {
                max = get(i);
            }
        }
        return max;
    }
}
