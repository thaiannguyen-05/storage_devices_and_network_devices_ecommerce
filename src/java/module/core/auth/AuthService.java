package module.core.auth;

import module.core.auth.repository.impl.SessionRepository;
import module.core.user.repository.impl.UserRepository;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.sessionRepository = new SessionRepository();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public SessionRepository getSessionRepository() {
        return sessionRepository;
    }
}
