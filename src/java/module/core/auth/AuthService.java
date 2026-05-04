package module.core.auth;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import entity.OutBoxEntity;
import entity.SessionEntity;
import entity.UserEntity;
import io.jsonwebtoken.Claims;
import module.bussiness.notification.EmailService;
import module.core.auth.dto.ForgotPasswordRequestDto;
import module.core.auth.dto.ProfileRequestDto;
import module.core.auth.dto.RefreshTokenRequestDto;
import module.core.auth.dto.ResetPasswordRequestDto;
import module.core.auth.dto.SigninRequestDto;
import module.core.auth.dto.SignupRequestDto;
import module.core.auth.dto.VerifyEmailCodeRequestDto;
import module.core.auth.repository.impl.SessionRepository;
import module.core.auth.response_dto.ForgotPasswordResponseDto;
import module.core.auth.response_dto.ProfileResponseDto;
import module.core.auth.response_dto.RefreshTokenResponseDto;
import module.core.auth.response_dto.ResetPasswordResponseDto;
import module.core.auth.response_dto.SigninResponseDto;
import module.core.auth.response_dto.SignupResponseDto;
import module.core.auth.response_dto.VerifyEmailCodeResponseDto;
import module.core.coreInterface.CoreInterface;
import module.core.coreInterface.RetryDto;
import module.core.outbox.OutBoxRepository;
import module.core.outbox.TypeEvent;
import module.core.user.UserService;
import module.core.user.dto.CreateUserDto;

public class AuthService {
    private final UserService userService;
    private final OutBoxRepository outBoxRepository;
    private final EmailService emailService;
    private final RetryDto retryDto;
    private final TypeEvent typeEventOubox;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;
    private final AuthConfig authConfig;
    private final SecureRandom secureRandom;

    public AuthService() {
        this.userService = new UserService();
        this.outBoxRepository = new OutBoxRepository();
        this.emailService = new EmailService();
        this.authConfig = new AuthConfig();
        this.retryDto = authConfig.createRetryDto();
        this.typeEventOubox = new TypeEvent();
        this.sessionRepository = new SessionRepository();
        this.tokenService = new TokenService();
        this.secureRandom = new SecureRandom();
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

        String refreshToken = tokenService.generateRefreshToken(matched);
        String hashRefreshToken = hashArgon2(refreshToken);
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

        String accessToken = tokenService.generateAccessToken(matched, session.getId());

        res.setSuccess(true);
        res.setUserName(matched.getName());
        res.setUserEmail(matched.getEmail());
        res.setUserRole(matched.getRole());
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setSessionId(session.getId());
        return res;
    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto req) {
        RefreshTokenResponseDto res = new RefreshTokenResponseDto();

        String accessToken = value(req.getAccessToken());
        String refreshToken = value(req.getRefreshToken());
        Claims accessClaims;
        Claims refreshClaims;

        try {
            accessClaims = tokenService.parseAccessToken(accessToken);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("The current access token is invalid.");
            return res;
        }

        String email = value(accessClaims.get("email", String.class)).toLowerCase();
        String sessionId = value(accessClaims.get("sessionId", String.class));
        String accessUserId = value(accessClaims.getSubject());

        UserEntity matched = userService.getUserByEmail(email);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account does not exist.");
            return res;
        }

        if (!matched.getId().equals(accessUserId)) {
            res.setSuccess(false);
            res.setErrorMessage("The token does not match the account.");
            return res;
        }

        SessionEntity session = sessionRepository.findById(sessionId);
        if (session == null || !matched.getId().equals(session.getUserId())) {
            res.setSuccess(false);
            res.setErrorMessage("The login session does not exist.");
            return res;
        }

        if (!verifyArgon2(refreshToken, session.getHashRefreshToken())) {
            res.setSuccess(false);
            res.setErrorMessage("The refresh token is invalid.");
            return res;
        }

        try {
            refreshClaims = tokenService.parseRefreshToken(refreshToken);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("The refresh token is invalid or expired.");
            return res;
        }

        String refreshEmail = value(refreshClaims.get("email", String.class)).toLowerCase();
        String refreshUserId = value(refreshClaims.getSubject());
        if (!matched.getEmail().equalsIgnoreCase(refreshEmail) || !matched.getId().equals(refreshUserId)) {
            res.setSuccess(false);
            res.setErrorMessage("The refresh token does not match the account.");
            return res;
        }

