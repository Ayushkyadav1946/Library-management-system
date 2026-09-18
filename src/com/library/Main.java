package com.library;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LibraryService;
import com.library.util.FileStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Command-line interface for the Library Management System.
 * Run with: java -cp bin com.library.Main
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static LibraryService service;

    public static void main(String[] args) {
        FileStorage storage = new FileStorage("data");
        service = new LibraryService(storage);

        System.out.println("=========================================");
        System.out.println(" Library Management System (CLI)");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": addBook(); break;
                    case "2": listBooksSorted(); break;
                    case "3": searchBook(); break;
                    case "4": addMember(); break;
                    case "5": listMembers(); break;
                    case "6": issueBook(); break;
                    case "7": returnBook(); break;
                    case "8": showOverdue(); break;
                    case "9": removeBook(); break;
                    case "0":
                        service.saveAll();
                        System.out.println("Saved. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Not a valid option, try again.");
                }
            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
            if (running) service.saveAll(); // persist after every successful action
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("---- MENU ----");
        System.out.println("1. Add a book");
        System.out.println("2. List books (sorted)");
        System.out.println("3. Search for a book by title");
        System.out.println("4. Add a member");
        System.out.println("5. List members");
        System.out.println("6. Issue a book to a member");
        System.out.println("7. Return a book");
        System.out.println("8. View overdue loans");
        System.out.println("9. Remove a book");
        System.out.println("0. Save and exit");
        System.out.print("Choose an option: ");
    }

    private static void addBook() {
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        int year = readInt("Year published: ");
        int copies = readInt("Number of copies: ");
        Book book = service.addBook(title, author, year, copies);
        System.out.println("Added: " + book);
    }

    private static void listBooksSorted() {
        System.out.print("Sort by (title/author/year) [title]: ");
        String criteria = sc.nextLine().trim().toLowerCase();
        if (criteria.isEmpty()) criteria = "title";
        System.out.print("Algorithm (merge/quick) [merge]: ");
        String algo = sc.nextLine().trim().toLowerCase();
        if (algo.isEmpty()) algo = "merge";

        List<Book> books = service.sortedBooks(criteria, algo);
        if (books.isEmpty()) {
            System.out.println("No books in the catalog yet.");
            return;
        }
        System.out.println("(" + books.size() + " books, sorted by " + criteria + " using " + algo + " sort)");
        for (Book b : books) System.out.println("  " + b);
    }

    private static void searchBook() {
        System.out.print("Title to search for: ");
        String title = sc.nextLine().trim();
        Book found = service.findByTitleBST(title);
        if (found == null) {
            // fall back to the binary-search demonstration in case of case/whitespace mismatch
            found = service.findByTitleBinarySearch(title);
        }
        System.out.println(found == null ? "No book found with that title." : "Found: " + found);
    }

    private static void addMember() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        Member m = service.addMember(name, email);
        System.out.println("Added: " + m);
    }

    private static void listMembers() {
        List<Member> members = service.allMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }
        for (Member m : members) {
            double fine = service.outstandingFinesForMember(m.getId());
            System.out.printf("  %s%s%n", m, fine > 0 ? String.format(" - $%.2f owed", fine) : "");
        }
    }

    private static void issueBook() {
        int bookId = readInt("Book ID: ");
        int memberId = readInt("Member ID: ");
        LibraryService.IssueResult result = service.issueBook(bookId, memberId);
        if (result.issued) {
            Loan l = result.loan;
            System.out.println("Issued. Loan #" + l.getId() + " due " + formatDay(l.getDueDay()) + ".");
        } else if (result.waitlisted) {
            System.out.println("No copies available - added to the waitlist for this book instead.");
        } else {
            System.out.println("Could not issue: check the book ID and member ID.");
        }
    }

    private static void returnBook() {
        int loanId = readInt("Loan ID: ");
        LibraryService.ReturnResult result = service.returnLoan(loanId);
        if (!result.returned) {
            System.out.println("Could not find an active loan with that ID.");
            return;
        }
        if (result.fine > 0) {
            System.out.printf("Returned. Fine due: $%.2f%n", result.fine);
        } else {
            System.out.println("Returned on time, no fine.");
        }
        if (result.autoIssuedToMemberId != null) {
            System.out.println("This copy was immediately issued to waitlisted member #" + result.autoIssuedToMemberId + ".");
        }
    }

    private static void showOverdue() {
        List<Loan> overdue = service.overdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("Nothing is overdue.");
            return;
        }
        long today = LocalDate.now().toEpochDay();
        for (Loan l : overdue) {
            long late = l.daysLate(today);
            double fine = late * LibraryService.FINE_PER_DAY;
            Book b = service.getBook(l.getBookId());
            Member m = service.getMember(l.getMemberId());
            System.out.printf("  Loan #%d: \"%s\" -> %s, %d day(s) late, $%.2f owed%n",
                    l.getId(), b != null ? b.getTitle() : "?", m != null ? m.getName() : "?", late, fine);
        }
    }

    private static void removeBook() {
        int bookId = readInt("Book ID to remove: ");
        boolean ok = service.removeBook(bookId);
        System.out.println(ok ? "Removed." : "Could not remove: book not found, or copies are still on loan.");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static String formatDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay).format(DATE_FMT);
    }
}
