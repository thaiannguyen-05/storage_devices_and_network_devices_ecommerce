package module.bussiness.payment.repository.impl;

import entity.VoucherEntity;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.bussiness.payment.repository.interfaces.IVoucherRepository;
import module.core.sql.ConnecDb;

public class VoucherRepository implements IVoucherRepository {

    public VoucherEntity findById(String id) {
        String sql = "SELECT * FROM `Voucher` WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find voucher by id", e);
        }
        return null;
    }

    public List<VoucherEntity> findByUserId(String userId) {
        String sql = "SELECT * FROM `Voucher` WHERE userId = ? AND expTime >= CURDATE() AND quantity > 0 ORDER BY percent DESC";
        List<VoucherEntity> result = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find vouchers by userId", e);
        }
        return result;
    }

    public boolean create(String userId, Double percent, LocalDate expTime, Integer quantity) {
        String sql = "INSERT INTO `Voucher` (id, userId, `percent`, expTime, quantity, createdAt) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.setDouble(3, percent);
            ps.setDate(4, java.sql.Date.valueOf(expTime));
            ps.setInt(5, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create voucher", e);
        }
    }

    public boolean decreaseQuantity(String id) {
        String sql = "UPDATE `Voucher` SET quantity = quantity - 1 WHERE id = ? AND quantity > 0";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to decrease voucher quantity", e);
        }
    }

    public long calculateDiscount(String voucherId, long subtotal) {
        VoucherEntity voucher = findById(voucherId);
        if (voucher == null) return 0;
        if (voucher.getExpTime().isBefore(LocalDate.now())) return 0;
        if (voucher.getQuantity() == null || voucher.getQuantity() <= 0) return 0;
        double percent = voucher.getPercent() == null ? 0 : voucher.getPercent();
        return Math.round(subtotal * percent / 100.0);
    }

    private VoucherEntity mapRow(ResultSet rs) throws SQLException {
        VoucherEntity entity = new VoucherEntity();
        entity.setId(rs.getString("id"));
        entity.setPercent(rs.getDouble("percent"));
        entity.setUserId(rs.getString("userId"));
        entity.setQuantity(rs.getInt("quantity"));
        java.sql.Date exp = rs.getDate("expTime");
        if (exp != null) entity.setExpTime(exp.toLocalDate());
        Timestamp created = rs.getTimestamp("createdAt");
        if (created != null) entity.setCreatedAt(created.toLocalDateTime());
        return entity;
    }
}
