package common.retry;

import java.util.concurrent.Callable;

public final class RetryExecutor {
    private RetryExecutor() {
    }

    public static <T> T execute(Callable<T> callable, RetryConfig<T> config) {
        return executeWithResult(callable, config).getResult();
    }

    public static <T> RetryResult<T> executeWithResult(Callable<T> callable, RetryConfig<T> config) {
        if (callable == null) {
            throw new IllegalArgumentException("callable must not be null");
        }
        RetryConfig<T> effectiveConfig = config == null ? RetryConfig.<T>builder().build() : config;
        long totalDelayMs = 0L;
        int attempt = 0;

        while (true) {
            attempt++;
            try {
                T result = callable.call();
                for (RetryListener<T> listener : effectiveConfig.getListeners()) {
                    listener.onSuccess(attempt, result);
                }
                return new RetryResult<T>(result, attempt, totalDelayMs, true);
            } catch (Exception error) {
                boolean exhausted = attempt > effectiveConfig.getMaxRetries();
                if (exhausted || !effectiveConfig.canRetry(error)) {
                    for (RetryListener<T> listener : effectiveConfig.getListeners()) {
                        listener.onFailure(attempt, error);
                    }
                    throw new RetryExhaustedException("Retry exhausted after " + attempt + " attempt(s)", error);
                }

                for (RetryListener<T> listener : effectiveConfig.getListeners()) {
                    listener.onRetry(attempt, error);
                }

                long delayMs = calculateDelay(effectiveConfig, attempt);
                totalDelayMs += delayMs;
                sleep(delayMs);
            }
        }
    }

    private static long calculateDelay(RetryConfig<?> config, int failedAttempt) {
        long initialDelayMs = config.getInitialDelayMs();
        if (initialDelayMs <= 0) {
            return 0L;
        }
        if (config.getStrategy() == BackoffStrategy.EXPONENTIAL) {
            return initialDelayMs * pow2(failedAttempt - 1);
        }
        if (config.getStrategy() == BackoffStrategy.FIBONACCI) {
            return initialDelayMs * fibonacci(failedAttempt);
        }
        return initialDelayMs;
    }

    private static long pow2(int exponent) {
        long value = 1L;
        for (int i = 0; i < exponent; i++) {
            value *= 2L;
        }
        return value;
    }

    private static long fibonacci(int n) {
        if (n <= 1) {
            return 1L;
        }
        long previous = 1L;
        long current = 1L;
        for (int i = 2; i <= n; i++) {
            long next = previous + current;
            previous = current;
            current = next;
        }
        return current;
    }

    private static void sleep(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new RetryExhaustedException("Retry interrupted", error);
        }
    }
}
