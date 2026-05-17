package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

public class BookStorePage {

    private final Page page;

    public BookStorePage(Page page) {
        this.page = page;
    }

    @Step("Search for book: {query}")
    public void searchBook(String query) {
        page.fill("#searchBox", query);
    }

    @Step("Wait for book title visible: {title}")
    public void waitForBookVisible(String title) {
        page.locator("//tr/td")
                .filter(new Locator.FilterOptions().setHasText(title))
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(5000)
                        .setState(WaitForSelectorState.VISIBLE));
    }
}
