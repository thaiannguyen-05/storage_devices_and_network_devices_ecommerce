package module.core.auth.repository.impl;

import entity.EmailVerificationCodeEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import module.core.auth.repository.interfaces.IEmailVerificationCodeRepository;
import module.core.sql.ConnecDb;

public class EmailVerificationCodeRepository implements IEmailVerificationCodeRepository {

    @Override
    public EmailVerificationCodeEntity create(String userId, String codeHash, int expiryMinutes) {
        String sql = "INSERT INTO `EmailVerificationCode` (`id`, `userId`, `codeHash`, `expiresAt`, `usedAt`, `createdAt`) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), NULL, NOW())";
        String id = UUID.randomUUID().toString();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            ps.setString(3, codeHash);
            ps.setInt(4, expiryMinutes);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create email verification code");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create email verification code", e);
        }

        String fetchSql = "SELECT `id`, `userId`, `codeHash`, `expiresAt`, `usedAt`, `createdAt` FROM `EmailVerificationCode` WHERE `id` = ? LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(fetchSql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to load created email verification code");
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load created email verification code", e);
        }
    }

    @Override
    public EmailVerificationCodeEntity findValidByUserId(String userId) {
        String sql = "SELECT `id`, `userId`, `codeHash`, `expiresAt`, `usedAt`, `createdAt` FROM `EmailVerificationCode` WHERE `userId` = ? AND `usedAt` IS NULL AND `expiresAt` > NOW() ORDER BY `createdAt` DESC LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find valid email verification code", e);
        }
    }

    @Override
    public boolean markUsed(String id) {
        String sql = "UPDATE `EmailVerificationCode` SET `usedAt` = NOW() WHERE `id` = ? AND `usedAt` IS NULL";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark email verification code as used", e);
        }
    }

    @Override
    public int invalidateAllByUserId(String userId) {
        String sql = "UPDATE `EmailVerificationCode` SET `usedAt` = NOW() WHERE `userId` = ? AND `usedAt` IS NULL";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to invalidate email verification codes", e);
        }
    }

    private EmailVerificationCodeEntity map(ResultSet rs) throws SQLException {
        Timestamp expiresAtTs = rs.getTimestamp("expiresAt");
        Timestamp usedAtTs = rs.getTimestamp("usedAt");
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        return new EmailVerificationCodeEntity(
                rs.getString("id"),
                rs.getString("userId"),
                rs.getString("codeHash"),
                expiresAtTs == null ? null : expiresAtTs.toLocalDateTime(),
                usedAtTs == null ? null : usedAtTs.toLocalDateTime(),
                createdAtTs == null ? null : createdAtTs.toLocalDateTime()
        );
    }
}
