package module.core.user;

import entity.UserEntity;
import java.util.List;
import module.core.user.repository.interfaces.IUserRepository;
import module.core.user.repository.impl.UserRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;

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

    public List<UserEntity> getAllUsers() {
        return this.userRepository.findAll();
    }

    public List<UserEntity> getAllUsers(int page, int pageSize) {
        return this.userRepository.findAll(page, pageSize);
    }

    public UserEntity getUserById(String id) {
        return this.userRepository.findById(id);
    }

    public UserEntity getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean updatePasswordById(String id, String hashPassword) {
        return this.userRepository.updatePasswordById(id, hashPassword);
    }

    public boolean activateUserById(String id) {
        return this.userRepository.activateById(id);
    }

    public boolean updateUser(String id, UpdateUserDto dto) {
        return this.userRepository.update(id, dto);
    }

    public boolean deleteUser(String id) {
        return this.userRepository.delete(id);
    }
}
