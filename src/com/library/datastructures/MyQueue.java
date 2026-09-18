package com.library.datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * A FIFO queue implemented from scratch as a singly linked list.
 * enqueue/dequeue/peek all run in O(1).
 *
 * Used in this project as the reservation waitlist for a book that has
 * no copies available: members reserve in the order they ask, and the
 * next copy returned goes to whoever has waited longest.
 */
public class MyQueue<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public T dequeue() {
        if (head == null) return null;
        T value = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return value;
    }

    public T peek() {
        return head == null ? null : head.value;
    }

    public boolean isEmpty() { return head == null; }
    public int size() { return size; }

    /** Non-destructive snapshot of the queue contents, head first. */
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        Node<T> node = head;
        while (node != null) {
            result.add(node.value);
            node = node.next;
        }
        return result;
    }
}
