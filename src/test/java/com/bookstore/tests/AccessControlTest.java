package com.bookstore.tests;

import com.bookstore.base.BaseTest;
import com.bookstore.pages.AdminBooksPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

@Feature("Phân quyền giao diện")
public class AccessControlTest extends BaseTest {

    @Test(groups = {"regression", "security"}, description = "TC-UI-SEC-01 Khách hàng mở /admin không thấy trang quản trị")
    @Severity(SeverityLevel.CRITICAL)
    public void customerCannotOpenAdmin() {
        loginViaApi(data.customer.username(), data.customer.password());
        driver().get(com.bookstore.core.Config.baseUrl() + "/admin");

        boolean redirected;
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(10))
                    .until(ExpectedConditions.not(ExpectedConditions.urlMatches(".*/admin/?$")));
            redirected = true;
        } catch (TimeoutException e) {
            redirected = false;
        }
        Assert.assertTrue(redirected, "Khách hàng vẫn ở trang /admin: " + driver().getCurrentUrl());
    }

    @Test(groups = {"smoke", "regression"}, description = "TC-UI-ADM-01 Admin thấy sách vừa tạo trong trang quản lý sách")
    public void adminSeesCreatedBook() {
        loginViaApi(data.admin.username(), data.admin.password());
        AdminBooksPage page = new AdminBooksPage().open();
        page.search(data.mainBook.title());
        Assert.assertTrue(page.isBookListed(data.mainBook.title()), "Không thấy sách vừa tạo");
    }
}
