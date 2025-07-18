package bstmap;


import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K , V> {
    private Node root;
    private int size;

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    /** Find the nearest node from the given key. */
    private Node find(Node n, K key) {
        if (n == null) {
            return null;
        }
        if (key.compareTo(n.key) == 0) {
            return n;
        } else if (key.compareTo(n.key) < 0) {
            return find(n.left, key);
        } else {
            return find(n.right, key);
        }
    }

    @Override
    public boolean containsKey(K key) {
        return (find(root, key) != null);
    }

    @Override
    public V get(K key) {
        Node n = find(root, key);
        if (n != null) {
            return n.value;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        size++;
        if (root == null) {
            root = new Node(key, value);
            return;
        }
        Node p = root;
        Node item = new Node(key, value);
        while (true) {
            if (key.compareTo(p.key) == 0) {
                p.value = value;
                size--;
                return;
            } else if (key.compareTo(p.key) < 0) {
                if (p.left == null) {
                    p.left = item;
                    return;
                }
                p = p.left;
            } else {
                if (p.right == null) {
                    p.right = item;
                    return;
                }
                p = p.right;
            }
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    private class Node {
        Node left;
        Node right;
        K key;
        V value;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            left = null;
            right = null;
        }
    }
}
