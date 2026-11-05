package com.bookstore.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Trang thanh toán: /checkout */
public class CheckoutPage extends BasePage {

    private final By codOption = byText("Thanh toán khi nhận hàng (COD)");
    private final By payButton = By.xpath(
            "//button[starts-with(normalize-space(), 'Thanh toán (') and contains(., '₫')]");
    private final By discountInput = By.cssSelector("input[placeholder='Mã giảm giá']");
    private final By applyDiscountButton = By.xpath("//button[normalize-space()='Áp dụng']");
    private final By shippingValue = By.xpath(
            "//span[normalize-space()='Vận chuyển:']/following-sibling::span[1]");

    public boolean isCodOptionShown() {
        return isVisible(codOption);
    }

    public void waitReady() {
        wait.until(ExpectedConditions.elementToBeClickable(payButton));
    }

    /** Đọc số tiền trên nút "Thanh toán (xxx₫)" */
    public long displayedTotal() {
        String digits = text(payButton).replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }

    public String shippingText() {
        return text(shippingValue);
    }

    @Step("Áp mã giảm giá {code}")
    public void applyDiscount(String code) {
        type(discountInput, code);
        click(applyDiscountButton);
    }

    public boolean isDiscountLineShown(String code) {
        // React tách "Giảm giá (", mã, "):" thành nhiều text node -> so khớp trên toàn bộ text của phần tử
        return isVisible(By.xpath("//span[contains(normalize-space(.), " + xpathLiteral("Giảm giá (" + code + ")") + ")]"));
    }

    @Step("Bấm \"Thanh toán\" để đặt hàng")
    public void placeOrder() {
        click(payButton);
    }

    /** Chờ phí vận chuyển được tính xong (giao diện không có tín hiệu loading rõ ràng) */
    public void waitShippingCalculated() {
        pause(1500);
    }
}
