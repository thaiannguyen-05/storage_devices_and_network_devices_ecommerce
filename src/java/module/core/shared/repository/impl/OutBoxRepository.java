package module.core.shared.repository.impl;

import entity.OutBoxEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import module.core.shared.repository.interfaces.IOutBoxRepository;
import module.core.sql.ConnecDb;

public class OutBoxRepository implements IOutBoxRepository {

    @Override
    public OutBoxEntity create(String payload) {
        String sql = "INSERT INTO `OutBox` (`id`, `payload`, `status`, `createdAt`, `updatedAt`) VALUES (?, CAST(? AS JSON), 'PENDING', NOW(), NOW())";
        String id = UUID.randomUUID().toString();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, payload);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create outbox event");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create outbox event", e);
        }

        String fetchSql = "SELECT `id`, `payload`, `status`, `createdAt`, `updatedAt` FROM `OutBox` WHERE `id` = ? LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(fetchSql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to load outbox event");
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load outbox event", e);
        }
    }

    @Override
    public boolean markProcessed(String id) {
        String sql = "UPDATE `OutBox` SET `status` = 'PROCESSED', `updatedAt` = NOW() WHERE `id` = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark outbox event processed", e);
        }
    }

    @Override
    public boolean markFailed(String id) {
        String sql = "UPDATE `OutBox` SET `status` = 'FAILED', `updatedAt` = NOW() WHERE `id` = ?";
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
                rs.getString("payload"),
                rs.getString("status"),
                createdAt,
                updatedAt
        );
    }
}
