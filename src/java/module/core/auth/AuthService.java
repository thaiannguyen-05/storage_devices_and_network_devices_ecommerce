package module.core.auth;

import common.type.UserPayload;
import entity.OutBoxEntity;
import entity.UserEntity;
import java.time.LocalDate;
import java.util.UUID;
import module.core.auth.dto.LoginRequestDto;
import module.core.auth.dto.RegisterRequestDto;
import module.core.auth.repository.impl.AuthRepository;
import module.core.auth.response_dto.LoginResponseDto;
import module.core.auth.response_dto.RegisterResponseDto;
import module.core.auth.response_dto.VerifyEmailResponseDto;
import module.core.outbox.OutBoxService;
import module.core.outbox.TypeEvent;
import module.core.outbox.repository.impl.OutBoxRepository;
import module.core.sql.JdbcHelper;

public class AuthService {
    private final AuthRepository authRepository = new AuthRepository();
    private final OutBoxRepository outBoxRepository = new OutBoxRepository();
    private final AuthTokenService tokenService = new AuthTokenService();
    private final PasswordService passwordService = new PasswordService();
    private final OutBoxService outBoxService = new OutBoxService();

    public LoginResponseDto login(LoginRequestDto dto, String ip) {
        LoginResponseDto response = new LoginResponseDto();
        UserEntity user = authRepository.findByEmail(trim(dto.getEmail()));
        if (user == null || !passwordService.matches(dto.getPassword(), user.getHashPassword())) {
            fail(response, "Email or password is invalid");
            return response;
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            fail(response, "Account is not active");
            return response;
        }
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        authRepository.saveSession(sessionId, tokenService.hashRefreshToken(refreshToken), user.getId(), ip);
        response.setSuccess(true);
        response.setSuccessMessage("Login successful");
        response.setSessionId(sessionId);
        response.setUser(new UserPayload(user.getId(), user.getEmail(), user.getRole(), user.getName()));
        return response;
    }

    public RegisterResponseDto register(RegisterRequestDto dto) {
        RegisterResponseDto response = new RegisterResponseDto();
        if (isBlank(dto.getEmail()) || isBlank(dto.getName()) || isBlank(dto.getPassword())) {
            fail(response, "Name, email and password are required");
            return response;
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            fail(response, "Password confirmation does not match");
            return response;
        }
        UserEntity existingUser = authRepository.findByEmail(trim(dto.getEmail()));
        if (existingUser != null) {
            if ("PENDING".equals(existingUser.getStatus())) {
                publishVerification(existingUser.getId(), response);
                response.setSuccessMessage("Verification email has been sent again");
                return response;
            }
            fail(response, "Email already exists");
            return response;
        }
        String userId = UUID.randomUUID().toString();
        LocalDate dob = isBlank(dto.getDateOfBirth()) ? LocalDate.of(2000, 1, 1) : LocalDate.parse(dto.getDateOfBirth());
        JdbcHelper.executeUpdate("INSERT INTO `User` (id, name, dateOfBirth, hashPassword, status, role, email) VALUES (?, ?, ?, ?, 'PENDING', 'USER', ?)",
                userId, trim(dto.getName()), dob, passwordService.hash(dto.getPassword()), trim(dto.getEmail()));
        authRepository.createCartForUser(userId);
        publishVerification(userId, response);
        response.setSuccessMessage("Register successful");
        return response;
    }

    private void publishVerification(String userId, RegisterResponseDto response) {
        String code = UUID.randomUUID().toString().substring(0, 8);
        outBoxService.publishEvent(code, TypeEvent.USER_REGISTERED, userId);
        response.setSuccess(true);
        response.setUserId(userId);
        response.setEmailVerificationCode(code);
    }

    public VerifyEmailResponseDto verifyEmail(String code, String userId) {
        VerifyEmailResponseDto response = new VerifyEmailResponseDto();
        if (isBlank(code) || isBlank(userId)) {
            fail(response, "Verification code is invalid or expired");
            return response;
        }
        OutBoxEntity entity = outBoxRepository.findValidCode(userId, TypeEvent.USER_REGISTERED.name(), code);
        if (entity == null) {
            fail(response, "Verification code is invalid or expired");
            return response;
        }
        outBoxRepository.markUsed(entity.getId());
        JdbcHelper.executeUpdate("UPDATE `User` SET status = 'ACTIVE' WHERE id = ?", userId);
        response.setSuccess(true);
        response.setSuccessMessage("Email verified");
        return response;
    }

    public module.core.common.BaseResponse requestPasswordReset(String email) {
        module.core.common.BaseResponse response = new module.core.common.BaseResponse();
        if (isBlank(email)) {
            response.setSuccess(true);
            response.setSuccessMessage("If the email exists, a reset code has been sent.");
            return response;
        }
        UserEntity user = authRepository.findByEmail(trim(email));
        if (user != null && "ACTIVE".equals(user.getStatus())) {
            String code = UUID.randomUUID().toString().substring(0, 8);
            outBoxService.publishEvent(code, TypeEvent.PASSWORD_RESET_REQUESTED, user.getId());
        }
        response.setSuccess(true);
        response.setSuccessMessage("If the email exists, a reset code has been sent.");
        return response;
    }

    public module.core.common.BaseResponse resetPassword(String email, String code, String newPassword, String confirmPassword) {
        module.core.common.BaseResponse response = new module.core.common.BaseResponse();
        if (isBlank(email) || isBlank(code) || isBlank(newPassword)) {
            fail(response, "Email, code and new password are required");
            return response;
        }
        if (newPassword.length() < 8) {
            fail(response, "Password must be at least 8 characters");
            return response;
        }
        if (!newPassword.equals(confirmPassword)) {
            fail(response, "Password confirmation does not match");
            return response;
        }

        UserEntity user = authRepository.findByEmail(trim(email));
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            fail(response, "Reset code is invalid or expired");
            return response;
        }

        OutBoxEntity entity = outBoxRepository.findValidCode(user.getId(), TypeEvent.PASSWORD_RESET_REQUESTED.name(), code);
        if (entity == null) {
            fail(response, "Reset code is invalid or expired");
            return response;
        }

        authRepository.updatePassword(user.getId(), passwordService.hash(newPassword));
        outBoxRepository.markUsed(entity.getId());
        response.setSuccess(true);
        response.setSuccessMessage("Password has been reset. Please login again.");
        return response;
    }

    public void logout(String sessionId) {
        if (!isBlank(sessionId)) {
            authRepository.deleteSession(sessionId);
        }
    }

    private void fail(module.core.common.BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
