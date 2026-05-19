package module.core.user.repository.interfaces;

import entity.UserEntity;
import java.util.List;
import java.util.Map;

public interface IUserRepository {
    void insert(UserEntity user);
    void createCartForUser(String userId);
    UserEntity findById(String id);
    UserEntity findByEmail(String email);
    List<UserEntity> findAll(int offset, int limit);
    List<UserEntity> findFiltered(String role, String status, String keyword, int offset, int limit);
    int countAll();
    int countFiltered(String role, String status, String keyword);
    void update(UserEntity user);
    void updateRole(String id, String role);
    void updatePassword(String id, String hashPassword);
    void updateStatus(String id, String status);
    Map<String, Integer> countByRole();
    Map<String, Integer> countByStatus();
    void delete(String id);
}
