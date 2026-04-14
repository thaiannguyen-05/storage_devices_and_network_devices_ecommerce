package module.bussiness.product;

import entity.ProductEntity;
import module.core.sql.repository.BrandRepository;
import module.core.sql.repository.ProductRepository;
import module.core.sql.repository.ProductVariantRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
        this.productVariantRepository = new ProductVariantRepository();
        this.brandRepository = new BrandRepository();
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public ProductVariantRepository getProductVariantRepository() {
        return productVariantRepository;
    }

    public BrandRepository getBrandRepository() {
        return brandRepository;
    }
    public List<ProductEntity> getAllProducts() throws SQLException{
        return productRepository.findAll();
    }
    public ProductEntity getProductById(String id) throws SQLException{
        return productRepository.findById(id);
    }
    public boolean createProduct(CreateProduct dto) throws SQLException{
        String id = UUID.randomUUID().toString();
        return productRepository.create(id, dto);
    }
    public boolean updateProduct(String id, UpdateProduct dto) throws SQLException{
        return productRepository.update(id, dto);
    }
    public boolean deleteProduct(String id) throws SQLException{
        return productRepository.delete(id);
    }   
}
