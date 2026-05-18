package module.core.outbox.repository.interfaces;

import entity.OutBoxEntity;
import java.util.List;

public interface IOutBoxRepository {
    void insert(OutBoxEntity entity);
    List<OutBoxEntity> findPending(int limit);
    OutBoxEntity findValidCode(String userId, String type, String code);
    void markProcessed(String id);
    void markFailed(String id);
    void markUsed(String id);
}
