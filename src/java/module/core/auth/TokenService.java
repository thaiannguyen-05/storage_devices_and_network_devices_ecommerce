package module.core.auth;

import entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

    public Claims parseAccessTokenAllowExpired(String token) {
        return parseTokenAllowExpired(token, "access");
    }

    public int getAccessTokenMaxAgeSeconds() {
        return accessTokenMinutes * SECONDS_PER_MINUTE;
    }

    public int getRefreshTokenMaxAgeSeconds() {
        return refreshTokenDays * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
    }

    private Claims parseToken(String token, String expectedType) {
        try {
            return parseClaims(token, expectedType);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid or expired token.", e);
        }
    }

    private Claims parseTokenAllowExpired(String token, String expectedType) {
        try {
            return parseClaims(token, expectedType);
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            validateTokenType(claims, expectedType);
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid token.", e);
        }
    }

    private Claims parseClaims(String token, String expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        validateTokenType(claims, expectedType);
        return claims;
    }

    private void validateTokenType(Claims claims, String expectedType) {
        String actualType = claims.get("type", String.class);
        if (actualType == null || !expectedType.equalsIgnoreCase(actualType)) {
            throw new RuntimeException("Invalid token type.");
        }
    }

    private String resolveJwtSecret() {
        String secret = ConfigService.get("JWT_SECRET");
        if (secret == null || secret.trim().isBlank()) {
            throw new IllegalStateException("JWT_SECRET is required.");
        }
        secret = secret.trim();
        if (secret.length() < authConfig.getJwtSecretMinLength()) {
            throw new IllegalStateException("JWT_SECRET is too short.");
        }
        return secret;
    }
}
