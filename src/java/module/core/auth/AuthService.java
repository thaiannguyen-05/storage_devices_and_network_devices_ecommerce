package module.core.auth;

import module.core.sql.repository.PasswordResetTokenRepository;
import module.core.sql.repository.SessionRepository;
import module.core.sql.repository.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionRepository = new SessionRepository();
        this.passwordResetTokenRepository = new PasswordResetTokenRepository();
        this.mailService = new MailService();
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

    public MailService getMailService() {
        return mailService;
    }
}
