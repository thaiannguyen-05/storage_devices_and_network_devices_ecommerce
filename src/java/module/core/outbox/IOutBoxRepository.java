package module.core.outbox;

import entity.OutBoxEntity;

public interface IOutBoxRepository {
    OutBoxEntity create(String userId, String code, String type);
    boolean markProcessed(String id);

    boolean markFailed(String id);
    OutBoxEntity findByUserIdAndType(String userId, String type);
}
