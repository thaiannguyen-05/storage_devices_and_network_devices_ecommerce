package module.core.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import entity.OutBoxEntity;
import entity.PasswordResetTokenEntity;
import entity.SessionEntity;
import entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import module.bussiness.notification.EmailService;
import module.core.auth.dto.ForgotPasswordRequestDto;
import module.core.auth.dto.ProfileRequestDto;
import module.core.auth.dto.ResetPasswordRequestDto;
import module.core.auth.dto.SigninRequestDto;
import module.core.auth.dto.SignupRequestDto;
import module.core.auth.dto.VerifyEmailCodeRequestDto;
import module.core.auth.repository.impl.PasswordResetTokenRepository;
import module.core.auth.repository.impl.SessionRepository;
import module.core.auth.response_dto.ForgotPasswordResponseDto;
import module.core.auth.response_dto.ProfileResponseDto;
import module.core.auth.response_dto.ResetPasswordResponseDto;
import module.core.auth.response_dto.SigninResponseDto;
import module.core.auth.response_dto.SignupResponseDto;
import module.core.auth.response_dto.VerifyEmailCodeResponseDto;
import module.core.config.ConfigService;
import module.core.coreInterface.CoreInterface;
import module.core.coreInterface.RetryDto;
import module.core.outbox.OutBoxRepository;
import module.core.outbox.TypeEvent;
import module.core.user.UserService;
import module.core.user.dto.CreateUserDto;

public class AuthService {
    private static final String DEFAULT_JWT_SECRET = "storeit-dev-secret-storeit-dev-secret-2026";

    private final UserService userService;
    private final OutBoxRepository outBoxRepository;
    private final EmailService emailService;
    private final RetryDto retryDto;
    private final TypeEvent typeEventOubox;
    private final SessionRepository sessionRepository;
    private final Key jwtSigningKey;
    private final int accessTokenMinutes;
    private final int refreshTokenDays;

