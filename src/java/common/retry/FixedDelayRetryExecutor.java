package common.retry;

import java.util.concurrent.Callable;

public class FixedDelayRetryExecutor implements RetryExecutor {
    private final int maxAttempts;
    private final long delayMillis;

    public FixedDelayRetryExecutor(int maxAttempts, long delayMillis) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.delayMillis = Math.max(0L, delayMillis);
    }

    @Override
    public <T> T execute(Callable<T> action) {
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.call();
            } catch (RuntimeException e) {
                lastError = e;
            } catch (Exception e) {
                lastError = new RuntimeException(e.getMessage(), e);
            }

            if (attempt < maxAttempts && delayMillis > 0L) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", e);
                }
            }
        }

        throw lastError == null ? new RuntimeException("Retry failed") : lastError;
    }
}
