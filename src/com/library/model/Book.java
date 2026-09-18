package com.library.model;

/**
 * Represents a single title in the library catalog.
 * A Book record can have multiple physical copies (totalCopies),
 * of which some number may currently be on loan.
 */
public class Book {
    private final int id;
    private String title;
    private String author;
    private int year;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, int year, int totalCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYear(int year) { this.year = year; }

    public void setTotalCopies(int totalCopies) {
        int diff = totalCopies - this.totalCopies;
        this.totalCopies = totalCopies;
        this.availableCopies = Math.max(0, this.availableCopies + diff);
    }

    public boolean isAvailable() { return availableCopies > 0; }

    public void borrowCopy() {
        if (availableCopies <= 0) {
            throw new IllegalStateException("No copies of \"" + title + "\" are available.");
        }
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    /** CSV serialization used by FileStorage. */
    public String toCsv() {
        return id + "|" + escape(title) + "|" + escape(author) + "|" + year + "|" + totalCopies + "|" + availableCopies;
    }

    public static Book fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        Book b = new Book(Integer.parseInt(p[0]), unescape(p[1]), unescape(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]));
        // directly restore available copies (bypass constructor default of "all available")
        int available = Integer.parseInt(p[5]);
        while (b.availableCopies > available) b.availableCopies--;
        return b;
    }

    private static String escape(String s) { return s.replace("|", "/"); }
    private static String unescape(String s) { return s; }

    @Override
    public String toString() {
        return String.format("[#%d] \"%s\" by %s (%d) - %d/%d available",
                id, title, author, year, availableCopies, totalCopies);
    }
}
