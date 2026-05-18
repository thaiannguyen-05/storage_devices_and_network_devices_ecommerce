package common.retry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RetryConfig<T> {
    private final int maxRetries;
    private final long initialDelayMs;
    private final BackoffStrategy strategy;
    private final List<Class<? extends Exception>> retryableExceptions;
    private final List<RetryListener<T>> listeners;

    private RetryConfig(Builder<T> builder) {
        this.maxRetries = builder.maxRetries;
        this.initialDelayMs = builder.initialDelayMs;
        this.strategy = builder.strategy;
        this.retryableExceptions = Collections.unmodifiableList(new ArrayList<Class<? extends Exception>>(builder.retryableExceptions));
        this.listeners = Collections.unmodifiableList(new ArrayList<RetryListener<T>>(builder.listeners));
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public BackoffStrategy getStrategy() {
        return strategy;
    }

    public List<Class<? extends Exception>> getRetryableExceptions() {
        return retryableExceptions;
    }

    public List<RetryListener<T>> getListeners() {
        return listeners;
    }

    public boolean canRetry(Exception error) {
        if (retryableExceptions.isEmpty()) {
            return true;
        }
        for (Class<? extends Exception> exceptionType : retryableExceptions) {
            if (exceptionType.isInstance(error)) {
                return true;
            }
        }
        return false;
    }

    public static class Builder<T> {
        private int maxRetries = 3;
        private long initialDelayMs = 1000L;
        private BackoffStrategy strategy = BackoffStrategy.FIXED;
        private final List<Class<? extends Exception>> retryableExceptions = new ArrayList<Class<? extends Exception>>();
        private final List<RetryListener<T>> listeners = new ArrayList<RetryListener<T>>();

        public Builder<T> maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be greater than or equal to 0");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder<T> initialDelayMs(long initialDelayMs) {
            if (initialDelayMs < 0) {
                throw new IllegalArgumentException("initialDelayMs must be greater than or equal to 0");
            }
            this.initialDelayMs = initialDelayMs;
            return this;
        }

        public Builder<T> strategy(BackoffStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("strategy must not be null");
            }
            this.strategy = strategy;
            return this;
        }

        @SafeVarargs
        public final Builder<T> retryOn(Class<? extends Exception>... exceptionTypes) {
            if (exceptionTypes != null) {
                for (Class<? extends Exception> exceptionType : exceptionTypes) {
                    if (exceptionType != null) {
                        retryableExceptions.add(exceptionType);
                    }
                }
            }
            return this;
        }

        public Builder<T> addListener(RetryListener<T> listener) {
            if (listener != null) {
                listeners.add(listener);
            }
            return this;
        }

        public RetryConfig<T> build() {
            return new RetryConfig<T>(this);
        }
    }
}