    public AuthService() {
        this.userService = new UserService();
        this.outBoxRepository = new OutBoxRepository();
        this.emailService = new EmailService();
        this.retryDto = new RetryDto();
        this.typeEventOubox = new TypeEvent();
        this.sessionRepository = new SessionRepository();
        this.retryDto.maxRetry = 3;
        this.retryDto.retryTime = 200;
        this.retryDto.maxTimeRetry = 1000;
        this.retryDto.randomState = 1;
        this.jwtSigningKey = Keys.hmacShaKeyFor(resolveJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = Math.max(1, ConfigService.getInt("JWT_ACCESS_TOKEN_MINUTES", 15));
        this.refreshTokenDays = Math.max(1, ConfigService.getInt("JWT_REFRESH_TOKEN_DAYS", 30));
    }

    public SignupResponseDto signup(SignupRequestDto req) {
        SignupResponseDto res = new SignupResponseDto();
        String fullname = value(req.getFullname());
        String email = value(req.getEmail()).toLowerCase();
        String password = value(req.getPassword());
        UserEntity existed = userService.getUserByEmail(email);
        if (existed != null) {
            res.setSuccess(false);
            res.setErrorMessage("Email already exists.");
            return res;
        }

        CreateUserDto dto = new CreateUserDto();
        dto.setName(fullname);
        dto.setEmail(email);
        dto.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
        dto.setHashPassword(hashArgon2(password));

        try {
            UserEntity createdUser = CoreInterface.retryInterface(() -> userService.createUser(dto), retryDto);
            String rawCode = generateVerificationCode();
            String codeHash = hashArgon2(rawCode);

            OutBoxEntity outbox = CoreInterface.retryInterface(
                    () -> outBoxRepository.create(createdUser.getId(), codeHash, typeEventOubox.getSendVerifyEmail()),
                    retryDto
            );

            try {
                CoreInterface.retryInterface(() -> {
                    emailService.sendVerificationCodeEmail(createdUser.getEmail(), createdUser.getName(), rawCode);
                    return true;
                }, retryDto);
                CoreInterface.retryInterface(() -> outBoxRepository.markProcessed(outbox.getId()), retryDto);
                res.setSuccess(true);
                res.setUserName(createdUser.getName());
                res.setUserEmail(createdUser.getEmail());
                res.setUserRole(createdUser.getRole());
                return res;
            } catch (Exception e) {
                try {
                    CoreInterface.retryInterface(() -> outBoxRepository.markFailed(outbox.getId()), retryDto);
                } catch (Exception ignored) {
                }
                res.setSuccess(false);
                res.setErrorMessage("Registration succeeded, but the verification code could not be sent. Please try again later.");
                return res;
            }
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Unable to create the account. Please try again." : e.getMessage();
            res.setSuccess(false);
            res.setErrorMessage(message);
            return res;
        }
    }

    public VerifyEmailCodeResponseDto verifyEmailCode(VerifyEmailCodeRequestDto req) {
        VerifyEmailCodeResponseDto res = new VerifyEmailCodeResponseDto();

        String email = value(req.getEmail()).toLowerCase();
        String code = value(req.getCode());

        UserEntity user = userService.getUserByEmail(email);
        if (user == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account does not exist.");
            return res;
        }

        OutBoxEntity outbox = outBoxRepository.findByUserIdAndType(user.getId(), TypeEvent.SEND_VERIFY_EMAIL);
        if (outbox == null) {
            res.setSuccess(false);
            res.setErrorMessage("The verification code is invalid or has expired.");
            return res;
        }

        if (!verifyArgon2(code, outbox.getCode())) {
            res.setSuccess(false);
            res.setErrorMessage("The verification code is incorrect.");
            return res;
        }

        try {
            boolean activated = CoreInterface.retryInterface(() -> userService.activateUserById(user.getId()), retryDto);
            if (!activated) {
                res.setSuccess(false);
                res.setErrorMessage("Unable to activate the account. Please try again.");
                return res;
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Unable to activate the account. Please try again.");
            return res;
        }

        res.setSuccess(true);
        res.setSuccessMessage("Email verification succeeded. You can now sign in.");
        return res;
    }

    public SigninResponseDto signin(SigninRequestDto req) {
        SigninResponseDto res = new SigninResponseDto();

        String username = value(req.getUsername()).toLowerCase();
        String password = value(req.getPassword());
        String ipAddress = normalizeIp(req.getIpAddress());
        res.setUsername(username);

        UserEntity matched = userService.getUserByEmail(username);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account does not exist.");
            return res;
        }

        if (!"ACTIVE".equalsIgnoreCase(matched.getStatus())) {
            res.setSuccess(false);
            res.setErrorMessage("The account has not been verified by email yet.");
            return res;
        }

        if (!verifyArgon2(password, matched.getHashPassword())) {
            res.setSuccess(false);
            res.setErrorMessage("Incorrect password.");
            return res;
        }

        String refreshToken = generateRefreshToken(matched);
        String hashRefreshToken = sha256(refreshToken);
        SessionEntity session;
        try {
            List<SessionEntity> sameIpSessions = CoreInterface.retryInterface(
                    () -> sessionRepository.findByUserIdAndIp(matched.getId(), ipAddress),
                    retryDto
            );

            boolean firstSeenIp = sameIpSessions == null || sameIpSessions.isEmpty();
            session = firstSeenIp
                    ? CoreInterface.retryInterface(() -> sessionRepository.create(matched.getId(), hashRefreshToken, ipAddress), retryDto)
                    : sameIpSessions.get(0);

            if (!firstSeenIp) {
                final String sessionId = session.getId();
                CoreInterface.retryInterface(() -> sessionRepository.updateHashRefreshToken(sessionId, hashRefreshToken), retryDto);
                session.setHashRefreshToken(hashRefreshToken);
            } else {
                sendLoginAlertAsyncSafe(matched, ipAddress);
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Unable to create the login session. Please try again.");
            return res;
        }

        String accessToken = generateAccessToken(matched, session.getId());

        res.setSuccess(true);
        res.setUserName(matched.getName());
        res.setUserEmail(matched.getEmail());
        res.setUserRole(matched.getRole());
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setSessionId(session.getId());
        return res;
    }

    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto req) {
        ForgotPasswordResponseDto res = new ForgotPasswordResponseDto();
        
        String email = value(req.getEmail()).toLowerCase();
        res.setEmail(email);

        UserEntity matched = userService.getUserByEmail(email);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account does not exist.");
            return res;
        }

        String rawCode = generateVerificationCode();
        String codeHash = hashArgon2(rawCode);

        try {
            OutBoxEntity outbox = CoreInterface.retryInterface(
                    () -> outBoxRepository.create(matched.getId(), codeHash, typeEventOubox.getSendForgotPasswordCode()),
                    retryDto
            );

            try {
                CoreInterface.retryInterface(() -> {
                    emailService.sendForgotPasswordCodeEmail(matched.getEmail(), matched.getName(), rawCode);
                    return true;
                }, retryDto);
                CoreInterface.retryInterface(() -> outBoxRepository.markProcessed(outbox.getId()), retryDto);
                res.setSuccess(true);
                res.setSuccessMessage("A password reset code has been sent to your email.");
                return res;
            } catch (Exception e) {
                try {
                    CoreInterface.retryInterface(() -> outBoxRepository.markFailed(outbox.getId()), retryDto);
                } catch (Exception ignored) {
                }
                res.setSuccess(false);
                res.setErrorMessage("The reset code was created, but the email could not be sent. Please try again later.");
                return res;
            }
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "Unable to create the reset code. Please try again." : e.getMessage();
            res.setSuccess(false);
            res.setErrorMessage(msg);
            return res;
        }
    }

    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto req) {
        ResetPasswordResponseDto res = new ResetPasswordResponseDto();

        String token = value(req.getToken());
        String newPassword = value(req.getNewPassword());
        res.setToken(token);

        String tokenHash = sha256(token);
        PasswordResetTokenEntity validToken = passwordResetTokenRepository.findValidByTokenHash(tokenHash);
        if (validToken == null) {
            res.setSuccess(false);
            res.setErrorMessage("The reset link is invalid or has expired.");
            return res;
        }

        try {
            boolean updated = CoreInterface.retryInterface(
                    () -> userService.updatePasswordById(validToken.getUserId(), hashArgon2(newPassword)),
                    retryDto
            );
            if (!updated) {
                res.setSuccess(false);
                res.setErrorMessage("Unable to update the password. Please try again.");
                return res;
            }

            CoreInterface.retryInterface(() -> passwordResetTokenRepository.markUsed(validToken.getId()), retryDto);
            CoreInterface.retryInterface(() -> passwordResetTokenRepository.invalidateAllByUserId(validToken.getUserId()), retryDto);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Unable to update the password. Please try again.");
            return res;
        }

        UserEntity updatedUser = userService.getUserById(validToken.getUserId());
        res.setSuccess(true);
        if (updatedUser != null) {
            res.setUserName(updatedUser.getName());
            res.setUserEmail(updatedUser.getEmail());
            res.setUserRole(updatedUser.getRole());
        }
        return res;
    }

    public ProfileResponseDto getProfile(ProfileRequestDto req) {
        ProfileResponseDto res = new ProfileResponseDto();

        String authUserEmail = value(req.getAuthUserEmail());
        UserEntity matched = userService.getUserByEmail(authUserEmail);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account information could not be found. Please sign in again.");
            return res;
        }

        res.setSuccess(true);
        res.setProfileUser(matched);
        return res;
    }

    private void sendLoginAlertAsyncSafe(UserEntity user, String ipAddress) {
        try {
            CoreInterface.retryInterface(() -> {
                emailService.sendLoginAlertEmail(user.getEmail(), user.getName(), ipAddress);
                return true;
            }, retryDto);
        } catch (Exception ignored) {
        }
    }

    private String generateAccessToken(UserEntity user, String sessionId) {
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

    private String generateRefreshToken(UserEntity user) {
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

    private String normalizeIp(String ipAddress) {
        String normalized = value(ipAddress);
        return normalized.isBlank() ? "unknown" : normalized;
    }

    private String resolveJwtSecret() {
        String secret = value(ConfigService.getOrDefault("JWT_SECRET", DEFAULT_JWT_SECRET));
        if (secret.length() < 32) {
            secret = (secret + DEFAULT_JWT_SECRET);
        }
        return secret;
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value(raw).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash value", e);
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String hashArgon2(String password) {
        Argon2 argon2 = Argon2Factory.create();
        char[] pwd = password == null ? new char[0] : password.toCharArray();
        try {
            return argon2.hash(3, 65536, 1, pwd);
        } finally {
            argon2.wipeArray(pwd);
        }
    }

    private boolean verifyArgon2(String rawPassword, String hash) {
        Argon2 argon2 = Argon2Factory.create();
        char[] pwd = rawPassword == null ? new char[0] : rawPassword.toCharArray();
        try {
            return hash != null && !hash.isBlank() && argon2.verify(hash, pwd);
        } finally {
            argon2.wipeArray(pwd);
        }
    }

    private String generateVerificationCode() {
        int randomValue = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(randomValue);
    }
}
