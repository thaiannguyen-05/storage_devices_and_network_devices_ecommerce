package module.core.sql.interfaces;

import entity.ProductEntity;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.UpdateProduct;

import java.sql.SQLException;
import java.util.List;

public interface IProductRepository {
    List<ProductEntity> findAll() throws SQLException;
    ProductEntity findById(String id) throws SQLException;
    boolean create(CreateProduct dto) throws SQLException;
    boolean update(String id, UpdateProduct dto) throws SQLException;
    boolean delete(String id) throws SQLException;
}
