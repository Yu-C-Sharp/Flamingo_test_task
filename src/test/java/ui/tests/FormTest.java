package ui.tests;

import ui.models.StudentFormData;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import ui.base.BaseUITest;
import ui.pages.FormPage;
import utils.enums.StudentRegistrationTableLabels;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Log
@Tag("ui")
@Feature("Student Registration Form")
class FormTest extends BaseUITest {

    private FormPage form;
    private static File tempFile;

    @BeforeEach
    void openFormPage() {
        page.navigate(TestConfig.DEMOQA_FORM_URL);
        form = new FormPage(page);
    }

    @AfterEach
    void cleanTestData() {
        removeTestFile(tempFile.toPath());
    }

    @Test
    @Description("Fill and submit full student registration form, verify success modal appears")
    void shouldSubmitCompleteStudentRegistrationForm() {
        tempFile = createFile("upload-file", ".txt");
        writeTestContentIntoFile(tempFile.toPath());

        StudentFormData testData = loadJsonData(TestConfig.STUDENT_FILE_PATH, StudentFormData.class).getFirst();
        fillAndSubmitForm(testData, tempFile);

        assertThat(form.isSuccessModalVisible())
                .as("Success modal should be visible after form submission")
                .isTrue();
        assertThat(form.getSuccessModalTitle())
                .isEqualTo("Thanks for submitting the form");
    }

    @Test
    @Description("Submitted form data should be visible in the success modal table")
    void shouldVerifySubmittedDataInModal() {
        tempFile = createFile("test-upload", ".png");

        StudentFormData testData = loadJsonData(TestConfig.STUDENT_FILE_PATH, StudentFormData.class).get(1);
        Map<StudentRegistrationTableLabels, String> expectedTableData = toTableMap(testData, tempFile.getName());

        fillAndSubmitForm(testData, tempFile);

        assertThat(form.isSuccessModalVisible()).isTrue();
        expectedTableData.forEach((label, expectedValue) -> {
            String value = form.getSubmittedValue(label.getValue());
            assertThat(value)
                    .as("Checking value for: " + label)
                    .isEqualTo(expectedValue);
            logger.info(String.format("Value [%s] for column [%s] checked", value, label));
        });
    }

    private void fillAndSubmitForm(StudentFormData formData, File uploadFile) {
        form.fillFirstName(formData.getFirstName());
        form.fillLastName(formData.getLastName());
        form.fillEmail(formData.getEmail());
        form.selectGender(formData.getGender());
        form.fillMobile(formData.getPhone());
        form.selectDateOfBirth(formData.getBirth().getMonth(), formData.getBirth().getYear(), formData.getBirth().getDay());
        form.fillSubjects(formData.getSubject());
        form.selectHobby(formData.getHobby());
        form.uploadFile(uploadFile.getPath());
        form.fillAddress(formData.getAddress());
        form.selectState(formData.getState());
        form.selectCity(formData.getCity());
        form.submit();
    }

    private Map<StudentRegistrationTableLabels, String> toTableMap(StudentFormData data, String fullFileName) {
        Map<StudentRegistrationTableLabels, String> map = new HashMap<>();
        map.put(StudentRegistrationTableLabels.STUDENT_FULL_NAME, data.getFirstName() + " " + data.getLastName());
        map.put(StudentRegistrationTableLabels.STUDENT_EMAIL, data.getEmail());
        map.put(StudentRegistrationTableLabels.GENDER, data.getGender());
        map.put(StudentRegistrationTableLabels.PHONE, data.getPhone());
        map.put(StudentRegistrationTableLabels.BIRTHDAY, data.getBirth().getDay() + " " + data.getBirth().getMonth() + "," + data.getBirth().getYear());
        map.put(StudentRegistrationTableLabels.SUBJECTS, data.getSubject());
        map.put(StudentRegistrationTableLabels.HOBBIES, data.getHobby());
        map.put(StudentRegistrationTableLabels.FULL_FILE_NAME, fullFileName);
        map.put(StudentRegistrationTableLabels.ADDRESS, data.getAddress());
        map.put(StudentRegistrationTableLabels.STATE_AND_CITY, data.getState() + " " + data.getCity());

        return map;
    }
}
