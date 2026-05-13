package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.nio.file.Paths;

public class FormPage {

    private final Page page;

    public FormPage(Page page) {
        this.page = page;
    }

    public void fillFirstName(String value) {
        page.fill("#firstName", value);
    }

    public void fillLastName(String value) {
        page.fill("#lastName", value);
    }

    public void fillEmail(String value) {
        page.fill("#userEmail", value);
    }

    public void selectGender(String gender) {
        Locator label;
        if ("Female".equalsIgnoreCase(gender)) {
            label = page.locator("label[for='gender-radio-2']");
        } else if ("Other".equalsIgnoreCase(gender)) {
            label = page.locator("label[for='gender-radio-3']");
        } else {
            label = page.locator("label[for='gender-radio-1']");
        }
        label.scrollIntoViewIfNeeded();
        label.click(new Locator.ClickOptions().setForce(true));
    }

    public void fillMobile(String value) {
        page.fill("#userNumber", value);
    }

    public void selectDateOfBirth(String month, String year, int day) {
        page.click("#dateOfBirthInput");
        page.selectOption(".react-datepicker__month-select", new SelectOption().setLabel(month));
        page.selectOption(".react-datepicker__year-select", new SelectOption().setLabel(year));
        page.locator(".react-datepicker__day--0" + String.format("%02d", day) + ":not(.react-datepicker__day--outside-month)").first().click();
    }

    public void fillSubjects(String subject) {
        page.fill("#subjectsInput", subject);
        page.keyboard().press("Enter");
    }

    public void selectHobby(String hobby) {
        Locator label = switch (hobby.toLowerCase()) {
            case "sports" -> page.locator("label[for='hobbies-checkbox-1']");
            case "reading" -> page.locator("label[for='hobbies-checkbox-2']");
            case "music" -> page.locator("label[for='hobbies-checkbox-3']");
            default -> throw new IllegalArgumentException("Unknown hobby: " + hobby);
        };
        label.scrollIntoViewIfNeeded();
        label.click(new Locator.ClickOptions().setForce(true));
    }

    public void uploadFile(String filePath) {
        page.setInputFiles("#uploadPicture", Paths.get(filePath));
    }

    public void fillAddress(String value) {
        page.fill("#currentAddress", value);
    }

    public void selectState(String state) {
        Locator stateDropdown = page.locator("#state");
        stateDropdown.scrollIntoViewIfNeeded();
        stateDropdown.click(new Locator.ClickOptions().setForce(true));
        page.locator("#stateCity-wrapper").locator("div", new Locator.LocatorOptions().setHasText(state)).last()
                .click(new Locator.ClickOptions().setForce(true));
    }

    public void selectCity(String city) {
        Locator cityDropdown = page.locator("#city");
        cityDropdown.scrollIntoViewIfNeeded();
        cityDropdown.click(new Locator.ClickOptions().setForce(true));
        page.locator("#stateCity-wrapper").locator("div", new Locator.LocatorOptions().setHasText(city)).last()
                .click(new Locator.ClickOptions().setForce(true));
    }

    public void submit() {
        page.locator("#submit").scrollIntoViewIfNeeded();
        page.locator("#submit").click(new Locator.ClickOptions().setForce(true));
    }

    public boolean isSuccessModalVisible() {
        return page.locator("#example-modal-sizes-title-lg").isVisible();
    }

    public String getSuccessModalTitle() {
        return page.locator("#example-modal-sizes-title-lg").textContent();
    }

    public String getSubmittedValue(String label) {
        return page.locator(".table-responsive tr", new Page.LocatorOptions().setHasText(label))
                .locator("td").nth(1)
                .textContent();
    }
}
