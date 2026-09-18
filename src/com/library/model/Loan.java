package com.library.model;

/**
 * Represents one circulation record: a single copy of a book
 * issued to a member, with an issue date and a due date.
 * If returnDay is -1 the loan is still active (book not yet returned).
 */
public class Loan {
    private final int id;
    private final int bookId;
    private final int memberId;
    private final long issueDay;   // epoch day
    private final long dueDay;     // epoch day
    private long returnDay;        // -1 while active

    public Loan(int id, int bookId, int memberId, long issueDay, long dueDay) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDay = issueDay;
        this.dueDay = dueDay;
        this.returnDay = -1;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public long getIssueDay() { return issueDay; }
    public long getDueDay() { return dueDay; }
    public long getReturnDay() { return returnDay; }

    public boolean isActive() { return returnDay < 0; }
    public void markReturned(long today) { this.returnDay = today; }

    /** Days late relative to a given "today". 0 or negative means not late. */
    public long daysLate(long today) {
        long end = isActive() ? today : returnDay;
        return end - dueDay;
    }

    public String toCsv() {
        return id + "|" + bookId + "|" + memberId + "|" + issueDay + "|" + dueDay + "|" + returnDay;
    }

    public static Loan fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        Loan l = new Loan(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                Long.parseLong(p[3]), Long.parseLong(p[4]));
        long r = Long.parseLong(p[5]);
        if (r >= 0) l.markReturned(r);
        return l;
    }
}
