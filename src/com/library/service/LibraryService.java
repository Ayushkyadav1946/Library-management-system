package com.library.service;

import com.library.algorithms.SearchAlgorithms;
import com.library.algorithms.SortAlgorithms;
import com.library.datastructures.BookCatalogBST;
import com.library.datastructures.MinHeap;
import com.library.datastructures.MyHashMap;
import com.library.datastructures.MyQueue;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.util.FileStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Coordinates every data structure in the project against a single
 * consistent model of books, members and loans:
 *
 *  - MyHashMap<Integer, Book>     booksById        -> O(1) average lookup by id
 *  - MyHashMap<Integer, Member>   membersById       -> O(1) average lookup by id
 *  - MyHashMap<Integer, Loan>     loansById         -> O(1) average lookup by id
 *  - BookCatalogBST               catalog           -> sorted-by-title browsing & search
 *  - MinHeap<Loan>                dueDateHeap       -> O(log n) "what's due/overdue soonest"
 *  - MyHashMap<Integer, MyQueue<Integer>> waitlists -> FIFO reservations per book
 */
public class LibraryService {

    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double FINE_PER_DAY = 0.25;

    private final MyHashMap<Integer, Book> booksById = new MyHashMap<>();
    private final MyHashMap<Integer, Member> membersById = new MyHashMap<>();
    private final MyHashMap<Integer, Loan> loansById = new MyHashMap<>();
    private final BookCatalogBST catalog = new BookCatalogBST();
    private final MinHeap<Loan> dueDateHeap = new MinHeap<>(Comparator.comparingLong(Loan::getDueDay));
    private final MyHashMap<Integer, MyQueue<Integer>> waitlists = new MyHashMap<>();

    private final FileStorage storage;
    private int nextBookId;
    private int nextMemberId;
    private int nextLoanId;

    public LibraryService(FileStorage storage) {
        this.storage = storage;
        loadFromDisk();
    }

    // ---------------------------------------------------------- persistence

    private void loadFromDisk() {
        int[] counters = storage.loadCounters();
        nextBookId = counters[0];
        nextMemberId = counters[1];
        nextLoanId = counters[2];

        for (Book b : storage.loadBooks()) {
            booksById.put(b.getId(), b);
            catalog.insert(b);
        }
        for (Member m : storage.loadMembers()) {
            membersById.put(m.getId(), m);
        }
        for (Loan l : storage.loadLoans()) {
            loansById.put(l.getId(), l);
            if (l.isActive()) dueDateHeap.insert(l);
        }
        for (int[] r : storage.loadReservations()) {
            getOrCreateWaitlist(r[0]).enqueue(r[1]);
        }
    }

    public void saveAll() {
        storage.saveBooks(booksById.values());
        storage.saveMembers(membersById.values());
        storage.saveLoans(loansById.values());
        storage.saveCounters(nextBookId, nextMemberId, nextLoanId);
        storage.saveReservations(reservationsSnapshot());
    }

    private List<int[]> reservationsSnapshot() {
        List<int[]> result = new ArrayList<>();
        for (Integer bookId : waitlists.keys()) {
            MyQueue<Integer> q = waitlists.get(bookId);
            for (Integer memberId : q.toList()) {
                result.add(new int[]{bookId, memberId});
            }
        }
        return result;
    }

    private long today() { return LocalDate.now().toEpochDay(); }

    // ---------------------------------------------------------- books

    public Book addBook(String title, String author, int year, int copies) {
        Book book = new Book(nextBookId++, title, author, year, copies);
        booksById.put(book.getId(), book);
        catalog.insert(book);
        return book;
    }

    public Book getBook(int id) { return booksById.get(id); }

    public List<Book> allBooks() { return booksById.values(); }

    /** Sorted browse of the whole catalog via BST in-order traversal. O(n). */
    public List<Book> catalogSortedByTitle() { return catalog.inOrder(); }

    /** O(h) exact-title lookup using the BST directly. */
    public Book findByTitleBST(String title) { return catalog.search(title); }

    /**
     * Demonstrates binary search: sorts a snapshot of the catalog by title
     * (merge sort) then binary-searches it for an exact title match.
     */
    public Book findByTitleBinarySearch(String title) {
        List<Book> sorted = SortAlgorithms.mergeSort(allBooks(),
                Comparator.comparing(b -> b.getTitle().toLowerCase()));
        Book probe = new Book(-1, title, "", 0, 0);
        int idx = SearchAlgorithms.binarySearch(sorted, probe,
                Comparator.comparing(b -> b.getTitle().toLowerCase()));
        return idx < 0 ? null : sorted.get(idx);
    }

    public List<Book> sortedBooks(String criteria, String algorithm) {
        Comparator<Book> comparator;
        switch (criteria) {
            case "author": comparator = Comparator.comparing(b -> b.getAuthor().toLowerCase()); break;
            case "year": comparator = Comparator.comparingInt(Book::getYear); break;
            default: comparator = Comparator.comparing(b -> b.getTitle().toLowerCase());
        }
        List<Book> copy = new ArrayList<>(allBooks());
        if ("quick".equals(algorithm)) {
            SortAlgorithms.quickSort(copy, comparator);
            return copy;
        }
        return SortAlgorithms.mergeSort(copy, comparator);
    }

