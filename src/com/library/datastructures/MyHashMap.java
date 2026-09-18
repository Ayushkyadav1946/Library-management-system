package com.library.datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * A hash table implemented from scratch using separate chaining for
 * collision resolution, and dynamic resizing to keep the load factor bounded.
 *
 * Average time complexity: O(1) for put/get/remove.
 * Worst case (all keys collide): O(n).
 *
 * Used in this project to look up Book and Member records by their
 * integer id in constant average time, instead of scanning a list.
 */
public class MyHashMap<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private Node<K, V>[] buckets;
    private int size;
    private static final double LOAD_FACTOR_LIMIT = 0.75;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        buckets = new Node[16];
        size = 0;
    }

    private int bucketIndex(K key, int capacity) {
        int h = (key == null) ? 0 : key.hashCode();
        h ^= (h >>> 16); // spread bits to reduce clustering
        return (h & 0x7fffffff) % capacity;
    }

    public void put(K key, V value) {
        if ((double) (size + 1) / buckets.length > LOAD_FACTOR_LIMIT) {
            resize();
        }
        int idx = bucketIndex(key, buckets.length);
        Node<K, V> node = buckets[idx];
        while (node != null) {
            if (keysEqual(node.key, key)) {
                node.value = value; // update existing
                return;
            }
            node = node.next;
        }
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[idx];
        buckets[idx] = newNode;
        size++;
    }

    public V get(K key) {
        int idx = bucketIndex(key, buckets.length);
        Node<K, V> node = buckets[idx];
        while (node != null) {
            if (keysEqual(node.key, key)) return node.value;
            node = node.next;
        }
        return null;
    }

    public boolean containsKey(K key) { return get(key) != null; }

    public V remove(K key) {
        int idx = bucketIndex(key, buckets.length);
        Node<K, V> node = buckets[idx];
        Node<K, V> prev = null;
        while (node != null) {
            if (keysEqual(node.key, key)) {
                if (prev == null) buckets[idx] = node.next;
                else prev.next = node.next;
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

    public List<K> keys() {
        List<K> result = new ArrayList<>();
        for (Node<K, V> head : buckets) {
            Node<K, V> node = head;
            while (node != null) {
                result.add(node.key);
                node = node.next;
            }
        }
        return result;
    }

    public List<V> values() {
        List<V> result = new ArrayList<>();
        for (Node<K, V> head : buckets) {
            Node<K, V> node = head;
            while (node != null) {
                result.add(node.value);
                node = node.next;
            }
        }
        return result;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] old = buckets;
        buckets = new Node[old.length * 2];
        size = 0;
        for (Node<K, V> head : old) {
            Node<K, V> node = head;
            while (node != null) {
                put(node.key, node.value);
                node = node.next;
            }
        }
    }

    private boolean keysEqual(K a, K b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
