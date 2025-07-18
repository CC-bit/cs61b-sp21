package bstmap;


import java.util.Collection;
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

    /** Insert a node into the current tree. Returns the new tree. */
    private Node insert(Node n, K key, V value) {
        if (n == null) {
            return new Node(key, value);
        }
        if (key.compareTo(n.key) == 0) {
            n.value = value;
            size--; // Insert value into an exist node, size--.
        } else if (key.compareTo(n.key) < 0) {
            n.left = insert(n.left, key, value);
        } else {
            n.right = insert(n.right, key, value);
        }
        return n;
    }

    @Override
    public void put(K key, V value) {
        size++; // If insert value into an exist node, then size--.
        root = insert(root, key, value);
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
