package utils.extensions;

import config.TestConfig;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class AllureEnvironmentExtension implements BeforeAllCallback {

    private static final AtomicBoolean written = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (written.compareAndSet(false, true)) {
            writeEnvironmentProperties();
        }
    }

    private static void writeEnvironmentProperties() throws IOException {
        String dir = System.getProperty("allure.results.directory", "target/allure-results");
        Path path = Paths.get(dir);
        Files.createDirectories(path);

        Properties props = new Properties();
        props.setProperty("Booker.URL", TestConfig.BOOKER_BASE_URL);
        props.setProperty("GraphQL.URL", TestConfig.GRAPHQL_URL);
        props.setProperty("DemoQA.URL", TestConfig.DEMOQA_BASE_URL);
        props.setProperty("Browser.Headless", String.valueOf(TestConfig.HEADLESS));
        props.setProperty("Browser.Timeout.ms", String.valueOf(TestConfig.TIMEOUT_MS));
        props.setProperty("Java.Version", System.getProperty("java.version", "unknown"));
        props.setProperty("OS", System.getProperty("os.name", "unknown"));

        try (var writer = Files.newBufferedWriter(path.resolve("environment.properties"))) {
            props.store(writer, null);
        }
    }
}
