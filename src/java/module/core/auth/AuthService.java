package module.core.auth;

import module.core.auth.repository.impl.PasswordResetTokenRepository;
import module.core.auth.repository.impl.SessionRepository;
import module.core.auth.service.EmailService;
import module.core.user.repository.impl.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionRepository = new SessionRepository();
        this.passwordResetTokenRepository = new PasswordResetTokenRepository();
        this.emailService = new EmailService();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public SessionRepository getSessionRepository() {
        return sessionRepository;
    }

    public PasswordResetTokenRepository getPasswordResetTokenRepository() {
        return passwordResetTokenRepository;
    }

    public EmailService getEmailService() {
        return emailService;
    }
}
