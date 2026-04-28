package module.core.user;

import entity.UserEntity;
import java.util.List;
import module.core.sql.interfaces.IUserRepository;
import module.core.sql.repository.UserRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.FindAllUserDto;
import module.core.user.dto.FindUserByIdDto;

public class UserService {

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserService() {
        this(new UserRepository() {});
    }
    
    public UserEntity createUser(CreateUserDto dto) {
        return this.userRepository.createUser(dto);
    }

    public List<UserEntity> findAllUser(FindAllUserDto dto) {
        return this.userRepository.findAllUser(dto);
    }

    public UserEntity findById(FindUserByIdDto dto) {
        return this.userRepository.findById(dto);
    }
}
