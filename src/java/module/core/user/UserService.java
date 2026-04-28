package module.core.user;

import entity.UserEntity;
import module.core.user.repository.interfaces.IUserRepository;
import module.core.user.repository.impl.UserRepository;
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
