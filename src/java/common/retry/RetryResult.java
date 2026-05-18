package common.retry;

public class RetryResult<T> {
    private T result;
    private int attemptCount;
    private long totalDelayMs;
    private boolean succeeded;

    public RetryResult() {
    }

    public RetryResult(T result, int attemptCount, long totalDelayMs, boolean succeeded) {
        this.result = result;
        this.attemptCount = attemptCount;
        this.totalDelayMs = totalDelayMs;
        this.succeeded = succeeded;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public long getTotalDelayMs() {
        return totalDelayMs;
    }

    public void setTotalDelayMs(long totalDelayMs) {
        this.totalDelayMs = totalDelayMs;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }
}
