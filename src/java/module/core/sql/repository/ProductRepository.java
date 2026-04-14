package module.core.sql.repository;
import entity.ProductEntity;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.UpdateProduct;
import module.core.sql.ConnecDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    public List<ProductEntity> findAll() throws SQLException{
        String sql = "SLECT id , name , description, brandId, status, userId, category FROM product ODER BY createdAt DESC";
        List<ProductEntity> products = new ArrayList<>();

        try(Connection con = ConnecDb.getConnection();
        PreparedStatement ps = con.PreparedStatement(sql);
        ResultSet rs = ps.executeQuery()){

            while(rs.next()){
                ProductEntity item  = new ProductEntity();
                item.setId(rs.getString("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setBrandId(rs.getString("brandId"));
                item.setStatus(rs.getString("status"));
                item.setUserId(rs.getString("userId"));
                item.setCategory(rs.getString("category"));
                products.add(item);
            }
        }
        return products;
    }
    public ProductEntity findById(String id) throws SQLException{
        String sql = "SELECT id, name, description, brandId, status, userId, category FROM product WHERE id = ?";
        try(Connection con =  ConnecDb.getConnection;
        PreparedStatement ps = con.PreparedStatement(sql)){
            ps.setString(1,id);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()) return null;
                ProductEntity item = new ProductEntity();
                item.setId(rs.getString("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setBrandId(rs.getString("brandId"));
                item.setStatus(rs.getString("status"));
                item.setUserId(rs.getString("userId"));
                item.setCategory(rs.getString("category"));
                return item;
            }
        }
    }
    public boolean create(String id, CreateProduct dto) throws SQLException{
        String sql = "INSERT INTO product (id, name, description, brandId, status, userId,createdAt,updatedAt,category)"+
        "VALUES(?,?,?,?,?,?,CURDATE(),CURDATE(),?)";

        try(Connection con =  ConnecDb.getConnection();
        PreparedStatement ps = con.PreparedStatement(sql)){
            ps.setString(1,id);
            ps.setString(2,dto.getName());
            ps.setString(3,dto.getDescription());   
            ps.setString(4,dto.getBrandId());
            ps.setString(5,dto.getStatus());
            ps.setString(6,dto.getUserId());
            ps.setString(7,dto.getCategory());

            return ps.executeUpdate() > 0;
        }
    }
    public boolean update(String id , UpdateProduct dto) throws SQLException{
        String sql ="UPDATE product SET name = ? , description = ? , brandId = ? , status = ?, category = ?, updateAt = CURDATE() WHERE id = ?";

        try(Connection con = ConnecDb.getConnection();
        PreparedStatement ps = con.PreparedStatement(sql)){

            ps.setString(1,dto.getName());
            ps.setString(2,dto.getDescription());
            ps.setString(3,dto.getBrandId());
            ps.setString(4,dto.getStatus());
            ps.setString(5,dto.getCategory());
            ps.setString(6,id);

            return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(String id) throws SQLException{
        String sql = "DELETE FROM product WHERE id = ? ";
        try(Connection con =  ConnecDb.getConnection();
        PreparedStatement ps = con.PreparedStatement(sql)){
            ps.setString(1,id);
            return ps.executeUpdate() > 0;
        }
    }
}
