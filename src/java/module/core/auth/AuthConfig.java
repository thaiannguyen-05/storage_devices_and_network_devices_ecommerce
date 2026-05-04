package module.core.auth;

import module.core.config.ConfigService;
import module.core.coreInterface.RetryDto;

public class AuthConfig {
    private static final int DEFAULT_JWT_SECRET_MIN_LENGTH = 32;
    private static final int DEFAULT_ACCESS_TOKEN_MINUTES = 15;
    private static final int DEFAULT_REFRESH_TOKEN_DAYS = 30;
    private static final int DEFAULT_RETRY_MAX = 3;
    private static final int DEFAULT_RETRY_DELAY_MS = 200;
    private static final int DEFAULT_RETRY_MAX_TIME_MS = 1000;
    private static final int DEFAULT_RETRY_RANDOM_STATE = 1;
    private static final int DEFAULT_ARGON2_ITERATIONS = 3;
    private static final int DEFAULT_ARGON2_MEMORY_KIB = 65536;
    private static final int DEFAULT_ARGON2_PARALLELISM = 1;
    private static final int DEFAULT_VERIFICATION_CODE_LENGTH = 6;
    private static final int DEFAULT_RESET_CODE_TTL_MINUTES = 10;

    private final int jwtSecretMinLength;
    private final int accessTokenMinutes;
    private final int refreshTokenDays;
    private final int argon2Iterations;
    private final int argon2MemoryKiB;
    private final int argon2Parallelism;
    private final int verificationCodeLength;
    private final int resetCodeTtlMinutes;
    private final RetryDto retryDto;

    public AuthConfig() {
        this.jwtSecretMinLength = Math.max(16, ConfigService.getInt("JWT_SECRET_MIN_LENGTH", DEFAULT_JWT_SECRET_MIN_LENGTH));
        this.accessTokenMinutes = Math.max(1, ConfigService.getInt("JWT_ACCESS_TOKEN_MINUTES", DEFAULT_ACCESS_TOKEN_MINUTES));
        this.refreshTokenDays = Math.max(1, ConfigService.getInt("JWT_REFRESH_TOKEN_DAYS", DEFAULT_REFRESH_TOKEN_DAYS));
        this.argon2Iterations = Math.max(1, ConfigService.getInt("AUTH_ARGON2_ITERATIONS", DEFAULT_ARGON2_ITERATIONS));
        this.argon2MemoryKiB = Math.max(8192, ConfigService.getInt("AUTH_ARGON2_MEMORY_KIB", DEFAULT_ARGON2_MEMORY_KIB));
        this.argon2Parallelism = Math.max(1, ConfigService.getInt("AUTH_ARGON2_PARALLELISM", DEFAULT_ARGON2_PARALLELISM));
        this.verificationCodeLength = Math.max(4, ConfigService.getInt("AUTH_VERIFICATION_CODE_LENGTH", DEFAULT_VERIFICATION_CODE_LENGTH));
        this.resetCodeTtlMinutes = Math.max(1, ConfigService.getInt("AUTH_RESET_CODE_TTL_MINUTES", DEFAULT_RESET_CODE_TTL_MINUTES));

        RetryDto configuredRetry = new RetryDto();
        configuredRetry.maxRetry = Math.max(1, ConfigService.getInt("AUTH_RETRY_MAX", DEFAULT_RETRY_MAX));
        configuredRetry.retryTime = Math.max(50, ConfigService.getInt("AUTH_RETRY_DELAY_MS", DEFAULT_RETRY_DELAY_MS));
        configuredRetry.maxTimeRetry = Math.max(configuredRetry.retryTime, ConfigService.getInt("AUTH_RETRY_MAX_TIME_MS", DEFAULT_RETRY_MAX_TIME_MS));
        configuredRetry.randomState = Math.max(1, ConfigService.getInt("AUTH_RETRY_RANDOM_STATE", DEFAULT_RETRY_RANDOM_STATE));
        this.retryDto = configuredRetry;
    }

    public int getJwtSecretMinLength() {
        return jwtSecretMinLength;
    }

    public int getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public int getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public int getArgon2Iterations() {
        return argon2Iterations;
    }

    public int getArgon2MemoryKiB() {
        return argon2MemoryKiB;
    }

    public int getArgon2Parallelism() {
        return argon2Parallelism;
    }

    public int getVerificationCodeLength() {
        return verificationCodeLength;
    }

    public int getResetCodeTtlMinutes() {
        return resetCodeTtlMinutes;
    }

    public RetryDto createRetryDto() {
        RetryDto copy = new RetryDto();
        copy.maxRetry = retryDto.maxRetry;
        copy.retryTime = retryDto.retryTime;
        copy.maxTimeRetry = retryDto.maxTimeRetry;
        copy.randomState = retryDto.randomState;
        return copy;
    }
}
