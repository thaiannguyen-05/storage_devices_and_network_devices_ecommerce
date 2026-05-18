package common.retry;

public interface RetryListener<T> {
    void onRetry(int attempt, Exception error);

    void onSuccess(int attempt, T result);

    void onFailure(int attempt, Exception error);
}
