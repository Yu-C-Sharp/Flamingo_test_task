package ui.tests;

import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.base.BaseUITest;
import ui.models.EmployeeFormData;
import ui.pages.WebTablePage;
import utils.enums.EmployeeRegistrationTableLabels;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Log
@Tag("ui")
@Feature("Web Tables")
class WebTableTest extends BaseUITest {

    private WebTablePage tablePage;

    @BeforeEach
    void openWebTables() {
        page.navigate(TestConfig.DEMOQA_WEBTABLES_URL);
        tablePage = new WebTablePage(page);
    }

    @Test
    @Description("Add a new record and verify it appears in the table")
    void shouldAddNewRecordSuccessfully() {

        EmployeeFormData testData = loadJsonData(TestConfig.EMPLOYEE_FILE_PATH, EmployeeFormData.class).getFirst();
        Map<EmployeeRegistrationTableLabels, String> expectedTableData = toTableMap(testData);

        fillAndSubmitForm(testData);

        tablePage.waitForRecordVisible(testData.getFirstName(), true);

        expectedTableData.forEach((column, expectedValue) -> {
            assertThat(tablePage.getCellValue(testData.getFirstName(), column))
                    .as("Checking column: " + column.getValue())
                    .isEqualTo(expectedValue);
            logger.info(String.format("Value [%s] for column [%s] checked", expectedValue, column));
        });
    }

    @Test
    @Description("Edit an existing record and verify the change is saved")
    void shouldEditExistingRecord() {

        EmployeeFormData testData = loadJsonData(TestConfig.EMPLOYEE_FILE_PATH, EmployeeFormData.class).get(1);
        EmployeeFormData newData = new EmployeeFormData(
                testData.getFirstName() + "Edited", testData.getLastName() + "Edited",
                testData.getAge() + 10, "tester78584@mail.com",
                testData.getSalary() + 777, testData.getDepartment() + "Edited"
        );
        Map<EmployeeRegistrationTableLabels, String> expectedTableData = toTableMap(newData);

        // The table has preloaded records; edit 'Cierra'
        tablePage.openEditRecordModalByName(testData.getFirstName());
        WebTablePage.EditModal.updateFirstName(newData.getFirstName());
        WebTablePage.EditModal.updateLastName(newData.getLastName());
        WebTablePage.EditModal.updateAge(String.valueOf(newData.getAge()));
        WebTablePage.EditModal.updateEmail(newData.getEmail());
        WebTablePage.EditModal.updateSalary(String.valueOf(newData.getSalary()));
        WebTablePage.EditModal.updateDepartment(newData.getDepartment());

        tablePage.waitForRecordVisible(newData.getFirstName(), true);

        expectedTableData.forEach((column, expectedValue) -> assertThat(tablePage.getCellValue(newData.getFirstName(), column))
                .as("Checking column: " + column.getValue())
                .isEqualTo(expectedValue));
    }

    @Test
    @Description("Delete a record and verify it no longer appears in the table")
    void shouldDeleteRecord() {
        // Alden is one of the preloaded records
        final String expectedValue = "Alden";
        tablePage.deleteRecordByName(expectedValue);
        tablePage.waitForRecordVisible(expectedValue, false);
    }

    @Test
    @Description("Search functionality filters the table to show only matching rows")
    void shouldSearchAndFilterRecords() {
        final String expectedValue = "Vega";
        final String unexpectedValue = "Alden";
        tablePage.search(expectedValue);
        tablePage.waitForRecordVisible(expectedValue, true);
        tablePage.waitForRecordVisible(unexpectedValue, false);
    }

    private void fillAndSubmitForm(EmployeeFormData formData) {
        tablePage.addRecord(
                formData.getFirstName(), formData.getLastName(), formData.getEmail(),
                formData.getAge(), formData.getSalary(), formData.getDepartment()
        );
    }

    private Map<EmployeeRegistrationTableLabels, String> toTableMap(EmployeeFormData data) {
        Map<EmployeeRegistrationTableLabels, String> map = new HashMap<>();
        map.put(EmployeeRegistrationTableLabels.FIRST_NAME, data.getFirstName());
        map.put(EmployeeRegistrationTableLabels.LAST_NAME, data.getLastName());
        map.put(EmployeeRegistrationTableLabels.AGE, String.valueOf(data.getAge()));
        map.put(EmployeeRegistrationTableLabels.EMAIL, data.getEmail());
        map.put(EmployeeRegistrationTableLabels.SALARY, String.valueOf(data.getSalary()));
        map.put(EmployeeRegistrationTableLabels.DEPARTMENT, data.getDepartment());

        return map;
    }
}
