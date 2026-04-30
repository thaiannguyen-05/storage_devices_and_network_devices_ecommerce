package module.core.auth.repository.impl;

import entity.PasswordResetTokenEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import module.core.auth.repository.interfaces.IPasswordResetTokenRepository;
import module.core.sql.ConnecDb;

public class PasswordResetTokenRepository implements IPasswordResetTokenRepository {

    @Override
    public PasswordResetTokenEntity create(String userId, String tokenHash, int expiryMinutes) {
        String sql = "INSERT INTO `PasswordResetToken` (`id`, `userId`, `tokenHash`, `expiresAt`, `usedAt`, `createdAt`) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), NULL, NOW())";
        String id = UUID.randomUUID().toString();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            ps.setString(3, tokenHash);
            ps.setInt(4, expiryMinutes);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create password reset token");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create password reset token", e);
        }

        String fetchSql = "SELECT `id`, `userId`, `tokenHash`, `expiresAt`, `usedAt`, `createdAt` FROM `PasswordResetToken` WHERE `id` = ? LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(fetchSql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to load created password reset token");
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load created password reset token", e);
        }
    }

    @Override
    public PasswordResetTokenEntity findValidByTokenHash(String tokenHash) {
        String sql = "SELECT `id`, `userId`, `tokenHash`, `expiresAt`, `usedAt`, `createdAt` FROM `PasswordResetToken` WHERE `tokenHash` = ? AND `usedAt` IS NULL AND `expiresAt` > NOW() ORDER BY `createdAt` DESC LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find valid password reset token", e);
        }
    }

    @Override
    public boolean markUsed(String id) {
        String sql = "UPDATE `PasswordResetToken` SET `usedAt` = NOW() WHERE `id` = ? AND `usedAt` IS NULL";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark password reset token as used", e);
        }
    }

    @Override
    public int invalidateAllByUserId(String userId) {
        String sql = "UPDATE `PasswordResetToken` SET `usedAt` = NOW() WHERE `userId` = ? AND `usedAt` IS NULL";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to invalidate password reset tokens", e);
        }
    }

    private PasswordResetTokenEntity map(ResultSet rs) throws SQLException {
        Timestamp expiresAtTs = rs.getTimestamp("expiresAt");
        Timestamp usedAtTs = rs.getTimestamp("usedAt");
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        return new PasswordResetTokenEntity(
                rs.getString("id"),
                rs.getString("userId"),
                rs.getString("tokenHash"),
                expiresAtTs == null ? null : expiresAtTs.toLocalDateTime(),
                usedAtTs == null ? null : usedAtTs.toLocalDateTime(),
                createdAtTs == null ? null : createdAtTs.toLocalDateTime()
        );
    }
}
