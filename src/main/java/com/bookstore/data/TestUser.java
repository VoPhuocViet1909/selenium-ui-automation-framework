package com.bookstore.data;

public class TestUser {

    private final String username;
    private final String email;
    private final String password;
    private final String fullName;

    public TestUser(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public String username() { return username; }
    public String email() { return email; }
    public String password() { return password; }
    public String fullName() { return fullName; }

    @Override
    public String toString() {
        return username;
    }
}
