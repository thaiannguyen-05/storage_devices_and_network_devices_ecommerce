package module.core.sql.repository;

import entity.PasswordResetTokenEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IPasswordResetTokenRepository;

public class PasswordResetTokenRepository implements IPasswordResetTokenRepository {

    @Override
    public void createToken(String email, String tokenHash, int expiryMinutes) {
        String sql = "INSERT INTO `PasswordResetToken` (`id`, `email`, `tokenHash`, `expiresAt`, `createdAt`) VALUES (UUID(), ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), NOW())";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, tokenHash);
            ps.setInt(3, expiryMinutes);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create reset token", e);
        }
    }

    @Override
    public PasswordResetTokenEntity findValidByTokenHash(String tokenHash) {
        String sql = "SELECT id, email, tokenHash, expiresAt, usedAt, createdAt FROM `PasswordResetToken` WHERE tokenHash = ? LIMIT 1";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                PasswordResetTokenEntity token = new PasswordResetTokenEntity();
                token.setId(rs.getString("id"));
                token.setEmail(rs.getString("email"));
                token.setTokenHash(rs.getString("tokenHash"));
                java.sql.Timestamp expiresTs = rs.getTimestamp("expiresAt");
                if (expiresTs != null) token.setExpiresAt(expiresTs.toLocalDateTime());
                java.sql.Timestamp usedTs = rs.getTimestamp("usedAt");
                if (usedTs != null) token.setUsedAt(usedTs.toLocalDateTime());
                java.sql.Timestamp createdTs = rs.getTimestamp("createdAt");
                if (createdTs != null) token.setCreatedAt(createdTs.toLocalDateTime());

                if (token.getUsedAt() != null) return null;
                if (token.getExpiresAt() == null || LocalDateTime.now().isAfter(token.getExpiresAt())) return null;
                return token;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reset token", e);
        }
    }

    @Override
    public void markUsed(String id) {
        String sql = "UPDATE `PasswordResetToken` SET `usedAt` = NOW() WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark token used", e);
        }
    }
}
