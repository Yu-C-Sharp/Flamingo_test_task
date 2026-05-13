package ui.tests;

import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import ui.base.BaseUITest;
import ui.pages.WebTablePage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Feature("Web Tables")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebTableTest extends BaseUITest {

    private WebTablePage tablePage;

    @BeforeEach
    void openWebTables() {
        page.navigate(TestConfig.DEMOQA_WEBTABLES_URL);
        page.evaluate("document.querySelectorAll('#fixedban, iframe').forEach(e => e.remove())");
        tablePage = new WebTablePage(page);
    }

    @Test
    @Order(1)
    @Description("Add a new record and verify it appears in the table")
    void shouldAddNewRecordSuccessfully() {
        tablePage.addRecord("Alice", "Walker", "alice@test.com", "28", "60000", "Engineering");

        assertThat(tablePage.isRecordVisible("Alice"))
                .as("Newly added record should be visible in the table")
                .isTrue();
    }

    @Test
    @Order(2)
    @Description("Edit an existing record and verify the change is saved")
    void shouldEditExistingRecord() {
        // The table has pre-loaded records; edit 'Cierra'
        tablePage.editRecordByName("Cierra");
        tablePage.updateFirstName("CierraEdited");

        assertThat(tablePage.isRecordVisible("CierraEdited"))
                .as("Edited record should appear with updated name")
                .isTrue();
    }

    @Test
    @Order(3)
    @Description("Delete a record and verify it no longer appears in the table")
    void shouldDeleteRecord() {
        // Alden is one of the pre-loaded records
        tablePage.deleteRecordByName("Alden");

        assertThat(tablePage.isRecordVisible("Alden"))
                .as("Deleted record should not be visible")
                .isFalse();
    }

    @Test
    @Order(4)
    @Description("Search functionality filters the table to show only matching rows")
    void shouldSearchAndFilterRecords() {
        tablePage.search("Vega");

        assertThat(tablePage.isRecordVisible("Vega"))
                .as("Search result should contain the matching record")
                .isTrue();

        assertThat(tablePage.isRecordVisible("Cierra"))
                .as("Non-matching record should not be visible after search")
                .isFalse();
    }

    @Test
    @Order(5)
    @Description("Click column header sorts the table ascending by that column")
    void shouldSortByFirstNameAscending() {
        tablePage.clickColumnHeader("First Name");

        List<String> names = tablePage.getColumnValues("1");

        assertThat(names).as("Names should be sorted A→Z").isSortedAccordingTo(String::compareToIgnoreCase);
    }
}
