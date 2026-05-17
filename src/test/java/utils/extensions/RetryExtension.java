package utils.extensions;

import lombok.extern.java.Log;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Log
public class RetryExtension implements InvocationInterceptor {

    private static final int DEFAULT_CI_RETRY_COUNT = 3;
    private static final String RETRY_COUNT_PROP = "retry.count";

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        int maxAttempts = resolveMaxAttempts();
        String testName = extensionContext.getDisplayName();
        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attempt == 1) {
                    invocation.proceed();
                } else {
                    logger.warning(String.format("[Retry] Attempt %d/%d: %s", attempt, maxAttempts, testName));
                    invocationContext.getExecutable().invoke(
                            extensionContext.getRequiredTestInstance(),
                            invocationContext.getArguments().toArray()
                    );
                    logger.info(String.format("[Retry] Passed on attempt %d: %s", attempt, testName));
                }
                return;
            } catch (InvocationTargetException e) {
                lastException = e.getCause();
                logger.warning(String.format("[Retry] Attempt %d/%d FAILED — %s: %s",
                        attempt, maxAttempts, testName, lastException.getMessage()));
            } catch (Throwable t) {
                lastException = t;
                logger.warning(String.format("[Retry] Attempt %d/%d FAILED — %s: %s",
                        attempt, maxAttempts, testName, lastException.getMessage()));
            }
        }

        logger.severe(String.format("[Retry] All %d attempts exhausted: %s", maxAttempts, testName));
        throw lastException;
    }

    private static int resolveMaxAttempts() {
        String prop = System.getProperty(RETRY_COUNT_PROP);
        if (prop != null) {
            try {
                int count = Integer.parseInt(prop);
                logger.info("[Retry] Using retry.count from system property: " + count);
                return Math.max(1, count);
            } catch (NumberFormatException e) {
                logger.warning("[Retry] Invalid retry.count value '" + prop + "' — ignoring");
            }
        }
        return isCI() ? DEFAULT_CI_RETRY_COUNT : 1;
    }

    private static boolean isCI() {
        String ci = System.getenv("CI");
        return ci != null && !ci.isBlank();
    }
}
