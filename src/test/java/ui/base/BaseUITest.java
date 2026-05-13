package ui.base;

import com.microsoft.playwright.*;
import config.TestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(ScreenshotExtension.class)
public abstract class BaseUITest {

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(TestConfig.HEADLESS)
        );
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext();
        context.setDefaultTimeout(TestConfig.TIMEOUT_MS);
        page = context.newPage();
    }

    @AfterEach
    void closePage() {
        if (context != null) {
            context.close();
        }
    }

    void takeScreenshot(String name) {
        if (page != null && !page.isClosed()) {
            try {
                Path dir = Paths.get("test-screenshots");
                Files.createDirectories(dir);
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(dir.resolve(name + ".png"))
                        .setFullPage(true));
            } catch (IOException ignored) {
            }
        }
    }
}