    public boolean removeBook(int bookId) {
        Book book = booksById.get(bookId);
        if (book == null) return false;
        if (book.getAvailableCopies() < book.getTotalCopies()) return false; // copies out on loan
        booksById.remove(bookId);
        catalog.delete(book.getTitle());
        waitlists.remove(bookId);
        return true;
    }

    // ---------------------------------------------------------- members

    public Member addMember(String name, String email) {
        Member member = new Member(nextMemberId++, name, email);
        membersById.put(member.getId(), member);
        return member;
    }

    public Member getMember(int id) { return membersById.get(id); }
    public List<Member> allMembers() { return membersById.values(); }

    public boolean removeMember(int memberId) {
        for (Loan l : loansById.values()) {
            if (l.getMemberId() == memberId && l.isActive()) return false; // still holds a book
        }
        return membersById.remove(memberId) != null;
    }

    // ---------------------------------------------------------- circulation

    public static class IssueResult {
        public final boolean issued;
        public final boolean waitlisted;
        public final Loan loan;
        IssueResult(boolean issued, boolean waitlisted, Loan loan) {
            this.issued = issued; this.waitlisted = waitlisted; this.loan = loan;
        }
    }

    public IssueResult issueBook(int bookId, int memberId) {
        Book book = booksById.get(bookId);
        Member member = membersById.get(memberId);
        if (book == null || member == null) return new IssueResult(false, false, null);

        if (!book.isAvailable()) {
            getOrCreateWaitlist(bookId).enqueue(memberId);
            return new IssueResult(false, true, null);
        }

        book.borrowCopy();
        long issueDay = today();
        Loan loan = new Loan(nextLoanId++, bookId, memberId, issueDay, issueDay + LOAN_PERIOD_DAYS);
        loansById.put(loan.getId(), loan);
        dueDateHeap.insert(loan);
        return new IssueResult(true, false, loan);
    }

    public static class ReturnResult {
        public final boolean returned;
        public final double fine;
        public final Integer autoIssuedToMemberId; // non-null if the waitlist immediately claimed the copy
        ReturnResult(boolean returned, double fine, Integer autoIssuedToMemberId) {
            this.returned = returned; this.fine = fine; this.autoIssuedToMemberId = autoIssuedToMemberId;
        }
    }

    public ReturnResult returnLoan(int loanId) {
        Loan loan = loansById.get(loanId);
        if (loan == null || !loan.isActive()) return new ReturnResult(false, 0, null);

        long today = today();
        loan.markReturned(today);
        dueDateHeap.remove(loan);

        Book book = booksById.get(loan.getBookId());
        double fine = 0;
        long late = loan.daysLate(today);
        if (late > 0) fine = late * FINE_PER_DAY;

        Integer autoIssuedTo = null;
        MyQueue<Integer> waiting = waitlists.get(loan.getBookId());
        if (waiting != null && !waiting.isEmpty()) {
            int nextMemberId = waiting.dequeue();
            book.returnCopy();
            IssueResult result = issueBook(loan.getBookId(), nextMemberId);
            if (result.issued) autoIssuedTo = nextMemberId;
        } else if (book != null) {
            book.returnCopy();
        }
        return new ReturnResult(true, fine, autoIssuedTo);
    }

    public List<Loan> allLoans() { return loansById.values(); }

    public List<Loan> activeLoansForMember(int memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loansById.values()) {
            if (l.getMemberId() == memberId && l.isActive()) result.add(l);
        }
        return result;
    }

    /** Peeks the single most-urgently-due active loan using the heap. O(1). */
    public Loan mostUrgentActiveLoan() {
        cleanHeapTop();
        return dueDateHeap.peekMin();
    }

    /** Removes any stale (already-returned) entries sitting at the top of the heap. */
    private void cleanHeapTop() {
        while (!dueDateHeap.isEmpty() && !dueDateHeap.peekMin().isActive()) {
            dueDateHeap.extractMin();
        }
    }

    /** All currently overdue active loans, sorted soonest-due-first via the heap. O(k log n). */
    public List<Loan> overdueLoans() {
        long today = today();
        List<Loan> all = new ArrayList<>(dueDateHeap.snapshot());
        List<Loan> overdue = new ArrayList<>();
        for (Loan l : all) {
            if (l.isActive() && l.daysLate(today) > 0) overdue.add(l);
        }
        return SortAlgorithms.mergeSort(overdue, Comparator.comparingLong(Loan::getDueDay));
    }

    public double outstandingFinesForMember(int memberId) {
        long today = today();
        double total = 0;
        for (Loan l : loansById.values()) {
            if (l.getMemberId() == memberId) {
                long late = l.daysLate(today);
                if (late > 0) total += late * FINE_PER_DAY;
            }
        }
        return total;
    }

    public int waitlistSize(int bookId) {
        MyQueue<Integer> q = waitlists.get(bookId);
        return q == null ? 0 : q.size();
    }

    private MyQueue<Integer> getOrCreateWaitlist(int bookId) {
        MyQueue<Integer> q = waitlists.get(bookId);
        if (q == null) {
            q = new MyQueue<>();
            waitlists.put(bookId, q);
        }
        return q;
    }
}