        String newAccessToken = tokenService.generateAccessToken(matched, session.getId());
        String newRefreshToken = tokenService.generateRefreshToken(matched);
        String newHashRefreshToken = hashArgon2(newRefreshToken);

        try {
            boolean updated = CoreInterface.retryInterface(
                    () -> sessionRepository.updateHashRefreshToken(session.getId(), newHashRefreshToken),
                    retryDto
            );
            if (!updated) {
                res.setSuccess(false);
                res.setErrorMessage("Unable to update the login session.");
                return res;
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Unable to update the login session.");
            return res;
        }

        res.setSuccess(true);
        res.setAccessToken(newAccessToken);
        res.setRefreshToken(newRefreshToken);
        res.setSessionId(session.getId());
        res.setUserName(matched.getName());
        res.setUserEmail(matched.getEmail());
        res.setUserRole(matched.getRole());
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

        String email = value(req.getEmail()).toLowerCase();
        String code = value(req.getCode());
        String newPassword = value(req.getNewPassword());
        String confirmNewPassword = value(req.getConfirmNewPassword());
        res.setToken(code);

        UserEntity matched = userService.getUserByEmail(email);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Account does not exist.");
            return res;
        }

        if (newPassword.isBlank() || !newPassword.equals(confirmNewPassword)) {
            res.setSuccess(false);
            res.setErrorMessage("The password confirmation does not match.");
            return res;
        }

        OutBoxEntity outbox = outBoxRepository.findByUserIdAndType(matched.getId(), typeEventOubox.getSendForgotPasswordCode());
        if (outbox == null) {
            res.setSuccess(false);
            res.setErrorMessage("No reset code was found for this account.");
            return res;
        }

        if (!"PENDING".equalsIgnoreCase(outbox.getStatus()) && !"PROCESSED".equalsIgnoreCase(outbox.getStatus())) {
            res.setSuccess(false);
            res.setErrorMessage("The reset code is not available.");
            return res;
        }

        if (!verifyArgon2(code, outbox.getCode())) {
            res.setSuccess(false);
            res.setErrorMessage("The reset code is incorrect.");
            return res;
        }

        try {
            boolean updated = CoreInterface.retryInterface(
                    () -> userService.updatePasswordById(matched.getId(), hashArgon2(newPassword)),
                    retryDto
            );
            if (!updated) {
                res.setSuccess(false);
                res.setErrorMessage("Unable to update the password. Please try again.");
                return res;
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Unable to update the password. Please try again.");
            return res;
        }

        UserEntity updatedUser = userService.getUserById(matched.getId());
        res.setSuccess(true);
        if (updatedUser != null) {
            res.setUserName(updatedUser.getName());
            res.setUserEmail(updatedUser.getEmail());
            res.setUserRole(updatedUser.getRole());
        }
        return res;
    }

    private void sendLoginAlertAsyncSafe(UserEntity user, String ipAddress) {
        try {
            OutBoxEntity outbox = CoreInterface.retryInterface(
                    () -> outBoxRepository.create(user.getId(), value(ipAddress), typeEventOubox.getSendLoginAlertEmail()),
                    retryDto
            );

            try {
                CoreInterface.retryInterface(() -> {
                    emailService.sendLoginAlertEmail(user.getEmail(), user.getName(), ipAddress);
                    return true;
                }, retryDto);
                CoreInterface.retryInterface(() -> outBoxRepository.markProcessed(outbox.getId()), retryDto);
            } catch (Exception e) {
                try {
                    CoreInterface.retryInterface(() -> outBoxRepository.markFailed(outbox.getId()), retryDto);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String normalizeIp(String ipAddress) {
        String normalized = value(ipAddress);
        return normalized.isBlank() ? "unknown" : normalized;
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String hashArgon2(String password) {
        Argon2 argon2 = Argon2Factory.create();
        char[] pwd = password == null ? new char[0] : password.toCharArray();
        try {
            return argon2.hash(
                    authConfig.getArgon2Iterations(),
                    authConfig.getArgon2MemoryKiB(),
                    authConfig.getArgon2Parallelism(),
                    pwd
            );
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
        int digits = authConfig.getVerificationCodeLength();
        int minValue = 1;
        int maxValue = 1;
        for (int i = 1; i < digits; i++) {
            minValue *= 10;
        }
        for (int i = 0; i < digits; i++) {
            maxValue *= 10;
        }
        int randomValue = minValue + secureRandom.nextInt(maxValue - minValue);
        return String.valueOf(randomValue);
    }
}
