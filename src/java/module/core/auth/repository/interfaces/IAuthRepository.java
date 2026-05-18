package module.core.auth.repository.interfaces;

import entity.UserEntity;

public interface IAuthRepository {
    UserEntity findByEmail(String email);
    UserEntity findById(String id);
    void saveSession(String id, String refreshTokenHash, String userId, String ip);
    void deleteSession(String sessionId);
    void updatePassword(String userId, String passwordHash);
    void createCartForUser(String userId);
}
