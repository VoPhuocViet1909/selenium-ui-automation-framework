package com.bookstore.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Trang đăng nhập: /login */
public class LoginPage extends BasePage {

    private final By usernameInput = By.cssSelector("form input[placeholder='admin']");
    private final By passwordInput = By.cssSelector("form input[placeholder='••••••••']");
    private final By submitButton = By.xpath("//form//button[@type='submit']");
    private final By errorBox = By.cssSelector("form div.text-destructive");

    @Step("Mở trang đăng nhập")
    public LoginPage open() {
        openPath("/login");
        visible(usernameInput);
        return this;
    }

    @Step("Mở trang đăng nhập với redirect={redirect}")
    public LoginPage open(String redirect) {
        openPath("/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8));
        visible(usernameInput);
        return this;
    }

    @Step("Đăng nhập với username={username}")
    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        submit();
    }

    public void submit() {
        click(submitButton);
    }

    public String errorMessage() {
        return text(errorBox);
    }
}
