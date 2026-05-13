package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.stream.Collectors;

public class WebTablePage {

    private final Page page;

    public WebTablePage(Page page) {
        this.page = page;
    }

    public void clickAddButton() {
        Locator btn = page.locator("#addNewRecordButton");
        btn.scrollIntoViewIfNeeded();
        btn.click(new Locator.ClickOptions().setForce(true));
    }

    public void fillRegistrationForm(String firstName, String lastName, String email,
                                     String age, String salary, String department) {
        page.fill("#firstName", firstName);
        page.fill("#lastName", lastName);
        page.fill("#userEmail", email);
        page.fill("#age", age);
        page.fill("#salary", salary);
        page.fill("#department", department);
    }

    public void submitForm() {
        Locator submit = page.locator("#submit");
        submit.scrollIntoViewIfNeeded();
        submit.click(new Locator.ClickOptions().setForce(true));
    }

    public void addRecord(String firstName, String lastName, String email,
                          String age, String salary, String department) {
        clickAddButton();
        fillRegistrationForm(firstName, lastName, email, age, salary, department);
        submitForm();
        page.waitForTimeout(800);
    }

    public void editRecordByName(String firstName) {
        Locator row = page.locator(".rt-tr-group").filter(new Locator.FilterOptions().setHasText(firstName));
        row.scrollIntoViewIfNeeded();
        row.locator("[title='Edit']").click(new Locator.ClickOptions().setForce(true));
    }

    public void updateFirstName(String newFirstName) {
        page.locator("#firstName").clear();
        page.fill("#firstName", newFirstName);
        submitForm();
    }

    public void deleteRecordByName(String firstName) {
        Locator row = page.locator(".rt-tr-group").filter(new Locator.FilterOptions().setHasText(firstName));
        row.scrollIntoViewIfNeeded();
        row.locator("[title='Delete']").click(new Locator.ClickOptions().setForce(true));
    }

    public void search(String query) {
        page.fill("#searchBox", query);
        page.waitForTimeout(1000);
    }

    public boolean isRecordVisible(String firstName) {
        try {
            page.locator(".rt-tbody .rt-tr-group")
                    .filter(new Locator.FilterOptions().setHasText(firstName))
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getVisibleRowCount() {
        List<String> firstCells = page.locator(".rt-tbody .rt-tr-group .rt-td:first-child")
                .allTextContents();
        return (int) firstCells.stream()
                .filter(s -> !s.isBlank() && !"-".equals(s))
                .count();
    }

    public void clickColumnHeader(String columnName) {
        Locator header = page.locator(".rt-th").filter(new Locator.FilterOptions().setHasText(columnName)).first();
        header.scrollIntoViewIfNeeded();
        header.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
    }

    public List<String> getColumnValues(String columnIndex) {
        // columnIndex is 1-based (CSS nth-child)
        return page.locator(".rt-tbody .rt-tr:not(.-padRow) .rt-td:nth-child(" + columnIndex + ")")
                .allTextContents()
                .stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
