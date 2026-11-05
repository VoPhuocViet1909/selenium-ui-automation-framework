package com.bookstore.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Trang quản lý sách của admin: /admin/books
 * Trang không có ô tìm kiếm, chỉ phân trang 10 sách/trang và backend không sắp xếp
 * -> sách vừa tạo có thể nằm ở bất kỳ trang nào, phải lật từng trang để tìm.
 */
public class AdminBooksPage extends BasePage {

    private final By bookColumnHeader = By.xpath("//th[normalize-space()='Sách']");
    private final By searchInput = By.xpath("//input[contains(@placeholder, 'Tìm')]");
    private final By pageInfo = By.xpath("//div[starts-with(normalize-space(), 'Trang ')]");
    private final By nextButton = By.xpath("//button[normalize-space()='Sau']");

    private static final int MAX_PAGES = 50;

    @Step("Mở trang quản lý sách")
    public AdminBooksPage open() {
        openPath("/admin/books");
        visible(bookColumnHeader);
        // Lúc đầu trang hiện "Trang 1 / 0" với bảng rỗng, chờ API trả dữ liệu thật
        wait.until(d -> !currentPageInfo().endsWith("/ 0"));
        return this;
    }

    @Step("Tìm sách \"{title}\"")
    public void search(String title) {
        if (!driver.findElements(searchInput).isEmpty()) {
            type(searchInput, title);
        }
    }

    @Step("Lật từng trang để tìm sách \"{title}\"")
    public boolean isBookListed(String title) {
        By book = byText(title);
        for (int i = 0; i < MAX_PAGES; i++) {
            if (driver.findElements(book).stream().anyMatch(WebElement::isDisplayed)) return true;

            WebElement next = driver.findElement(nextButton);
            if (!next.isEnabled()) return false; // đã tới trang cuối

            String before = currentPageInfo();
            click(nextButton);
            wait.until(d -> !currentPageInfo().equals(before)); // chờ trang mới tải xong
            visible(bookColumnHeader);
        }
        return false;
    }

    private String currentPageInfo() {
        return visible(pageInfo).getText().trim();
    }
}
