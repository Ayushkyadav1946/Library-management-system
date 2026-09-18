package com.library.model;

/** Represents a registered library member who can borrow books. */
public class Member {
    private final int id;
    private String name;
    private String email;

    public Member(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    public String toCsv() {
        return id + "|" + name.replace("|", "/") + "|" + email.replace("|", "/");
    }

    public static Member fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        return new Member(Integer.parseInt(p[0]), p[1], p[2]);
    }

    @Override
    public String toString() {
        return String.format("[#%d] %s <%s>", id, name, email);
    }
}
