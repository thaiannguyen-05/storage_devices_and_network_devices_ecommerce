package module.core.auth.repository.interfaces;

import entity.SessionEntity;
import java.util.List;

public interface ISessionRepository {
    SessionEntity create(String userId, String hashRefreshToken, String ip);
    List<SessionEntity> findAll();
    SessionEntity findById(String id);
    SessionEntity findByHashRefreshToken(String hashRefreshToken);
    List<SessionEntity> findByUserIdAndIp(String userId, String ip);
    boolean updateHashRefreshToken(String id, String hashRefreshToken);
    boolean delete(String id);

    boolean deleteByUserId(String userId);
    
}
