package module.bussiness.voucher.repository.impl;

import entity.VoucherEntity;
import java.util.List;
import module.bussiness.voucher.repository.interfaces.IVoucherRepository;
import module.core.sql.JdbcHelper;

public class VoucherRepository implements IVoucherRepository {
    @Override
    public List<VoucherEntity> findAll() {
        return JdbcHelper.executeQuery("SELECT * FROM Voucher ORDER BY createdAt DESC",
                rs -> map(rs));
    }

    @Override
    public VoucherEntity findById(String id) {
        List<VoucherEntity> rows = JdbcHelper.executeQuery("SELECT * FROM Voucher WHERE id = ?",
                rs -> map(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<VoucherEntity> findActiveByUserId(String userId) {
        return JdbcHelper.executeQuery(
                "SELECT * FROM Voucher WHERE userId = ? AND expTime >= CURRENT_DATE AND quantity > 0 ORDER BY expTime ASC",
                rs -> map(rs), userId);
    }

    @Override
    public void insert(VoucherEntity voucher) {
        JdbcHelper.executeUpdate(
                "INSERT INTO Voucher (id, percent, userId, expTime, quantity) VALUES (?, ?, ?, ?, ?)",
                voucher.getId(), voucher.getPercent(), voucher.getUserId(), voucher.getExpTime(), voucher.getQuantity());
    }

    @Override
    public void update(VoucherEntity voucher) {
        JdbcHelper.executeUpdate(
                "UPDATE Voucher SET percent = ?, userId = ?, expTime = ?, quantity = ? WHERE id = ?",
                voucher.getPercent(), voucher.getUserId(), voucher.getExpTime(), voucher.getQuantity(), voucher.getId());
    }

    @Override
    public void delete(String id) {
        JdbcHelper.executeUpdate("DELETE FROM Voucher WHERE id = ?", id);
    }

    private VoucherEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.Date expDate = rs.getDate("expTime");
        java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
        return new VoucherEntity(rs.getString("id"), rs.getDouble("percent"), rs.getString("userId"),
                expDate == null ? null : expDate.toLocalDate(),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                rs.getInt("quantity"));
    }
}
