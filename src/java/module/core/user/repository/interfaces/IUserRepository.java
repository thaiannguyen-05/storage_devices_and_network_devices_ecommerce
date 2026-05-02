package module.core.user.repository.interfaces;

import entity.UserEntity;
import java.util.List;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;

public interface IUserRepository {
    UserEntity createUser(CreateUserDto dto);
    List<UserEntity> findAll();
    List<UserEntity> findAll(int page, int pageSize);
    UserEntity findById(String id);
    boolean update(String id, UpdateUserDto dto);
    boolean updatePasswordById(String id, String hashPassword);
    boolean activateById(String id);
    boolean delete(String id);
}
