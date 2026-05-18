package module.core.user.repository.interfaces;

import entity.UserEntity;
import java.util.List;

public interface IUserRepository {
    void insert(UserEntity user);
    void createCartForUser(String userId);
    UserEntity findById(String id);
    UserEntity findByEmail(String email);
    List<UserEntity> findAll(int offset, int limit);
    int countAll();
    void update(UserEntity user);
    void updateStatus(String id, String status);
    void delete(String id);
}
