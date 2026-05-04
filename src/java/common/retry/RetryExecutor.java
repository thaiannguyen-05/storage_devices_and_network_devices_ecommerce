package common.retry;

import java.util.concurrent.Callable;

public interface RetryExecutor {
    <T> T execute(Callable<T> action);
}
