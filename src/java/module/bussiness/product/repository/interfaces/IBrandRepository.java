package module.bussiness.product.repository.interfaces;

import entity.BrandEntity;
import java.util.List;

public interface IBrandRepository {
    List<BrandEntity> findAll();
    BrandEntity findById(String id);
    void insert(BrandEntity brand);
    void update(BrandEntity brand);
    void delete(String id);
    void updateStatus(String id, String status);
}
