package module.core.shared.repository.interfaces;

import entity.OutBoxEntity;

public interface IOutBoxRepository {
    OutBoxEntity create(String payload);
    boolean markProcessed(String id);
    boolean markFailed(String id);
}
