package module.core.outbox;

import entity.OutBoxEntity;
import entity.UserEntity;
import common.logger.AppLogger;
import java.time.LocalDateTime;
import java.util.UUID;
import module.core.auth.repository.impl.AuthRepository;
import module.core.config.ConfigService;
import module.core.mail.EmailService;
import module.core.outbox.repository.impl.OutBoxRepository;

public class OutBoxService {
    private static final AppLogger LOGGER = AppLogger.of(OutBoxService.class);

    private final OutBoxRepository outBoxRepository = new OutBoxRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final EmailService emailService = new EmailService();
    private final ConfigService config = ConfigService.getInstance();

    public void publishEvent(String code, TypeEvent type, String userId) {
        OutBoxEntity entity = new OutBoxEntity(UUID.randomUUID().toString(), code, "PENDING",
                LocalDateTime.now(), LocalDateTime.now(), type.name(), userId);
        outBoxRepository.insert(entity);
        processEvent(entity);
    }

    public void processPending() {
        outBoxRepository.findPending(50).forEach(event -> processEvent(event));
    }

    private void processEvent(OutBoxEntity event) {
        try {
            TypeEvent type = TypeEvent.valueOf(event.getType());
            if (type == TypeEvent.USER_REGISTERED) {
                sendVerificationEmail(event);
            } else if (type == TypeEvent.PASSWORD_RESET_REQUESTED) {
                sendPasswordResetEmail(event);
            } else {
                outBoxRepository.markProcessed(event.getId());
                return;
            }
            outBoxRepository.markProcessed(event.getId());
        } catch (Exception ex) {
            outBoxRepository.markFailed(event.getId());
            LOGGER.error("Failed to process outbox event " + event.getId() + " type=" + event.getType()
                    + " userId=" + event.getUserId(), ex);
        }
    }

    private void sendVerificationEmail(OutBoxEntity event) {
        UserEntity user = findUser(event);
        String verifyUrl = appBaseUrl() + "/auth?action=verify-email&userId=" + user.getId() + "&code=" + event.getCode();
        String body = "Hello " + user.getName() + ",\n\n"
                + "Your verification code is: " + event.getCode() + "\n\n"
                + "Or open this link to verify your account:\n" + verifyUrl + "\n\n"
                + "If you did not register this account, you can ignore this email.";
        emailService.send(user.getEmail(), "Verify your LinhNamStore account", body);
    }

    private void sendPasswordResetEmail(OutBoxEntity event) {
        UserEntity user = findUser(event);
        String body = "Hello " + user.getName() + ",\n\n"
                + "Your password reset code is: " + event.getCode() + "\n\n"
                + "Enter this code on the forgot password page to set a new password.\n\n"
                + "If you did not request a password reset, you can ignore this email.";
        emailService.send(user.getEmail(), "LinhNamStore password reset code", body);
    }

    private UserEntity findUser(OutBoxEntity event) {
        UserEntity user = authRepository.findById(event.getUserId());
        if (user == null) {
            throw new IllegalStateException("Outbox user not found: " + event.getUserId());
        }
        return user;
    }

    private String appBaseUrl() {
        String baseUrl = config.get("APP_BASE_URL", "http://localhost:8080/WebApplication3");
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
