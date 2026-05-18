package module.bussiness.product.repository.impl;

import entity.BrandEntity;
import java.util.List;
import module.bussiness.product.repository.interfaces.IBrandRepository;
import module.core.sql.JdbcHelper;

public class BrandRepository implements IBrandRepository {
    @Override
    public List<BrandEntity> findAll() {
        return JdbcHelper.executeQuery("SELECT * FROM Brand ORDER BY name ASC",
                rs -> new BrandEntity(rs.getString("id"), rs.getString("name"), rs.getString("userId"),
                        rs.getString("description"), rs.getString("status"), rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getTimestamp("updatedAt").toLocalDateTime()));
    }
}
