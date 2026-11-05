package com.bookstore.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Trang giỏ hàng: /cart */
public class CartPage extends BasePage {

    private final By heading = By.xpath("//h1[normalize-space()='Giỏ hàng của bạn']");
    /** Nút "Thanh toán (n)" của trang giỏ (khác nút "Thanh toán (xxx₫)" ở trang checkout) */
    private final By checkoutButton = By.xpath(
            "//button[starts-with(normalize-space(), 'Thanh toán (') and not(contains(., '₫'))]");

    @Step("Mở trang giỏ hàng")
    public CartPage open() {
        openPath("/cart");
        visible(heading);
        return this;
    }

    public boolean isHeadingShown() {
        return isVisible(heading);
    }

    private By itemLink(String bookTitle) {
        return By.xpath("//a[normalize-space()=" + xpathLiteral(bookTitle) + "]");
    }

    /** Khối chứa một dòng sản phẩm: div nhỏ nhất có cả link tên sách và nút xoá */
    private By row(String bookTitle) {
        return By.xpath("(//div[.//a[normalize-space()=" + xpathLiteral(bookTitle) + "]"
                + " and .//button[@aria-label='Xóa khỏi giỏ']])[last()]");
    }

    private By rowButton(String bookTitle, String ariaLabel) {
        return By.xpath("(//div[.//a[normalize-space()=" + xpathLiteral(bookTitle) + "]"
                + " and .//button[@aria-label='Xóa khỏi giỏ']])[last()]//button[@aria-label='" + ariaLabel + "']");
    }

    public boolean hasItem(String bookTitle) {
        return isVisible(itemLink(bookTitle));
    }

    public String rowText(String bookTitle) {
        return text(row(bookTitle));
    }

    /** Chờ đến khi dòng sản phẩm chứa đoạn text (vd: "120.000₫ x 2") */
    public void waitRowContains(String bookTitle, String expected) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(row(bookTitle), expected));
    }

    /**
     * Đảm bảo sản phẩm đang được chọn để thanh toán.
     * Giỏ hàng tự tick sẵn mọi sản phẩm khi tải xong, nên chỉ bấm khi ô đang trống
     * (bấm thêm lần nữa sẽ bỏ chọn -> nút "Thanh toán (0)" bị khoá).
     */
    @Step("Chọn sản phẩm \"{bookTitle}\" để thanh toán")
    public void selectItem(String bookTitle) {
        By checkbox = By.xpath("//*[@aria-label=" + xpathLiteral("Select " + bookTitle) + "]");
        if (!"true".equals(visible(checkbox).getAttribute("aria-checked"))) {
            click(checkbox);
        }
        wait.until(d -> "true".equals(d.findElement(checkbox).getAttribute("aria-checked")));
    }

    @Step("Tăng số lượng \"{bookTitle}\"")
    public void increase(String bookTitle) {
        click(rowButton(bookTitle, "Tăng số lượng"));
    }

    @Step("Giảm số lượng \"{bookTitle}\"")
    public void decrease(String bookTitle) {
        click(rowButton(bookTitle, "Giảm số lượng"));
    }

    @Step("Xoá \"{bookTitle}\" khỏi giỏ")
    public void remove(String bookTitle) {
        click(rowButton(bookTitle, "Xóa khỏi giỏ"));
    }

    public void waitItemGone(String bookTitle) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(itemLink(bookTitle)));
    }

    @Step("Bấm \"Thanh toán\" trong giỏ hàng")
    public CheckoutPage goToCheckout() {
        click(checkoutButton);
        waitUrlMatches(".*/checkout.*");
        return new CheckoutPage();
    }

    /** Dùng khi chờ một thay đổi phía server mà giao diện không báo hiệu */
    public void settle() {
        pause(1500);
    }
}
