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

    /** Returns the node with the given key. */
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
            Node left = insert(n.left, key, value);
            n.left = left;
            left.parent = n;
        } else {
            Node right = insert(n.right, key, value);
            n.right = right;
            right.parent = n;
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
        Node p = find(root, key);
        if (p == null) {
            return null;
        }
        V removedValue = p.value;

        if (p.left != null && p.right != null) {
            Node small = p.left;
            while (small.right != null) {
                small = small.right;
            }
            K smallKey = small.key;
            V smallValue = remove(smallKey);
            p.key = smallKey;
            p.value = smallValue;
        } else {
            Node a;
            if (p.left != null) {
                a = p.left;
            } else {
                a = p.right;
            }
            if (p.parent == null) { // Root node.
                root = a;
            } else if (p.parent.left == p) {
                p.parent.left = a;
            } else {
                p.parent.right = a;
            }
        }
        return removedValue;
    }

    @Override
    public V remove(K key, V value) {
        V removedValue = get(key);
        if (removedValue != null && removedValue == value) {
            remove(key);
        }
        return removedValue;
    }

    @Override
    public Iterator<K> iterator() {
        return new MapIterator();
    }

    private class MapIterator implements Iterator<K> {
        Node p;
        int iterSize;

        MapIterator() {
            iterSize = size;
            p = root;
            if (p != null) {
                while (p.left != null) {
                    p = p.left;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return iterSize > 0;
        }

        @Override
        public K next() {
            K iterKey = p.key;
            size--;
            if (p.right != null) {
                p = p.right;
                while (p.left != null) {
                    p = p.left;
                }
            } else {
                while (p.parent.key.compareTo(iterKey) < 0) {
                    p = p.parent;
                }
                p = p.parent;
            }
            return iterKey;
        }
    }

    private class Node {
        Node parent;
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
