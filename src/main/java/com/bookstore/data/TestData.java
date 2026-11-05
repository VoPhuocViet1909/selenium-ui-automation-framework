package com.bookstore.data;

import com.bookstore.api.ApiClient;
import com.bookstore.api.ApiResponse;
import com.bookstore.core.Config;

import java.util.Map;

/**
 * Dữ liệu dùng chung cho cả bộ test, tạo một lần qua API trước khi chạy:
 * danh mục, tác giả, 3 cuốn sách, mã giảm giá 10% và một khách hàng.
 * Nhờ vậy test không phụ thuộc vào dữ liệu có sẵn trong database.
 */
public final class TestData {

    private static volatile TestData instance;

    public final String runId;
    public final TestUser admin;
    public final TestUser customer;
    public final SeedBook mainBook;
    public final SeedBook secondBook;
    public final SeedBook lowStockBook;
    public final String promoCode;
    public final int promoPercent = 10;

    private TestData(String runId, TestUser admin, TestUser customer, SeedBook mainBook,
                     SeedBook secondBook, SeedBook lowStockBook, String promoCode) {
        this.runId = runId;
        this.admin = admin;
        this.customer = customer;
        this.mainBook = mainBook;
        this.secondBook = secondBook;
        this.lowStockBook = lowStockBook;
        this.promoCode = promoCode;
    }

    public static TestData get() {
        if (instance == null) {
            synchronized (TestData.class) {
                if (instance == null) instance = seed();
            }
        }
        return instance;
    }

    private static TestData seed() {
        waitForBackend(120);
        String runId = DataFactory.uid();
        TestUser admin = new TestUser(Config.get("adminUser"), "admin@gmail.com", Config.get("adminPass"), "Admin");
        ApiClient api = ApiClient.login(admin.username(), admin.password());

        String categoryId = api.post("admin/categories",
                        Map.of("name", "Selenium Category " + runId, "description", "Tạo bởi bộ test Selenium"))
                .ensureSuccess("Tạo danh mục").result().path("id").asText();

        String authorId = api.post("authors",
                        Map.of("name", "Selenium Author " + runId, "biography", "Tạo bởi bộ test",
                                "nationality", "Việt Nam"))
                .ensureSuccess("Tạo tác giả").result().path("id").asText();

        SeedBook main = createBook(api, "[SE] Kiểm thử phần mềm " + runId, 120_000, 100, categoryId, authorId);
        SeedBook second = createBook(api, "[SE] Selenium cơ bản " + runId, 85_000, 100, categoryId, authorId);
        SeedBook lowStock = createBook(api, "[SE] Sách sắp hết " + runId, 50_000, 2, categoryId, authorId);

        String promoCode = ("SE" + runId.toUpperCase().replaceAll("[^A-Z0-9]", ""));
        if (promoCode.length() > 20) promoCode = promoCode.substring(0, 20);
        api.post("promotions", Map.of(
                "name", "Selenium giảm 10% " + runId,
                "code", promoCode,
                "discountPercent", 10,
                "startDate", DataFactory.isoDate(-1),
                "endDate", DataFactory.isoDate(30),
                "usageLimit", 1000,
                "description", "Mã giảm giá cho test Selenium")).ensureSuccess("Tạo mã khuyến mãi");

        TestUser customer = Customers.register("se_cus");

        warmUpFrontend(main.id());

        System.out.printf("%n[seed] runId=%s | sách: %s, %s, %s | mã KM: %s%n%n",
                runId, main.title(), second.title(), lowStock.title(), promoCode);
        return new TestData(runId, admin, customer, main, second, lowStock, promoCode);
    }

    private static SeedBook createBook(ApiClient api, String title, long price, int stock,
                                       String categoryId, String authorId) {
        Map<String, Object> payload = DataFactory.newBook(title, price, stock);
        payload.put("categoryIds", new String[]{categoryId});
        payload.put("authorIds", new String[]{authorId});
        ApiResponse res = api.post("admin/books", payload).ensureSuccess("Tạo sách " + title);
        return new SeedBook(res.result().path("id").asText(), res.result().path("title").asText(),
                res.result().path("price").asLong(), res.result().path("stockQuantity").asInt());
    }

    /**
     * Frontend chạy "npm run dev" chỉ biên dịch mỗi trang ở lần mở đầu tiên (10-30 giây).
     * Mở trước các trang test sẽ dùng để các test sau không bị chậm / timeout.
     */
    private static void warmUpFrontend(String bookId) {
        java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build();
        String[] paths = {"/login", "/cart", "/checkout", "/account", "/admin", "/admin/books", "/products/" + bookId};
        for (String path : paths) {
            try {
                http.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(Config.baseUrl() + path))
                                .timeout(java.time.Duration.ofSeconds(180)).GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.discarding());
                System.out.println("[warm-up] " + path);
            } catch (Exception e) {
                System.out.println("[warm-up] bỏ qua " + path + ": " + e.getMessage());
            }
        }
    }

    private static void waitForBackend(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        ApiClient anon = new ApiClient();
        while (System.currentTimeMillis() < deadline) {
            try {
                if (anon.get("books", Map.of("page", 0, "pageSize", 1)).status() == 200) return;
            } catch (IllegalStateException ignored) {
                // backend chưa lên
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("Backend không phản hồi tại " + Config.apiUrl()
                + " sau " + timeoutSeconds + "s. Đã chạy \"docker compose up -d\" chưa?");
    }
}
