package module.core.auth;

import entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import module.core.config.ConfigService;

public class TokenService {
    private static final String DEFAULT_JWT_SECRET = "storeit-dev-secret-storeit-dev-secret-2026";

    private final Key jwtSigningKey;
    private final int accessTokenMinutes;
    private final int refreshTokenDays;

    public TokenService() {
        this.jwtSigningKey = Keys.hmacShaKeyFor(resolveJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = Math.max(1, ConfigService.getInt("JWT_ACCESS_TOKEN_MINUTES", 15));
        this.refreshTokenDays = Math.max(1, ConfigService.getInt("JWT_REFRESH_TOKEN_DAYS", 30));
    }

    public String generateAccessToken(UserEntity user, String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenMinutes * 60L);
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
        Instant expiresAt = now.plusSeconds(refreshTokenDays * 24L * 60L * 60L);
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

    private String resolveJwtSecret() {
        String secret = ConfigService.getOrDefault("JWT_SECRET", DEFAULT_JWT_SECRET);
        if (secret == null) {
            secret = DEFAULT_JWT_SECRET;
        }
        secret = secret.trim();
        if (secret.length() < 32) {
            secret = secret + DEFAULT_JWT_SECRET;
        }
        return secret;
    }
}
