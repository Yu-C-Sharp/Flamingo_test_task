package ui.tests;

import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.base.BaseUITest;
import ui.pages.FormPage;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Feature("Student Registration Form")
class FormTest extends BaseUITest {

    @Test
    @Description("Fill and submit full student registration form, verify success modal appears")
    void shouldSubmitCompleteStudentRegistrationForm() throws Exception {
        page.navigate(TestConfig.DEMOQA_FORM_URL);
        page.evaluate("document.querySelectorAll('#fixedban, iframe').forEach(e => e.remove())");

        FormPage form = new FormPage(page);

        form.fillFirstName("John");
        form.fillLastName("Doe");
        form.fillEmail("john.doe@example.com");
        form.selectGender("Male");
        form.fillMobile("1234567890");
        form.selectDateOfBirth("May", "1990", 15);
        form.fillSubjects("Maths");
        form.selectHobby("Sports");

        // Create a temp file to simulate upload
        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.writeString(tempFile, "test content");
        form.uploadFile(tempFile.toString());

        form.fillAddress("123 Main Street, City");
        form.selectState("NCR");
        form.selectCity("Delhi");
        form.submit();

        assertThat(form.isSuccessModalVisible())
                .as("Success modal should be visible after form submission")
                .isTrue();

        assertThat(form.getSuccessModalTitle())
                .isEqualTo("Thanks for submitting the form");

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Description("Submitted form data should be visible in the success modal table")
    void shouldVerifySubmittedDataInModal() throws Exception {
        page.navigate(TestConfig.DEMOQA_FORM_URL);
        page.evaluate("document.querySelectorAll('#fixedban, iframe').forEach(e => e.remove())");

        FormPage form = new FormPage(page);

        form.fillFirstName("Jane");
        form.fillLastName("Smith");
        form.fillEmail("jane.smith@example.com");
        form.selectGender("Female");
        form.fillMobile("9876543210");
        form.selectDateOfBirth("June", "1995", 20);
        form.fillSubjects("English");
        form.selectHobby("Reading");

        Path tempFile = Files.createTempFile("test-upload", ".png");
        form.uploadFile(tempFile.toString());

        form.fillAddress("456 Oak Avenue");
        form.selectState("Uttar Pradesh");
        form.selectCity("Agra");
        form.submit();

        assertThat(form.isSuccessModalVisible()).isTrue();

        assertThat(form.getSubmittedValue("Student Name")).isEqualTo("Jane Smith");
        assertThat(form.getSubmittedValue("Student Email")).isEqualTo("jane.smith@example.com");
        assertThat(form.getSubmittedValue("Gender")).isEqualTo("Female");
        assertThat(form.getSubmittedValue("Mobile")).isEqualTo("9876543210");

        Files.deleteIfExists(tempFile);
    }
}
