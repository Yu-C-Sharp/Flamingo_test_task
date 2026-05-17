package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import utils.enums.EmployeeRegistrationTableLabels;

public class WebTablePage {

    private final Page page;

    public WebTablePage(Page page) {
        this.page = page;
    }

    @Step("Click Add button")
    private void clickAddButton() {
        page.locator("#addNewRecordButton").click();
        page.locator("#registration-form-modal").waitFor();
    }

    @Step("Fill registration form: {firstName} {lastName}")
    private void fillRegistrationForm(String firstName, String lastName, String email,
                                      int age, int salary, String department) {
        page.fill("#firstName", firstName);
        page.fill("#lastName", lastName);
        page.fill("#userEmail", email);
        page.fill("#age", String.valueOf(age));
        page.fill("#salary", String.valueOf(salary));
        page.fill("#department", department);
    }

    @Step("Submit form")
    private void submitForm() {
        page.locator("#submit").click();
    }

    @Step("Add record: {firstName} {lastName}")
    public void addRecord(String firstName, String lastName, String email,
                          int age, int salary, String department) {
        clickAddButton();
        fillRegistrationForm(firstName, lastName, email, age, salary, department);
        submitForm();
    }

    @Step("Open edit record modal by name: {firstName}")
    public void openEditRecordModalByName(String firstName) {
        page.locator("//tr[td[normalize-space()='" + firstName + "']]//span[@title='Edit']").click();
        page.locator("#registration-form-modal").waitFor();
        new EditModal(page);
    }

    @Step("Delete record by name: {firstName}")
    public void deleteRecordByName(String firstName) {
        page.locator("//tr[td[normalize-space()='" + firstName + "']]//span[@title='Delete']").click();
    }

    @Step("Search for: {query}")
    public void search(String query) {
        page.fill("#searchBox", query);
    }

    public static class EditModal {

        private static Page page = null;

        EditModal(Page page) {
            EditModal.page = page;
        }

        @Step("Update first name to: {newFirstName}")
        public static void updateFirstName(String newFirstName) {
            page.locator("#firstName").clear();
            page.fill("#firstName", newFirstName);
        }

        @Step("Update last name to: {newLastName}")
        public static void updateLastName(String newLastName) {
            page.locator("#lastName").clear();
            page.fill("#lastName", newLastName);
        }

        @Step("Update email to: {newEmail}")
        public static void updateEmail(String newEmail) {
            page.locator("#userEmail").clear();
            page.fill("#userEmail", newEmail);
        }

        @Step("Update age to: {newAge}")
        public static void updateAge(String newAge) {
            page.locator("#age").clear();
            page.fill("#age", newAge);
        }

        @Step("Update salary to: {newSalary}")
        public static void updateSalary(String newSalary) {
            page.locator("#salary").clear();
            page.fill("#salary", newSalary);
        }

        @Step("Update department to: {newDepartment}")
        public static void updateDepartment(String newDepartment) {
            page.locator("#department").clear();
            page.fill("#department", newDepartment);
            submitForm();
        }

        @Step("Submit form")
        private static void submitForm() {
            page.locator("#submit").click();
        }
    }

    @Step("Check if record is visible: {firstName}")
    public void waitForRecordVisible(String firstName, boolean isVisibleExpected) {
        Locator cell = page.locator("//tr/td")
                .filter(new Locator.FilterOptions().setHasText(firstName))
                .first();

        if (isVisibleExpected) {
            cell.waitFor(new Locator.WaitForOptions()
                    .setTimeout(5000)
                    .setState(WaitForSelectorState.VISIBLE));
        } else {
            cell.waitFor(new Locator.WaitForOptions()
                    .setTimeout(5000)
                    .setState(WaitForSelectorState.DETACHED));
        }
    }

    @Step("Get cell value for {column} in row with key: {rowKey}")
    public String getCellValue(String rowKey, EmployeeRegistrationTableLabels column) {
        return page.locator(
                "//tr[td[normalize-space()='" + rowKey + "']]/td[" + (column.getIndex() + 1) + "]"
        ).textContent().trim();
    }
}
