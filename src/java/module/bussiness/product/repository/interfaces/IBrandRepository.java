package module.bussiness.product.repository.interfaces;

import entity.BrandEntity;

import java.util.List;

public interface IBrandRepository {
    List<BrandEntity> findAll();
    BrandEntity findById(String id);
}
