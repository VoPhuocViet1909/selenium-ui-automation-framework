package com.bookstore.data;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Sinh dữ liệu test duy nhất cho mỗi lần chạy để các test không phụ thuộc lẫn nhau. */
public final class DataFactory {

    private DataFactory() {
    }

    public static String uid() {
        return Long.toString(System.currentTimeMillis(), 36)
                + Integer.toString(ThreadLocalRandom.current().nextInt(46656), 36);
    }

    public static TestUser newUser(String prefix) {
        String id = uid();
        String username = (prefix + "_" + id);
        if (username.length() > 50) username = username.substring(0, 50);
        return new TestUser(username, prefix + "_" + id + "@test.local", "Test@123", "QA " + prefix + " " + id);
    }

    public static Map<String, Object> registerPayload(TestUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", u.username());
        m.put("email", u.email());
        m.put("password", u.password());
        m.put("fullName", u.fullName());
        m.put("phoneNumber", "0912345678");
        return m;
    }

    /** Địa chỉ hợp lệ (mã GHN giả vì môi trường test không kết nối GHN) */
    public static Map<String, Object> newAddress(String recipient) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("recipientName", recipient);
        m.put("phoneNumber", "0912345678");
        m.put("street", "12 Nguyễn Văn Bảo");
        m.put("ward", "Phường 4");
        m.put("district", "Gò Vấp");
        m.put("province", "Hồ Chí Minh");
        m.put("provinceId", 202);
        m.put("districtId", 1461);
        m.put("wardCode", "21310");
        return m;
    }

    public static Map<String, Object> newBook(String title, long price, int stock) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("isbn", "978" + String.format("%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L)));
        m.put("price", price);
        m.put("importPrice", price * 6 / 10);
        m.put("stockQuantity", stock);
        m.put("publishDate", "2024-01-15");
        m.put("description", "Sách tạo tự động bởi bộ test Selenium");
        m.put("status", "AVAILABLE");
        return m;
    }

    public static String isoDate(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays).toString();
    }

    /** 120000 -> "120.000" (định dạng tiền vi-VN như trên giao diện) */
    public static String vnd(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }
}
