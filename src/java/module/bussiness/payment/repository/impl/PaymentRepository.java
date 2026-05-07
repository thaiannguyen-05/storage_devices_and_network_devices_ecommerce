package module.bussiness.payment.repository.impl;

import entity.PaymentEntity;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.bussiness.payment.repository.interfaces.IPaymentRepository;
import module.core.sql.ConnecDb;

public class PaymentRepository implements IPaymentRepository {

    @Override
    public boolean saveInitPayment(String orderId, String userId, String invoiceNumber, BigDecimal amount, String redirectUrl, String signature, String status) {
        if (orderId == null || orderId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        String sql = "INSERT INTO `Payment` (id, orderId, userId, amount, accessKey, partnerCode, redirectUrl, ipnUrl, extraData, requestType, signature, status, createdAt, updatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, orderId);
            ps.setString(3, userId);
            ps.setBigDecimal(4, amount == null ? BigDecimal.ZERO : amount);
            ps.setString(5, "SEPAY");
            ps.setString(6, "SEPAY");
            ps.setString(7, redirectUrl == null ? "" : redirectUrl);
            ps.setString(8, "");
            ps.setString(9, invoiceNumber == null ? "" : invoiceNumber);
            ps.setString(10, "PURCHASE");
            ps.setString(11, signature == null ? "" : signature);
            ps.setString(12, status == null ? "PENDING" : status);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save init payment", e);
        }
    }

    @Override
    public boolean updateStatusByOrderId(String orderId, String status) {
        if (orderId == null || orderId.isBlank() || status == null || status.isBlank()) {
            return false;
        }

        String sql = "UPDATE `Payment` SET status = ?, updatedAt = NOW() WHERE orderId = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment status", e);
        }
    }

    public PaymentEntity findById(String id) {
        String sql = "SELECT * FROM `Payment` WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment by id", e);
        }
        return null;
    }

    public List<PaymentEntity> findByUserId(String userId) {
        String sql = "SELECT * FROM `Payment` WHERE userId = ? ORDER BY createdAt DESC";
        List<PaymentEntity> result = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payments by userId", e);
        }
        return result;
    }

    public List<PaymentEntity> findAll() {
        String sql = "SELECT * FROM `Payment` ORDER BY createdAt DESC";
        List<PaymentEntity> result = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all payments", e);
        }
        return result;
    }

    public boolean updatePayment(String id, String orderId, BigDecimal amount, String status, String redirectUrl, String signature) {
        String sql = "UPDATE `Payment` SET orderId = ?, amount = ?, status = ?, redirectUrl = ?, signature = ?, updatedAt = NOW() WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setBigDecimal(2, amount == null ? BigDecimal.ZERO : amount);
            ps.setString(3, status);
            ps.setString(4, redirectUrl == null ? "" : redirectUrl);
            ps.setString(5, signature == null ? "" : signature);
            ps.setString(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment", e);
        }
    }

    public boolean deleteById(String id) {
        String sql = "DELETE FROM `Payment` WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete payment", e);
        }
    }

    private PaymentEntity mapRow(ResultSet rs) throws SQLException {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(rs.getString("id"));
        entity.setOrderId(rs.getString("orderId"));
        entity.setUserId(rs.getString("userId"));
        entity.setAmount(rs.getBigDecimal("amount"));
        entity.setAccessKey(rs.getString("accessKey"));
        entity.setPartnerCode(rs.getString("partnerCode"));
        entity.setRedirectUrl(rs.getString("redirectUrl"));
        entity.setIpnUrl(rs.getString("ipnUrl"));
        entity.setExtraData(rs.getString("extraData"));
        entity.setRequestType(rs.getString("requestType"));
        entity.setSignature(rs.getString("signature"));
        entity.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("createdAt");
        if (created != null) entity.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updatedAt");
        if (updated != null) entity.setUpdatedAt(updated.toLocalDateTime());
        return entity;
    }
}
