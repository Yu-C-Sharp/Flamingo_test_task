package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import io.qameta.allure.Step;

import java.nio.file.Paths;

public class FormPage {

    private final Page page;

    public FormPage(Page page) {
        this.page = page;
    }

    @Step("Fill first name: {value}")
    public void fillFirstName(String value) {
        page.fill("#firstName", value);
    }

    @Step("Fill last name: {value}")
    public void fillLastName(String value) {
        page.fill("#lastName", value);
    }

    @Step("Fill email: {value}")
    public void fillEmail(String value) {
        page.fill("#userEmail", value);
    }

    @Step("Select gender: {gender}")
    public void selectGender(String gender) {
        Locator locator = switch (gender.toLowerCase()) {
            case "female" -> page.locator("label[for='gender-radio-2']");
            case "other" -> page.locator("label[for='gender-radio-3']");
            default -> page.locator("label[for='gender-radio-1']");
        };
        locator.scrollIntoViewIfNeeded();
        locator.click(new Locator.ClickOptions().setForce(true));
    }

    @Step("Fill mobile: {value}")
    public void fillMobile(String value) {
        page.fill("#userNumber", value);
    }

    @Step("Select date of birth: {day} {month} {year}")
    public void selectDateOfBirth(String month, String year, int day) {
        page.click("#dateOfBirthInput");
        page.selectOption(".react-datepicker__month-select", new SelectOption().setLabel(month));
        page.selectOption(".react-datepicker__year-select", new SelectOption().setLabel(year));
        page.locator(".react-datepicker__day--0" + String.format("%02d", day) + ":not(.react-datepicker__day--outside-month)").first().click();
    }

    @Step("Fill subject: {subject}")
    public void fillSubjects(String subject) {
        page.fill("#subjectsInput", subject);
        page.keyboard().press("Enter");
    }

    @Step("Select hobby: {hobby}")
    public void selectHobby(String hobby) {
        Locator locator = switch (hobby.toLowerCase()) {
            case "sports" -> page.locator("label[for='hobbies-checkbox-1']");
            case "reading" -> page.locator("label[for='hobbies-checkbox-2']");
            case "music" -> page.locator("label[for='hobbies-checkbox-3']");
            default -> throw new IllegalArgumentException("Unknown hobby: " + hobby);
        };
        locator.click(new Locator.ClickOptions().setForce(true));
    }

    @Step("Upload file: {filePath}")
    public void uploadFile(String filePath) {
        page.setInputFiles("#uploadPicture", Paths.get(filePath));
    }

    @Step("Fill address: {value}")
    public void fillAddress(String value) {
        page.fill("#currentAddress", value);
    }

    @Step("Select state: {state}")
    public void selectState(String state) {
        Locator stateDropdown = page.locator("#state");
        stateDropdown.scrollIntoViewIfNeeded();
        stateDropdown.click();
        page.locator("#stateCity-wrapper")
                .locator("div", new Locator.LocatorOptions().setHasText(state))
                .last()
                .click();
    }

    @Step("Select city: {city}")
    public void selectCity(String city) {
        Locator cityDropdown = page.locator("#city");
        cityDropdown.scrollIntoViewIfNeeded();
        cityDropdown.click();
        page.locator("#stateCity-wrapper")
                .locator("div", new Locator.LocatorOptions().setHasText(city))
                .last()
                .click();
    }

    @Step("Submit the form")
    public void submit() {
        Locator submitButton = page.locator("#submit");
        submitButton.scrollIntoViewIfNeeded();
        submitButton.click();
    }

    @Step("Check success modal is visible")
    public boolean isSuccessModalVisible() {
        return page.locator("#example-modal-sizes-title-lg").isVisible();
    }

    @Step("Get success modal title")
    public String getSuccessModalTitle() {
        return page.locator("#example-modal-sizes-title-lg").textContent();
    }

    @Step("Get submitted value for: {label}")
    public String getSubmittedValue(String label) {
        return page.locator(".table-responsive tr", new Page.LocatorOptions().setHasText(label))
                .locator("td").nth(1)
                .textContent();
    }
}
