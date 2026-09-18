package com.library.util;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves the library's state as plain pipe-delimited text files,
 * so data survives between runs without needing a database.
 */
public class FileStorage {

    private final Path dataDir;

    public FileStorage(String dataDirPath) {
        this.dataDir = Paths.get(dataDirPath);
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory: " + dataDirPath, e);
        }
    }

    private Path booksFile() { return dataDir.resolve("books.txt"); }
    private Path membersFile() { return dataDir.resolve("members.txt"); }
    private Path loansFile() { return dataDir.resolve("loans.txt"); }
    private Path countersFile() { return dataDir.resolve("counters.txt"); }
    private Path reservationsFile() { return dataDir.resolve("reservations.txt"); }

    public List<Book> loadBooks() {
        return loadLines(booksFile(), Book::fromCsv);
    }

    public List<Member> loadMembers() {
        return loadLines(membersFile(), Member::fromCsv);
    }

    public List<Loan> loadLoans() {
        return loadLines(loansFile(), Loan::fromCsv);
    }

    public void saveBooks(List<Book> books) {
        saveLines(booksFile(), books, Book::toCsv);
    }

    public void saveMembers(List<Member> members) {
        saveLines(membersFile(), members, Member::toCsv);
    }

    public void saveLoans(List<Loan> loans) {
        saveLines(loansFile(), loans, Loan::toCsv);
    }

    public int[] loadCounters() {
        // [nextBookId, nextMemberId, nextLoanId]
        int[] counters = {1, 1, 1};
        Path f = countersFile();
        if (!Files.exists(f)) return counters;
        try {
            List<String> lines = Files.readAllLines(f);
            if (!lines.isEmpty()) {
                String[] p = lines.get(0).split(",");
                counters[0] = Integer.parseInt(p[0]);
                counters[1] = Integer.parseInt(p[1]);
                counters[2] = Integer.parseInt(p[2]);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read counters file, starting fresh. " + e.getMessage());
        }
        return counters;
    }

    public void saveCounters(int nextBookId, int nextMemberId, int nextLoanId) {
        try {
            Files.write(countersFile(), (nextBookId + "," + nextMemberId + "," + nextLoanId).getBytes());
        } catch (IOException e) {
            System.err.println("Warning: could not save counters file. " + e.getMessage());
        }
    }

    /** Each reservation is stored as "bookId,memberId", one per line, in FIFO order. */
    public List<int[]> loadReservations() {
        List<int[]> result = new ArrayList<>();
        Path f = reservationsFile();
        if (!Files.exists(f)) return result;
        try {
            for (String line : Files.readAllLines(f)) {
                if (line.isBlank()) continue;
                String[] p = line.split(",");
                result.add(new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1])});
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read reservations file: " + e.getMessage());
        }
        return result;
    }

    public void saveReservations(List<int[]> reservations) {
        try (BufferedWriter bw = Files.newBufferedWriter(reservationsFile())) {
            for (int[] r : reservations) {
                bw.write(r[0] + "," + r[1]);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: could not save reservations file: " + e.getMessage());
        }
    }

    private interface CsvParser<T> { T parse(String line); }
    private interface CsvWriter<T> { String write(T item); }

    private <T> List<T> loadLines(Path path, CsvParser<T> parser) {
        List<T> result = new ArrayList<>();
        if (!Files.exists(path)) return result;
        try {
            for (String line : Files.readAllLines(path)) {
                if (!line.isBlank()) result.add(parser.parse(line));
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + path.getFileName() + ": " + e.getMessage());
        }
        return result;
    }

    private <T> void saveLines(Path path, List<T> items, CsvWriter<T> writer) {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            for (T item : items) {
                bw.write(writer.write(item));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: could not save " + path.getFileName() + ": " + e.getMessage());
        }
    }
}
