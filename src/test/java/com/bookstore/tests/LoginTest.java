package com.bookstore.tests;

import com.bookstore.base.BaseTest;
import com.bookstore.data.TestData;
import com.bookstore.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

@Feature("Đăng nhập")
public class LoginTest extends BaseTest {

    private static final String MSG_REQUIRED = "Vui lòng nhập tên đăng nhập và mật khẩu";
    private static final String MSG_INVALID = "Sai tên đăng nhập hoặc mật khẩu. Vui lòng kiểm tra lại.";

    @Test(groups = {"smoke", "regression"}, description = "TC-UI-LOGIN-01 Khách hàng đăng nhập thành công -> chuyển tới /account")
    @Severity(SeverityLevel.BLOCKER)
    public void customerLoginSuccess() {
        LoginPage login = new LoginPage().open();
        login.login(data.customer.username(), data.customer.password());

        login.waitUrlMatches(".*/account.*");
        Object token = ((JavascriptExecutor) driver()).executeScript("return localStorage.getItem('authToken');");
        Assert.assertNotNull(token, "Không lưu authToken sau khi đăng nhập");
    }

    @Test(groups = {"smoke", "regression"}, description = "TC-UI-LOGIN-02 Admin đăng nhập thành công -> chuyển tới /admin")
    @Severity(SeverityLevel.BLOCKER)
    public void adminLoginSuccess() {
        LoginPage login = new LoginPage().open();
        login.login(data.admin.username(), data.admin.password());
        login.waitUrlMatches(".*/admin.*");
    }

    /**
     * Data-driven: mỗi dòng là một test case.
     * Kỹ thuật: Equivalence Partitioning (trống / sai / không tồn tại).
     */
    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        // DataProvider chạy trước @BeforeMethod nên lấy dữ liệu trực tiếp
        TestData seed = TestData.get();
        String user = seed.customer.username();
        return new Object[][]{
                {"TC-UI-LOGIN-03 Bỏ trống cả hai ô", "", "", MSG_REQUIRED},
                {"TC-UI-LOGIN-04 Bỏ trống mật khẩu", user, "", MSG_REQUIRED},
                {"TC-UI-LOGIN-05 Bỏ trống tên đăng nhập", "", "Test@123", MSG_REQUIRED},
        };
    }

    /** Sai thông tin đăng nhập: đang FAIL đúng vì BUG-010 (hiện "Đăng nhập thất bại" thay vì thông báo đúng) */
    @DataProvider(name = "wrongCredentials")
    public Object[][] wrongCredentials() {
        TestData seed = TestData.get();
        return new Object[][]{
                {"TC-UI-LOGIN-06 Sai mật khẩu", seed.customer.username(), "SaiMatKhau!", MSG_INVALID},
                // Cùng thông báo với sai mật khẩu -> không lộ username nào tồn tại (user enumeration)
                {"TC-UI-LOGIN-07 Tài khoản không tồn tại", "khong_ton_tai_" + seed.runId, "Test@123", MSG_INVALID},
        };
    }

    @Test(dataProvider = "invalidLogins", groups = {"regression"},
            description = "TC-UI-LOGIN-03..07 Đăng nhập không hợp lệ hiển thị đúng thông báo")
    @Description("Đăng nhập với dữ liệu không hợp lệ: phải ở lại /login và hiện đúng thông báo lỗi")
    public void invalidLoginShowsError(String caseName, String username, String password, String expectedMessage) {
        LoginPage login = new LoginPage().open();
        login.login(username, password);

        Assert.assertEquals(login.errorMessage(), expectedMessage, caseName);
        Assert.assertTrue(login.currentUrl().contains("/login"), caseName + ": không được rời trang đăng nhập");
    }

    @Test(dataProvider = "wrongCredentials", groups = {"known-bug"},
            description = "TC-UI-LOGIN-06..07 Sai mật khẩu / tài khoản không tồn tại hiển thị đúng thông báo [BUG-010]")
    public void wrongCredentialsShowError(String caseName, String username, String password, String expectedMessage) {
        invalidLoginShowsError(caseName, username, password, expectedMessage);
    }

    @Test(groups = {"known-bug", "security"}, description = "TC-UI-LOGIN-08 Không ghi mật khẩu ra console trình duyệt [BUG-009]")
    @Severity(SeverityLevel.CRITICAL)
    public void passwordNotLoggedToConsole() {
        LoginPage login = new LoginPage().open();
        String secret = data.customer.password();
        login.login(data.customer.username(), secret);
        login.waitUrlMatches(".*/account.*");

        List<String> leaked = driver().manage().logs().get(LogType.BROWSER).getAll().stream()
                .map(LogEntry::getMessage)
                .filter(m -> m.contains(secret))
                .collect(Collectors.toList());
        Assert.assertTrue(leaked.isEmpty(), "Mật khẩu xuất hiện trong console:\n" + String.join("\n", leaked));
    }

    @Test(groups = {"regression"}, description = "TC-UI-LOGIN-09 Đăng nhập xong quay lại đúng trang được yêu cầu (redirect)")
    public void redirectAfterLogin() {
        LoginPage login = new LoginPage().open("/cart");
        login.login(data.customer.username(), data.customer.password());
        login.waitUrlMatches(".*/cart$");
    }
}
