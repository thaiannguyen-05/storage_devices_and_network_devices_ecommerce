package module.core.sql.interfaces;

import entity.UserEntity;
import module.core.user.dto.CreateUserDto;

public interface IUserRepository {
    UserEntity createUser(CreateUserDto dto);
    UserEntity findByEmail(String email);
    void updateProfileByEmail(String email, String name);
    void updatePasswordByEmail(String email, String hashPassword);
}
