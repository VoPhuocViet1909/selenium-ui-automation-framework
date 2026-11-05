package com.bookstore.tests;

import com.bookstore.base.BaseTest;
import com.bookstore.data.Customers;
import com.bookstore.data.SeedBook;
import com.bookstore.pages.CartPage;
import com.bookstore.pages.ProductPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.bookstore.data.DataFactory.vnd;

@Feature("Xem sách & giỏ hàng")
public class CartTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "TC-UI-SHOP-01 Trang chi tiết hiển thị đúng tên và giá sách")
    @Severity(SeverityLevel.CRITICAL)
    public void productDetailShowsTitleAndPrice() {
        SeedBook book = data.mainBook;
        ProductPage product = new ProductPage().open(book.id());

        Assert.assertEquals(product.title(), book.title());
        Assert.assertTrue(product.isPriceShown(vnd(book.price())), "Không thấy giá " + vnd(book.price()));
    }

    @Test(groups = {"regression"}, description = "TC-UI-SHOP-02 Khách chưa đăng nhập bấm \"Thêm vào giỏ\" -> chuyển tới trang đăng nhập")
    public void guestAddToCartRedirectsToLogin() {
        ProductPage product = new ProductPage().open(data.mainBook.id());
        product.addToCart();
        product.waitUrlMatches(".*/login\\?redirect=.*");
    }

    @Test(groups = {"smoke", "regression"}, description = "TC-UI-SHOP-03 Thêm sách vào giỏ và thấy trong trang giỏ hàng")
    @Severity(SeverityLevel.BLOCKER)
    public void addToCartShowsInCart() {
        Customers.Session c = Customers.fresh("se_ui");
        loginViaApi(c.user.username(), c.user.password());

        ProductPage product = new ProductPage().open(data.secondBook.id());
        product.addToCart();
        product.waitCartBadge("1");

        CartPage cart = new CartPage().open();
        Assert.assertTrue(cart.hasItem(data.secondBook.title()), "Sách không có trong giỏ");
    }

    @Test(groups = {"regression"}, description = "TC-UI-SHOP-04 Tăng/giảm số lượng trong giỏ cập nhật thành tiền")
    public void increaseDecreaseQuantity() {
        SeedBook book = data.mainBook;
        Customers.Session c = Customers.fresh("se_ui");
        c.api.post("cart/add", Map.of("bookId", book.id(), "quantity", 1)).ensureSuccess("Thêm vào giỏ");
        loginViaApi(c.user.username(), c.user.password());

        CartPage cart = new CartPage().open();
        cart.increase(book.title());
        cart.waitRowContains(book.title(), vnd(book.price()) + "₫ x 2");
        cart.decrease(book.title());
        cart.waitRowContains(book.title(), vnd(book.price()) + "₫ x 1");
    }

    /** Boundary Value Analysis: số lượng = tồn kho (biên trên), bấm + phải không vượt quá */
    @Test(groups = {"regression"}, description = "TC-UI-SHOP-05 Không thể tăng số lượng vượt tồn kho (BVA)")
    @Severity(SeverityLevel.CRITICAL)
    public void cannotExceedStock() {
        SeedBook book = data.lowStockBook;
        Customers.Session c = Customers.fresh("se_ui");
        c.api.post("cart/add", Map.of("bookId", book.id(), "quantity", book.stock())).ensureSuccess("Thêm vào giỏ");
        loginViaApi(c.user.username(), c.user.password());

        CartPage cart = new CartPage().open();
        cart.increase(book.title());
        cart.settle();

        int quantity = c.api.get("cart").result().path("items").path(0).path("quantity").asInt();
        Assert.assertEquals(quantity, book.stock(), "Số lượng trong giỏ vượt tồn kho");
    }

    @Test(groups = {"regression"}, description = "TC-UI-SHOP-06 Xoá sản phẩm khỏi giỏ")
    public void removeFromCart() {
        SeedBook book = data.mainBook;
        Customers.Session c = Customers.fresh("se_ui");
        c.api.post("cart/add", Map.of("bookId", book.id(), "quantity", 1)).ensureSuccess("Thêm vào giỏ");
        loginViaApi(c.user.username(), c.user.password());

        CartPage cart = new CartPage().open();
        cart.remove(book.title());
        cart.waitItemGone(book.title());
    }
}
