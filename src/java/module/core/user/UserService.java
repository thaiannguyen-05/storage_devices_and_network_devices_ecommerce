package module.core.user;

import module.core.sql.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
