package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load test.properties", e);
        }
    }

    // Restful Booker
    public static final String BOOKER_BASE_URL = get("booker.base.url", "https://restful-booker.herokuapp.com");
    public static final String BOOKER_USERNAME  = get("booker.username", "admin");
    public static final String BOOKER_PASSWORD  = get("booker.password", "password123");

    // Rick and Morty GraphQL
    public static final String GRAPHQL_URL = get("graphql.url", "https://rickandmortyapi.com/graphql");

    // Playwright
    public static final boolean HEADLESS    = Boolean.parseBoolean(get("browser.headless", "true"));
    public static final int     TIMEOUT_MS  = Integer.parseInt(get("browser.timeout.ms", "30000"));

    // DemoQA
    public static final String DEMOQA_BASE_URL      = get("demoqa.base.url", "https://demoqa.com");
    public static final String DEMOQA_FORM_URL       = DEMOQA_BASE_URL + "/automation-practice-form";
    public static final String DEMOQA_WEBTABLES_URL  = DEMOQA_BASE_URL + "/webtables";

    private static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
