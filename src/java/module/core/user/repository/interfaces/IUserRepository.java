package module.core.user.repository.interfaces;

import entity.UserEntity;
import module.core.user.dto.CreateUserDto;

public interface IUserRepository {
    UserEntity createUser(CreateUserDto dto);
}
