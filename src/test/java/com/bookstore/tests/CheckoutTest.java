package com.bookstore.tests;

import com.bookstore.base.BaseTest;
import com.bookstore.data.Customers;
import com.bookstore.data.SeedBook;
import com.bookstore.pages.CartPage;
import com.bookstore.pages.CheckoutPage;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.bookstore.data.DataFactory.vnd;

@Feature("Thanh toán")
public class CheckoutTest extends BaseTest {

    /** Khách mới có sẵn 1 cuốn trong giỏ, đã đăng nhập và đang ở trang checkout */
    private CheckoutPage checkoutWith(Customers.Session c, SeedBook book) {
        c.api.post("cart/add", Map.of("bookId", book.id(), "quantity", 1)).ensureSuccess("Thêm vào giỏ");
        loginViaApi(c.user.username(), c.user.password());
        CartPage cart = new CartPage().open();
        cart.selectItem(book.title());
        CheckoutPage checkout = cart.goToCheckout();
        checkout.waitReady();
        return checkout;
    }

    // known-bug: đang FAIL đúng vì BUG-007 + BUG-008 (docs/bugs trong repo bookstore-qa-automation)
    @Test(groups = {"known-bug"}, description = "TC-UI-CHK-01 Đặt hàng COD thành công từ giỏ hàng [BUG-007, BUG-008]")
    @Severity(SeverityLevel.BLOCKER)
    public void placeCodOrder() {
        Customers.Session c = Customers.fresh("se_chk");
        CheckoutPage checkout = checkoutWith(c, data.mainBook);

        Assert.assertTrue(checkout.isCodOptionShown(), "Không thấy lựa chọn COD");
        checkout.placeOrder();
        // Chỉ chờ rời trang checkout sang khu vực đơn hàng; kết quả đặt hàng kiểm tra chắc chắn qua API bên dưới.
        // Hiện test này FAIL đúng vì bug của hệ thống: BUG-007 (đơn COD bị rollback) và
        // BUG-008 (frontend vẫn báo thành công, chuyển tới /account/orders/undefined).
        checkout.waitUrlMatches(".*/account/orders.*");

        JsonNode orders = c.api.get("orders").result();
        Assert.assertEquals(orders.size(), 1, "Số đơn hàng của khách");
        Assert.assertEquals(orders.get(0).path("status").asText(), "PENDING");
    }

    @Test(groups = {"known-bug"}, description = "TC-UI-CHK-02 Số tiền hiển thị khi thanh toán phải bằng tổng tiền thực tế của đơn [BUG-007]")
    @Severity(SeverityLevel.CRITICAL)
    public void displayedTotalMatchesOrderTotal() {
        Customers.Session c = Customers.fresh("se_chk");
        CheckoutPage checkout = checkoutWith(c, data.secondBook);

        checkout.waitShippingCalculated();
        long shown = checkout.displayedTotal();
        String shipping = checkout.shippingText();
        checkout.placeOrder();
        checkout.waitUrlMatches(".*/account/orders.*");

        JsonNode orders = c.api.get("orders").result();
        Assert.assertEquals(orders.size(), 1, "Số đơn hàng của khách (xem BUG-007)");
        JsonNode order = orders.get(0);
        long actual = order.path("total").asLong();
        Assert.assertEquals(actual, shown, String.format(
                "Màn hình hiển thị %s₫ (vận chuyển: \"%s\") nhưng đơn hàng ghi %s₫ (ship %s₫)",
                vnd(shown), shipping, vnd(actual), vnd(order.path("shippingFee").asLong())));
    }

    @Test(groups = {"known-bug"}, description = "TC-UI-CHK-03 Áp mã giảm giá 10% trên trang thanh toán [BUG-011]")
    public void applyDiscountCode() {
        Customers.Session c = Customers.fresh("se_chk");
        SeedBook book = data.mainBook;
        CheckoutPage checkout = checkoutWith(c, book);
        long before = checkout.displayedTotal();

        checkout.applyDiscount(data.promoCode);

        Assert.assertTrue(checkout.isDiscountLineShown(data.promoCode), "Không thấy dòng giảm giá");
        long expected = before - book.price() * data.promoPercent / 100;
        Assert.assertEquals(checkout.displayedTotal(), expected, "Tổng tiền sau giảm giá");
    }
}
