package com.library.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classic comparison sorts implemented explicitly (not java.util.Collections.sort)
 * so their mechanics and complexity are visible for the course project.
 */
public class SortAlgorithms {

    /**
     * Merge sort: stable, O(n log n) time in all cases, O(n) extra space.
     * Good choice when stability matters or worst-case guarantees matter.
     */
    public static <T> List<T> mergeSort(List<T> list, Comparator<T> comparator) {
        if (list.size() <= 1) return new ArrayList<>(list);
        int mid = list.size() / 2;
        List<T> left = mergeSort(list.subList(0, mid), comparator);
        List<T> right = mergeSort(list.subList(mid, list.size()), comparator);
        return merge(left, right, comparator);
    }

    private static <T> List<T> merge(List<T> left, List<T> right, Comparator<T> comparator) {
        List<T> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    /**
     * Quick sort: in-place, average O(n log n), worst case O(n^2)
     * (mitigated here with a randomized pivot).
     */
    public static <T> void quickSort(List<T> list, Comparator<T> comparator) {
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low >= high) return;
        int pivotIndex = low + (int) (Math.random() * (high - low + 1));
        swap(list, pivotIndex, high); // move random pivot to the end
        T pivot = list.get(high);

        int storeIndex = low;
        for (int i = low; i < high; i++) {
            if (comparator.compare(list.get(i), pivot) < 0) {
                swap(list, i, storeIndex);
                storeIndex++;
            }
        }
        swap(list, storeIndex, high);

        quickSort(list, low, storeIndex - 1, comparator);
        quickSort(list, storeIndex + 1, high, comparator);
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }
}
