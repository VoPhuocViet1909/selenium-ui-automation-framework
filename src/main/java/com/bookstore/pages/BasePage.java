package com.bookstore.pages;

import com.bookstore.core.Config;
import com.bookstore.core.DriverFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Lớp cha của mọi Page Object.
 * Mọi thao tác đều đi qua explicit wait (không dùng Thread.sleep / implicit wait),
 * vì Selenium không tự chờ phần tử như Playwright.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    /** Nút X của popup "Vòng quay may mắn" (minigame tự mở mỗi lần tải trang) */
    private static final By MINIGAME_CLOSE = By.cssSelector("button[aria-label='Đóng mini game']");

    /** Nút giỏ hàng trên header (aria-label="Mở giỏ hàng") */
    protected final By cartButton = By.cssSelector("button[aria-label='Mở giỏ hàng']");

    protected BasePage() {
        this.driver = DriverFactory.get();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds()));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    @Step("Mở trang {path}")
    protected void openPath(String path) {
        driver.get(Config.baseUrl() + path);
        closeMinigameIfShown(Duration.ofSeconds(3));
    }

    /**
     * Popup minigame mở bằng useEffect sau khi trang hiển thị nên có thể xuất hiện trễ.
     * Chờ tối đa {@code maxWait}; nếu popup hiện thì bấm X như người dùng thật.
     */
    public void closeMinigameIfShown(Duration maxWait) {
        try {
            new WebDriverWait(driver, maxWait)
                    .until(ExpectedConditions.elementToBeClickable(MINIGAME_CLOSE))
                    .click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(MINIGAME_CLOSE));
        } catch (org.openqa.selenium.TimeoutException notShown) {
            // popup không xuất hiện -> bỏ qua
        }
    }

    // ---------- Thao tác cơ bản ----------

    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /** Phần tử đầu tiên đang hiển thị (trang có thể render cùng nút ở nhiều chỗ, vd: giỏ hàng + sidebar) */
    protected WebElement firstVisible(By by) {
        return wait.until(d -> d.findElements(by).stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    /**
     * Bấm phần tử. Nếu đang có lớp phủ loading (vòng quay) che lên thì Chrome báo
     * ElementClickInterceptedException -> chờ và thử lại cho đến khi bấm được hoặc hết thời gian.
     */
    protected void click(By by) {
        wait.until(d -> {
            WebElement el = firstVisible(by);
            if (!el.isEnabled()) return false;
            try {
                el.click();
                return true;
            } catch (ElementClickInterceptedException coveredByOverlay) {
                // Bị che: thường là popup minigame -> đóng nó rồi thử lại ở vòng sau
                driver.findElements(MINIGAME_CLOSE).stream()
                        .filter(WebElement::isDisplayed).findFirst()
                        .ifPresent(WebElement::click);
                return false;
            }
        });
    }

    protected void type(By by, String text) {
        WebElement el = visible(by);
        el.clear();
        if (text != null && !text.isEmpty()) el.sendKeys(text);
    }

    protected String text(By by) {
        return visible(by).getText().trim();
    }

    public boolean isVisible(By by) {
        try {
            visible(by);
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------- Locator tiện ích ----------

    /** Phần tử có đoạn text trực tiếp chứa chuỗi cho trước */
    public static By byText(String text) {
        return By.xpath("//*[text()[contains(., " + xpathLiteral(text) + ")]]");
    }

    /** Escape chuỗi để đặt vào XPath an toàn (kể cả khi có dấu nháy) */
    protected static String xpathLiteral(String s) {
        if (!s.contains("'")) return "'" + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        return "concat('" + s.replace("'", "',\"'\",'") + "')";
    }

    // ---------- Header / URL ----------

    public String cartBadgeText() {
        return text(cartButton);
    }

    public void waitCartBadge(String expected) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(cartButton, expected));
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitUrlMatches(String regex) {
        wait.until(ExpectedConditions.urlMatches(regex));
    }

    /** Nghỉ ngắn khi giao diện không có tín hiệu nào để chờ (vd: debounce gọi API). Hạn chế dùng. */
    protected static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
