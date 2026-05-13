package ui.base;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class ScreenshotExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        context.getTestInstance()
                .filter(i -> i instanceof BaseUITest)
                .map(i -> (BaseUITest) i)
                .ifPresent(test -> {
                    String className = context.getTestClass()
                            .map(Class::getSimpleName)
                            .orElse("unknown");
                    String methodName = context.getDisplayName()
                            .replaceAll("[^a-zA-Z0-9_-]", "_");
                    test.takeScreenshot(className + "_" + methodName);
                });
    }
}
