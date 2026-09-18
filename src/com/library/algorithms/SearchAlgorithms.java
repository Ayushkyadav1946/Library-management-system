package com.library.algorithms;

import java.util.Comparator;
import java.util.List;

/** Classic search algorithms implemented explicitly for the course project. */
public class SearchAlgorithms {

    /**
     * Binary search over a list that is already sorted according to comparator.
     * O(log n) time, O(1) extra space (iterative).
     * Returns the index of a match, or -1 if not found.
     */
    public static <T> int binarySearch(List<T> sortedList, T target, Comparator<T> comparator) {
        int low = 0, high = sortedList.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = comparator.compare(sortedList.get(mid), target);
            if (cmp == 0) return mid;
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    /**
     * Linear search: works on unsorted data, O(n) time.
     * Kept here to let the CLI demonstrate the contrast with binary search.
     */
    public static <T> int linearSearch(List<T> list, T target, Comparator<T> comparator) {
        for (int i = 0; i < list.size(); i++) {
            if (comparator.compare(list.get(i), target) == 0) return i;
        }
        return -1;
    }
}
