package com.bookstore.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Trang chi tiết sách: /products/{id} */
public class ProductPage extends BasePage {

    private final By title = By.cssSelector("h1");
    private final By addToCartButton = By.xpath(
            "//button[normalize-space()='Thêm vào giỏ' or normalize-space()='Hết hàng' or normalize-space()='Đang xử lý...']");

    @Step("Mở trang chi tiết sách {bookId}")
    public ProductPage open(String bookId) {
        openPath("/products/" + bookId);
        visible(title);
        return this;
    }

    public String title() {
        return text(title);
    }

    public boolean isPriceShown(String formattedPrice) {
        return isVisible(byText(formattedPrice));
    }

    @Step("Bấm \"Thêm vào giỏ\"")
    public void addToCart() {
        click(addToCartButton);
    }
}
