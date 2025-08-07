package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 * <p>
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author dhzp
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;

    /** The map size. */
    private int size;

    /** The loadFactor. */
    private final double factor;

    /** Constructors */
    public MyHashMap() {
        this(16);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        factor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    @SuppressWarnings("unchecked")
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    /** Return the bucket index of given key. */
    private int index(Collection<Node>[] b, K key) {
        return Math.floorMod(key.hashCode(), b.length);
    }
    private int index(K key) {
        return index(buckets, key);
    }


    private Node getNode(K key) {
        return getNode(buckets, key);
    }
    /** Return the node of given key or null if not contains that key. */
    private Node getNode(Collection<Node>[] b, K key) {
        Collection<Node> bucket = b[index(b, key)];
        if (bucket != null) {
            for (Node n : bucket) {
                if (n.key.equals(key)) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public void clear() {
        buckets = createTable(16);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override
    public V get(K key) {
        V value = null;
        Node n = getNode(key);
        if (n != null) {
            value = n.value;
        }
        return value;
    }

    @Override
    public int size() {
        return size;
    }

    /* Put key and value in the given buckets. */
    private void putBuckets(Collection<Node>[] b, K key, V value) {
        int bucketIndex = index(b, key);
        if (b[bucketIndex] == null) {
            b[bucketIndex] = createBucket();
        }
        Collection<Node> bucket = b[bucketIndex];
        Node n = getNode(b, key);
        if (n != null) {
            n.value = value;
        } else {
            bucket.add(createNode(key, value));
        }
    }

    @Override
    public void put(K key, V value) {
        if ((double) (size + 1) / buckets.length > factor) {
            Collection<Node>[] newBuckets = createTable(2 * buckets.length);
            for (K k : keySet()) {
                putBuckets(newBuckets, k, get(k));
            }
            buckets = newBuckets;
        }
        if (!containsKey(key)) {
            size++;
        }
        putBuckets(buckets, key, value);
    }

    @Override
    public Set<K> keySet() {
        return new KeySet(this);
    }

    private class KeySet extends AbstractSet<K> {
        MyHashMap<K, V> map;

        KeySet(MyHashMap<K, V> map) {
            this.map = map;
        }

        @Override
        public Iterator<K> iterator() {
            return map.iterator();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            return map.containsKey((K) o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object key : c) {
                if (!contains(key)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public V remove(K key) {
        Node n = getNode(key);
        if (n == null) {
            return null;
        }
        V value = n.value;
        buckets[index(key)].remove(n);
        size--;
        return value;
    }

    @Override
    public V remove(K key, V value) {
        Node n = getNode(key);
        if (n != null) {
            if (n.value.equals(value)) {
                buckets[index(key)].remove(n);
                size--;
                return value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyMapIterator();
    }

    private class MyMapIterator implements Iterator<K> {
        int curIndex;
        Iterator<Node> iter;

        MyMapIterator() {
            curIndex = findIndex(-1);
            if (curIndex >= 0) {
                iter = buckets[curIndex].iterator();
            }
        }

        private int findIndex(int cur) {
            for (int i = cur + 1; i < buckets.length; i++) {
                if (buckets[i] != null) {
                    cur = i;
                    break;
                }
            }
            return cur;
        }

        @Override
        public boolean hasNext() {
            if (curIndex < 0 || buckets[curIndex] == null) {
                return false;
            }
            return iter.hasNext();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node n = iter.next();
            if (!iter.hasNext()) {
                int next = findIndex(curIndex);
                if (curIndex == next) {
                    curIndex = -1;
                } else {
                    curIndex = next;
                    iter = buckets[curIndex].iterator();
                }
            }
            return n.key;
        }
    }
}
