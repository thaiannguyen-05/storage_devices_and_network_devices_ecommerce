package module.core.auth;

import entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import module.core.config.ConfigService;

public class TokenService {
    private static final String DEFAULT_JWT_SECRET = "storeit-dev-secret-storeit-dev-secret-2026";
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    private final AuthConfig authConfig;
    private final SecretKey jwtSigningKey;
    private final int accessTokenMinutes;
    private final int refreshTokenDays;

    public TokenService() {
        this.authConfig = new AuthConfig();
        this.jwtSigningKey = Keys.hmacShaKeyFor(resolveJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = authConfig.getAccessTokenMinutes();
        this.refreshTokenDays = authConfig.getRefreshTokenDays();
    }

    public String generateAccessToken(UserEntity user, String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenMinutes * SECONDS_PER_MINUTE);
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("sessionId", sessionId)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(jwtSigningKey)
                .compact();
    }

    public String generateRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds((long) refreshTokenDays * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("type", "refresh")
                .claim("nonce", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(jwtSigningKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return parseToken(token, "access");
    }

    public Claims parseRefreshToken(String token) {
        return parseToken(token, "refresh");
    }

    public int getAccessTokenMaxAgeSeconds() {
        return accessTokenMinutes * SECONDS_PER_MINUTE;
    }

    public int getRefreshTokenMaxAgeSeconds() {
        return refreshTokenDays * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
    }

    private Claims parseToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSigningKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String actualType = claims.get("type", String.class);
            if (actualType == null || !expectedType.equalsIgnoreCase(actualType)) {
                throw new RuntimeException("Invalid token type.");
            }

            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid or expired token.", e);
        }
    }

    private String resolveJwtSecret() {
        String secret = ConfigService.getOrDefault("JWT_SECRET", DEFAULT_JWT_SECRET);
        if (secret == null) {
            secret = DEFAULT_JWT_SECRET;
        }
        secret = secret.trim();
        if (secret.length() < authConfig.getJwtSecretMinLength()) {
            secret = secret + DEFAULT_JWT_SECRET;
        }
        return secret;
    }
}
