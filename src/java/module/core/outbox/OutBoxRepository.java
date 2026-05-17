package module.core.outbox;

import entity.OutBoxEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import module.core.sql.ConnecDb;

public class OutBoxRepository implements IOutBoxRepository {

    @Override
    public OutBoxEntity findByUserIdAndType(String userId, String type) {
        String sql = "SELECT id, code, status, \"createdAt\", \"updatedAt\", type, \"userId\" "
                + "FROM \"OutBox\" "
                + "WHERE \"userId\" = ? AND type = ? AND status IN ('PENDING', 'PROCESSED') "
                + "ORDER BY \"createdAt\" DESC LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find outbox event", e);
        }
    }

    @Override
    public OutBoxEntity create(String userId, String code, String type) {
        String sql = "INSERT INTO \"OutBox\" (id, code, status, \"createdAt\", \"updatedAt\", type, \"userId\") VALUES (?, ?, 'PENDING', NOW(), NOW(), ?, ?)";
        String id = UUID.randomUUID().toString();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, code);
            ps.setString(3, type);
            ps.setString(4, userId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create outbox event");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create outbox event", e);
        }
        return new OutBoxEntity(id, code, "PENDING", LocalDateTime.now(), LocalDateTime.now(), type, userId);
    }

    @Override
    public boolean markProcessed(String id) {
        String sql = "UPDATE \"OutBox\" SET status = 'PROCESSED', \"updatedAt\" = NOW() WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark outbox event processed", e);
        }
    }

    @Override
    public boolean markFailed(String id) {
        String sql = "UPDATE \"OutBox\" SET status = 'FAILED', \"updatedAt\" = NOW() WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark outbox event failed", e);
        }
    }

    private OutBoxEntity map(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        Timestamp updatedAtTs = rs.getTimestamp("updatedAt");
        LocalDateTime createdAt = createdAtTs == null ? null : createdAtTs.toLocalDateTime();
        LocalDateTime updatedAt = updatedAtTs == null ? null : updatedAtTs.toLocalDateTime();
        return new OutBoxEntity(
                rs.getString("id"),
                rs.getString("code"),
                rs.getString("status"),
                createdAt,
                updatedAt,
                rs.getString("type"),
                rs.getString("userId")
        );
    }
}
