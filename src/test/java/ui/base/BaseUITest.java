package ui.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import config.TestConfig;
import io.qameta.allure.Allure;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import ui.models.IFormData;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log
public abstract class BaseUITest {

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() throws IOException {
        Files.createDirectories(Paths.get("screenshots"));
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(TestConfig.HEADLESS)
                        .setArgs(List.of("--start-maximized"))
        );
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
        context.setDefaultTimeout(TestConfig.TIMEOUT_MS);
        page = context.newPage();
    }

    @AfterEach
    void closeSession(TestInfo testInfo) {
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots", testInfo.getDisplayName() + ".png"))
                .setFullPage(true));

        Allure.addAttachment("Screenshot - " + testInfo.getDisplayName(),
                "image/png", new ByteArrayInputStream(screenshot), "png");

        if (context != null) {
            context.close();
        }
    }

    protected <T extends IFormData> List<T> loadJsonData(String filePath, Class<T> targetClass) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(
                    new File(filePath),
                    mapper.getTypeFactory().constructCollectionType(List.class, targetClass)
            );
        } catch (IOException exception) {
            logger.warning(exception.getMessage());
            throw new RuntimeException("Can't deserialize data from JSON");
        }
    }

    protected void writeTestContentIntoFile(Path filePath) {
        try {
            Files.writeString(filePath, "test content");
        } catch (IOException exception) {
            logger.warning(exception.getMessage());
            throw new RuntimeException("Can't write a content into the test file");
        }
    }

    protected static void removeTestFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            logger.warning(exception.getMessage());
            throw new RuntimeException("Can't remove the test file");
        }
    }

    protected File createFile(String fileName, String fileExtension) {
        try {
            return Files.createTempFile(fileName, fileExtension).toFile();
        } catch (IOException exception) {
            logger.warning(exception.getMessage());
            throw new RuntimeException("File wasn't created successfully");
        }
    }
}
