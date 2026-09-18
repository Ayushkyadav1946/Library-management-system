package com.library.datastructures;

import com.library.model.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * A binary search tree keyed on book title (case-insensitive), used to
 * keep the catalog browsable in sorted order and searchable in O(h) time,
 * where h is the tree height (O(log n) on average for random insert order).
 *
 * This is a plain (unbalanced) BST, deliberately kept simple to focus on
 * the core insert/search/delete/in-order-traversal mechanics that a DSA
 * course expects; a follow-up exercise would be to balance it (AVL/Red-Black).
 */
public class BookCatalogBST {

    private static class Node {
        Book book;
        Node left, right;
        Node(Book book) { this.book = book; }
    }

    private Node root;
    private int size;

    public void insert(Book book) {
        root = insert(root, book);
        size++;
    }

    private Node insert(Node node, Book book) {
        if (node == null) return new Node(book);
        int cmp = compareTitles(book.getTitle(), node.book.getTitle());
        if (cmp < 0) node.left = insert(node.left, book);
        else node.right = insert(node.right, book); // ties go right, keeps duplicates findable
        return node;
    }

    /** Exact title search. Returns null if not found. O(h) time. */
    public Book search(String title) {
        Node node = root;
        while (node != null) {
            int cmp = compareTitles(title, node.book.getTitle());
            if (cmp == 0) return node.book;
            node = (cmp < 0) ? node.left : node.right;
        }
        return null;
    }

    public void delete(String title) {
        root = delete(root, title);
    }

    private Node delete(Node node, String title) {
        if (node == null) return null;
        int cmp = compareTitles(title, node.book.getTitle());
        if (cmp < 0) {
            node.left = delete(node.left, title);
        } else if (cmp > 0) {
            node.right = delete(node.right, title);
        } else {
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // two children: replace with in-order successor (smallest in right subtree)
            Node successor = node.right;
            while (successor.left != null) successor = successor.left;
            node.book = successor.book;
            node.right = delete(node.right, successor.book.getTitle());
            size++; // undo the decrement above, the recursive call already decremented once
        }
        return node;
    }

    /** In-order traversal returns all books sorted alphabetically by title. O(n). */
    public List<Book> inOrder() {
        List<Book> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(Node node, List<Book> acc) {
        if (node == null) return;
        inOrder(node.left, acc);
        acc.add(node.book);
        inOrder(node.right, acc);
    }

    public int size() { return size; }

    private int compareTitles(String a, String b) {
        return a.compareToIgnoreCase(b);
    }
}
