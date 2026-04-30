package module.bussiness.product.repository.interfaces;

import entity.ProductEntity;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.UpdateProduct;

import java.util.List;

public interface IProductRepository {
    List<ProductEntity> findAll();
    ProductEntity findById(String id);
    boolean create(CreateProduct dto);
    boolean update(String id, UpdateProduct dto);
    boolean delete(String id);
}
