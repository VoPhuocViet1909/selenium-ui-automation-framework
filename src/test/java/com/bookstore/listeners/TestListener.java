package com.bookstore.listeners;

import com.bookstore.core.DriverFactory;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

/** Khi test fail: chụp màn hình + ghi URL hiện tại đính kèm vào Allure Report. */
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        if (!DriverFactory.isActive()) return;
        try {
            byte[] png = ((TakesScreenshot) DriverFactory.get()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot - " + result.getName(), "image/png",
                    new ByteArrayInputStream(png), "png");
            Allure.addAttachment("URL", DriverFactory.get().getCurrentUrl());
        } catch (Exception e) {
            System.err.println("Không chụp được màn hình: " + e.getMessage());
        }
    }
}
