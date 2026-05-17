package integration;

import api.models.book.Book;
import api.models.book.BooksResponse;
import api.utils.enums.SuccessStatusCodes;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BookStorePage;

import static api.helpers.ApiHelper.getBookStoreBooks;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Feature("API + UI Integration — Book Store")
class ApiUiIntegrationTest {

    private static Playwright playwright;
    private static Browser browser;
    private static Book selectedBook;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setUp() {
        Response response = getBookStoreBooks();
        assertThat(response.statusCode())
                .as("GET /BookStore/v1/Books should return 200")
                .isEqualTo(SuccessStatusCodes.OK.getStatusCode());

        BooksResponse booksResponse = response.as(BooksResponse.class);
        assertThat(booksResponse.getBooks())
                .as("Book store must contain at least one book")
                .isNotEmpty();

        selectedBook = booksResponse.getBooks().get(0);

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(TestConfig.HEADLESS)
        );
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openBrowser() {
        context = browser.newContext();
        context.setDefaultTimeout(TestConfig.TIMEOUT_MS);
        page = context.newPage();
    }

    @AfterEach
    void closeBrowserContext() {
        context.close();
    }

    @Test
    @Description("Book retrieved via API should appear in the Book Store UI")
    void shouldVerifyApiBookAppearsInUI() {
        assertThat(selectedBook.getIsbn()).isNotBlank();
        assertThat(selectedBook.getTitle()).isNotBlank();
        assertThat(selectedBook.getAuthor()).isNotBlank();

        page.navigate(TestConfig.DEMOQA_BOOKS_URL);

        BookStorePage bookStorePage = new BookStorePage(page);
        bookStorePage.searchBook(selectedBook.getTitle());
        bookStorePage.waitForBookVisible(selectedBook.getTitle());
    }
}
