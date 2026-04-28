package module.core.sql.interfaces;

import entity.UserEntity;
import java.sql.SQLException;
import java.util.List;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.DeleteUserDto;
import module.core.user.dto.FindAllUserDto;
import module.core.user.dto.FindUserByIdDto;
import module.core.user.dto.UpdateUserDto;

public interface IUserRepository {
    UserEntity createUser(CreateUserDto dto);
    UserEntity updateUser(UpdateUserDto dto);
    boolean deleteUser(DeleteUserDto dto);
    List<UserEntity> findAllUser(FindAllUserDto dto);
    UserEntity findById(FindUserByIdDto dto);
}
