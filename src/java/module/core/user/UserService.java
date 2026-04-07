package module.core.user;

import entity.UserEntity;
import module.core.sql.interfaces.IUserRepository;
import module.core.sql.repository.UserRepository;
import module.core.user.dto.CreateUserDto;

public class UserService {

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserService() {
        this(new UserRepository());
    }
    
    public UserEntity createUser(CreateUserDto dto) {
        return this.userRepository.createUser(dto);
    }
}
