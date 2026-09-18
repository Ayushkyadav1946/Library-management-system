package com.library.datastructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A binary min-heap implemented on top of an array (ArrayList), supporting
 * insert, peek, extractMin and arbitrary removal-by-value in O(log n) time.
 *
 * Used in this project to always know, in O(1), which active loan is due
 * back soonest (or is most overdue) without re-sorting the whole loan list.
 *
 * An internal position map lets us support remove(item) in O(log n) instead
 * of the usual O(n) linear scan a naive heap would need — this matters
 * because a returned loan must be removed from the middle of the heap,
 * not just from the top.
 */
public class MinHeap<T> {

    private final List<T> data = new ArrayList<>();
    private final Map<T, Integer> position = new HashMap<>();
    private final Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public int size() { return data.size(); }
    public boolean isEmpty() { return data.isEmpty(); }

    public void insert(T item) {
        data.add(item);
        int idx = data.size() - 1;
        position.put(item, idx);
        siftUp(idx);
    }

    public T peekMin() {
        return data.isEmpty() ? null : data.get(0);
    }

    public T extractMin() {
        if (data.isEmpty()) return null;
        T min = data.get(0);
        swap(0, data.size() - 1);
        data.remove(data.size() - 1);
        position.remove(min);
        if (!data.isEmpty()) siftDown(0);
        return min;
    }

    /** Removes a specific item from anywhere in the heap. O(log n). */
    public boolean remove(T item) {
        Integer idx = position.get(item);
        if (idx == null) return false;
        int last = data.size() - 1;
        swap(idx, last);
        data.remove(last);
        position.remove(item);
        if (idx <= data.size() - 1) {
            siftDown(idx);
            siftUp(idx);
        }
        return true;
    }

    /** Returns all items currently in the heap, without removing them (unsorted heap order). */
    public List<T> snapshot() {
        return new ArrayList<>(data);
    }

    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) / 2;
            if (comparator.compare(data.get(idx), data.get(parent)) < 0) {
                swap(idx, parent);
                idx = parent;
            } else break;
        }
    }

    private void siftDown(int idx) {
        int n = data.size();
        while (true) {
            int left = 2 * idx + 1, right = 2 * idx + 2, smallest = idx;
            if (left < n && comparator.compare(data.get(left), data.get(smallest)) < 0) smallest = left;
            if (right < n && comparator.compare(data.get(right), data.get(smallest)) < 0) smallest = right;
            if (smallest == idx) break;
            swap(idx, smallest);
            idx = smallest;
        }
    }

    private void swap(int i, int j) {
        T a = data.get(i), b = data.get(j);
        data.set(i, b);
        data.set(j, a);
        position.put(b, i);
        position.put(a, j);
    }
}
