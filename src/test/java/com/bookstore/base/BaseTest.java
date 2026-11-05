package com.bookstore.base;

import com.bookstore.api.ApiClient;
import com.bookstore.core.Config;
import com.bookstore.core.DriverFactory;
import com.bookstore.data.TestData;
import com.bookstore.listeners.TestListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.time.Instant;

@Listeners(TestListener.class)
public abstract class BaseTest {

    protected TestData data;

    /** Seed dữ liệu test qua API đúng 1 lần cho cả bộ test */
    @BeforeSuite(alwaysRun = true)
    public void seedTestData() {
        TestData.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        data = TestData.get();
        DriverFactory.init();
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        DriverFactory.quit();
    }

    protected WebDriver driver() {
        return DriverFactory.get();
    }

    /**
     * Đăng nhập "tắt": lấy token qua API rồi ghi vào localStorage giống cách frontend lưu
     * (authToken, refreshToken, user). Nhanh và ổn định hơn điền form ở mọi test;
     * chỉ test chức năng Đăng nhập mới đi qua giao diện form.
     */
    @Step("Đăng nhập qua API với {username}")
    protected void loginViaApi(String username, String password) {
        ApiClient api = ApiClient.login(username, password);
        JsonNode u = api.user();
        ObjectNode stored = ApiClient.JSON.createObjectNode()
                .put("id", u.path("id").asText())
                .put("email", u.path("email").asText())
                .put("fullName", u.path("fullName").asText(u.path("username").asText()))
                .put("role", u.path("role").asText().toUpperCase())
                .put("username", u.path("username").asText())
                .put("isEmailVerified", u.path("isEmailVerified").asBoolean(false))
                .put("createdAt", Instant.now().toString());

        // localStorage gắn với origin -> phải mở một trang của app trước khi ghi.
        // Dùng /login vì trang chủ đang lỗi khi sách không có discountPrice (xem BUG-006)
        driver().get(Config.baseUrl() + "/login");
        ((JavascriptExecutor) driver()).executeScript(
                "localStorage.setItem('authToken', arguments[0]);"
                        + "if (arguments[1]) localStorage.setItem('refreshToken', arguments[1]);"
                        + "localStorage.setItem('user', arguments[2]);",
                api.token(), api.refreshToken(), stored.toString());
    }
}
