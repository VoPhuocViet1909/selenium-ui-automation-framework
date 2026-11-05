package com.bookstore.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;
import java.util.Map;

/**
 * Mỗi luồng (thread) có một WebDriver riêng nhờ ThreadLocal,
 * nên TestNG chạy song song được mà các test không giẫm lên nhau.
 * Từ Selenium 4.6, Selenium Manager tự tải driver phù hợp với trình duyệt,
 * không cần WebDriverManager.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void init() {
        String browser = Config.get("browser").toLowerCase();
        WebDriver driver;
        switch (browser) {
            case "edge":
                EdgeOptions edge = new EdgeOptions();
                if (Config.headless()) edge.addArguments("--headless=new");
                edge.addArguments("--window-size=1366,900", "--lang=vi-VN");
                driver = new EdgeDriver(edge);
                break;
            case "chrome":
            default:
                ChromeOptions chrome = new ChromeOptions();
                if (Config.headless()) chrome.addArguments("--headless=new");
                chrome.addArguments("--window-size=1366,900", "--lang=vi-VN",
                        "--disable-dev-shm-usage", "--no-sandbox");
                // Cho phép đọc console log của trình duyệt (dùng trong test bảo mật)
                chrome.setCapability("goog:loggingPrefs", Map.of("browser", "ALL"));
                driver = new ChromeDriver(chrome);
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120)); // dev server biên dịch trang lần đầu khá chậm
        DRIVER.set(driver);
    }

    public static WebDriver get() {
        WebDriver driver = DRIVER.get();
        if (driver == null) throw new IllegalStateException("WebDriver chưa được khởi tạo cho thread này");
        return driver;
    }

    public static boolean isActive() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
