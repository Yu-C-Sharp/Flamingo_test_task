package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Cannot load test.properties", exception);
        }
    }

    // Restful Booker
    public static final String BOOKER_BASE_URL = get("booker.base.url", "https://restful-booker.herokuapp.com");
    public static final String BOOKER_USERNAME = get("booker.username", "admin");
    public static final String BOOKER_PASSWORD = get("booker.password", "password123");

    // Rick and Morty GraphQL
    public static final String GRAPHQL_URL = get("graphql.url", "https://rickandmortyapi.com/graphql");

    // Playwright
    public static final boolean HEADLESS = Boolean.parseBoolean(get("browser.headless", "false"));
    public static final int TIMEOUT_MS = Integer.parseInt(get("browser.timeout.ms", "30000"));

    // DemoQA
    public static final String DEMOQA_BASE_URL = get("demoqa.base.url", "https://demoqa.com");
    public static final String DEMOQA_FORM_URL = DEMOQA_BASE_URL + "/automation-practice-form";
    public static final String DEMOQA_WEBTABLES_URL = DEMOQA_BASE_URL + "/webtables";
    public static final String DEMOQA_BOOKS_URL = DEMOQA_BASE_URL + "/books";

    // Test files
    public static final String STUDENT_FILE_PATH = get("student.file.path", "");
    public static final String EMPLOYEE_FILE_PATH = get("employee.file.path", "");

    private static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
