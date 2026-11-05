package com.bookstore.data;

public class SeedBook {

    private final String id;
    private final String title;
    private final long price;
    private final int stock;

    public SeedBook(String id, String title, long price, int stock) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public String id() { return id; }
    public String title() { return title; }
    public long price() { return price; }
    public int stock() { return stock; }
}
