package module.core.auth;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import entity.EmailVerificationCodeEntity;
import entity.OutBoxEntity;
import entity.PasswordResetTokenEntity;
import entity.UserEntity;
import module.bussiness.notification.EmailService;
import module.core.auth.dto.ForgotPasswordRequestDto;
import module.core.auth.dto.ProfileRequestDto;
import module.core.auth.dto.ResetPasswordRequestDto;
import module.core.auth.dto.SigninRequestDto;
import module.core.auth.dto.SignupRequestDto;
import module.core.auth.dto.VerifyEmailCodeRequestDto;
import module.core.auth.response_dto.ForgotPasswordResponseDto;
import module.core.auth.response_dto.ProfileResponseDto;
import module.core.auth.response_dto.ResetPasswordResponseDto;
import module.core.auth.response_dto.SigninResponseDto;
import module.core.auth.response_dto.SignupResponseDto;
import module.core.auth.response_dto.VerifyEmailCodeResponseDto;
import module.core.config.ConfigService;
import module.core.coreInterface.CoreInterface;
import module.core.coreInterface.RetryDto;
import module.core.shared.repository.impl.OutBoxRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.repository.impl.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private final OutBoxRepository outBoxRepository;
    private final EmailService emailService;
    private final RetryDto retryDto;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.outBoxRepository = new OutBoxRepository();
        this.emailService = new EmailService();
        this.retryDto = new RetryDto();
        this.retryDto.maxRetry = 3;
        this.retryDto.retryTime = 200;
        this.retryDto.maxTimeRetry = 1000;
        this.retryDto.randomState = 1;
    }

    public SignupResponseDto signup(SignupRequestDto req) {
        SignupResponseDto res = new SignupResponseDto();
        String fullname = value(req.getFullname());
        String email = value(req.getEmail()).toLowerCase();
        String password = value(req.getPassword());
        UserEntity existed = findUserByEmail(email);
        if (existed != null) {
            res.setSuccess(false);
            res.setErrorMessage("Email đã tồn tại.");
            return res;
        }

        CreateUserDto dto = new CreateUserDto();
        dto.setName(fullname);
        dto.setEmail(email);
        dto.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
        dto.setHashPassword(hashArgon2(password));

        try {
            UserEntity createdUser = CoreInterface.retryInterface(() -> userRepository.createUser(dto), retryDto);
            String rawCode = generateVerificationCode();
            String codeHash = hashArgon2(rawCode);

            String payload = buildVerifyEmailPayload(createdUser, codeHash);
            OutBoxEntity outbox = CoreInterface.retryInterface(() -> outBoxRepository.create(payload), retryDto);

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
                res.setErrorMessage("Đăng ký thành công nhưng chưa gửi được mã xác thực. Vui lòng thử lại sau.");
                return res;
            }
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Không thể tạo tài khoản. Vui lòng thử lại." : e.getMessage();
            res.setSuccess(false);
            res.setErrorMessage(message);
            return res;
        }
    }

    public VerifyEmailCodeResponseDto verifyEmailCode(VerifyEmailCodeRequestDto req) {
        VerifyEmailCodeResponseDto res = new VerifyEmailCodeResponseDto();

        String email = value(req.getEmail()).toLowerCase();
        String code = value(req.getCode());

        UserEntity user = findUserByEmail(email);
        if (user == null) {
            res.setSuccess(false);
            res.setErrorMessage("Tài khoản không tồn tại.");
            return res;
        }

        EmailVerificationCodeEntity verificationCode = emailVerificationCodeRepository.findValidByUserId(user.getId());
        if (verificationCode == null) {
            res.setSuccess(false);
            res.setErrorMessage("Mã xác thực không hợp lệ hoặc đã hết hạn.");
            return res;
        }

        if (!sha256(code).equals(verificationCode.getCodeHash())) {
            res.setSuccess(false);
            res.setErrorMessage("Mã xác thực không đúng.");
            return res;
        }

        try {
            CoreInterface.retryInterface(() -> emailVerificationCodeRepository.markUsed(verificationCode.getId()), retryDto);
            boolean activated = CoreInterface.retryInterface(() -> userRepository.activateById(user.getId()), retryDto);
            if (!activated) {
                res.setSuccess(false);
                res.setErrorMessage("Không thể kích hoạt tài khoản. Vui lòng thử lại.");
                return res;
            }
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Không thể kích hoạt tài khoản. Vui lòng thử lại.");
            return res;
        }


        res.setSuccess(true);
        res.setSuccessMessage("Xác thực email thành công. Bạn có thể đăng nhập.");
        return res;
    }

    public SigninResponseDto signin(SigninRequestDto req) {
        SigninResponseDto res = new SigninResponseDto();

        String username = value(req.getUsername()).toLowerCase();
        String password = value(req.getPassword());
        res.setUsername(username);

        UserEntity matched = findUserByEmail(username);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Tài khoản không tồn tại.");
            return res;
        }

        if (!"ACTIVE".equalsIgnoreCase(matched.getStatus())) {
            res.setSuccess(false);
            res.setErrorMessage("Tài khoản chưa xác thực email.");
            return res;
        }

        if (!verifyPassword(password, matched.gethashArgon2())) {
            res.setSuccess(false);
            res.setErrorMessage("Mật khẩu không đúng.");
            return res;
        }

        res.setSuccess(true);
        res.setUserName(matched.getName());
        res.setUserEmail(matched.getEmail());
        res.setUserRole(matched.getRole());
        return res;
    }

    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto req) {
        ForgotPasswordResponseDto res = new ForgotPasswordResponseDto();

        String email = value(req.getEmail()).toLowerCase();
        String fallbackBaseUrl = value(req.getBaseUrl());
        res.setEmail(email);

        UserEntity matched = findUserByEmail(email);
        if (matched != null) {
            try {
                CoreInterface.retryInterface(() -> passwordResetTokenRepository.invalidateAllByUserId(matched.getId()), retryDto);
                String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "");
                String tokenHash = sha256(rawToken);
                CoreInterface.retryInterface(() -> passwordResetTokenRepository.create(matched.getId(), tokenHash, 15), retryDto);

                String baseUrl = ConfigService.getOrDefault("APP_BASE_URL", fallbackBaseUrl);
                String resetLink = baseUrl + "/auth?action=resetPassword&token=" + rawToken;
                CoreInterface.retryInterface(() -> {
                    emailService.sendPasswordResetEmail(matched.getEmail(), resetLink);
                    return true;
                }, retryDto);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Không thể gửi email lúc này." : e.getMessage();
                res.setSuccess(false);
                res.setErrorMessage(msg);
                return res;
            }
        }

        res.setSuccess(true);
        res.setSuccessMessage("Nếu email tồn tại, hệ thống đã gửi link đặt lại mật khẩu (hiệu lực 15 phút).");
        return res;
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
            res.setErrorMessage("Link không hợp lệ hoặc đã hết hạn.");
            return res;
        }

        try {
            boolean updated = CoreInterface.retryInterface(() -> userRepository.updatePasswordById(validToken.getUserId(), hashArgon2(newPassword)), retryDto);
            if (!updated) {
                res.setSuccess(false);
                res.setErrorMessage("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
                return res;
            }

            CoreInterface.retryInterface(() -> passwordResetTokenRepository.markUsed(validToken.getId()), retryDto);
            CoreInterface.retryInterface(() -> passwordResetTokenRepository.invalidateAllByUserId(validToken.getUserId()), retryDto);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            return res;
        }

        UserEntity updatedUser = userRepository.findById(validToken.getUserId());
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
        UserEntity matched = findUserByEmail(authUserEmail);
        if (matched == null) {
            res.setSuccess(false);
            res.setErrorMessage("Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
            return res;
        }

        res.setSuccess(true);
        res.setProfileUser(matched);
        return res;
    }

    private UserEntity findUserByEmail(String email) {
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            if (email.equalsIgnoreCase(user.getEmail())) {
                return user;
            }
        }
        return null;
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

    private boolean verifyPassword(String rawPassword, String hash) {
        Argon2 argon2 = Argon2Factory.create();
        char[] pwd = rawPassword == null ? new char[0] : rawPassword.toCharArray();
        try {
            return argon2.verify(hash, pwd);
        } finally {
            argon2.wipeArray(pwd);
        }
    }

    private String generateVerificationCode() {
        int value = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(value);
    }

    private String buildVerifyEmailPayload(UserEntity user, String code) {
        String safeName = escapeJson(value(user.getName()));
        String safeEmail = escapeJson(value(user.getEmail()));
        String safeCode = escapeJson(value(code));
        return "{"
                + "\"eventType\":\"SEND_VERIFY_EMAIL\","
                + "\"userId\":\"" + user.getId() + "\","
                + "\"email\":\"" + safeEmail + "\","
                + "\"name\":\"" + safeName + "\","
                + "\"code\":\"" + safeCode + "\""
                + "}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
