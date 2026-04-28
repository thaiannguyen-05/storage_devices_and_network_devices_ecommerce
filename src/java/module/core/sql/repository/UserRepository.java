package module.core.sql.repository;

import entity.UserEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IUserRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.DeleteUserDto;
import module.core.user.dto.FindAllUserDto;
import module.core.user.dto.FindUserByIdDto;
import module.core.user.dto.UpdateUserDto;

public class UserRepository implements IUserRepository {

    @Override
    public UserEntity createUser(CreateUserDto dto) {
        String sql = "INSERT INTO user (name, dateOfBirth, hashPassword, email) VALUES (?,?,?,?)";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, dto.getName());
            ps.setObject(2, dto.getDateOfBirth());
            ps.setString(3, dto.getHashPassword());
            ps.setString(4, dto.getEmail());
            ps.executeUpdate();
            return new UserEntity(null, dto.getName(), dto.getDateOfBirth(), dto.getHashPassword(), null, dto.getEmail(), null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user !", e);
        }
    }

    @Override
    public UserEntity updateUser(UpdateUserDto dto) {
        String sql = "UPDATE user set name = ?,dateOfBirth = ?, hashPassword = ?, email = ? WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, dto.getName());
            ps.setObject(2, dto.getDateOfBirth());
            ps.setString(3, dto.getHashPassword());
            ps.setString(4, dto.getEmail());
            ps.setInt(5, dto.getId());
            ps.executeUpdate();
            return new UserEntity(null, dto.getName(), dto.getDateOfBirth(), dto.getHashPassword(), null, dto.getEmail(), null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user !", e);
        }
    }

    @Override
    public boolean deleteUser(DeleteUserDto dto) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getId());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user!", e);
        }
    }

    @Override
    public List<UserEntity> findAllUser(FindAllUserDto dto) {
        String sql = "SELECT * FROM user ORDER BY createdAt DESC LIMIT ? OFFSET ?";
        List<UserEntity> listUsers = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getLimit());
            ps.setInt(2, (dto.getPage() - 1) * dto.getLimit());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserEntity users = new UserEntity();
                    users.setId(rs.getString("id"));
                    users.setName(rs.getString("name"));
                    users.setDateOfBirth(rs.getObject("dateOfBirth", LocalDate.class));
                    users.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
                    users.setHashPassword(rs.getString("hashPassword"));
                    users.setStatus(rs.getString("status"));
                    users.setEmail(rs.getString("email"));
                    users.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
                    listUsers.add(users);
                }
            }
            return listUsers;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all user !", e);
        }
    }

    @Override
    public UserEntity findById(FindUserByIdDto dto) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserEntity user = new UserEntity();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setDateOfBirth(rs.getObject("dateOfBirth", LocalDate.class));
                    user.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
                    user.setHashPassword(rs.getString("hashPassword"));
                    user.setStatus(rs.getString("status"));
                    user.setEmail(rs.getString("email"));
                    user.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
                    return user;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by id !", e);
        }
        return null;
    }
}
