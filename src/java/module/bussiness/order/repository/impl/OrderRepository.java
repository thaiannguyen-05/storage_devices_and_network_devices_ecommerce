package module.bussiness.order.repository.impl;

import entity.OrderEntity;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import module.bussiness.order.repository.interfaces.IOrderRepository;
import module.core.admin.dto.AdminOrderDto;
import module.core.sql.JdbcHelper;

public class OrderRepository implements IOrderRepository {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void insert(OrderEntity o) {
        JdbcHelper.executeUpdate(
            "INSERT INTO `Order` (id, userId, productId, variantId, quantity, phone, address, status, customerName, email, note, paymentMethod, voucherId, totalAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            o.getId(), o.getUserId(), o.getProductId(), o.getVariantId(), o.getQuantity(),
            o.getPhone(), o.getAddress(), o.getStatus(),
            o.getCustomerName(), o.getEmail(), o.getNote(), o.getPaymentMethod(), o.getVoucherId(), o.getTotalAmount());
    }

    @Override
    public OrderEntity findById(String id) {
        List<OrderEntity> rows = JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id WHERE o.id = ?",
            rs -> map(rs), id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<OrderEntity> findByUserId(String userId, int offset, int limit) {
        return JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id WHERE o.userId = ? ORDER BY o.createdAt DESC LIMIT ? OFFSET ?",
            rs -> map(rs), userId, limit, offset
        );
    }

    @Override
    public List<OrderEntity> findAll(int offset, int limit) {
        return JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id ORDER BY o.createdAt DESC LIMIT ? OFFSET ?",
            rs -> map(rs), limit, offset
        );
    }

    @Override
    public int countAll() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `Order`");
    }

    @Override
    public List<AdminOrderDto> findAllWithUser(int offset, int limit, String status, String keyword) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder(baseAdminOrderSelect());
        appendAdminFilters(sql, params, status, keyword);
        sql.append(" ORDER BY o.createdAt DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return JdbcHelper.executeQuery(sql.toString(), rs -> mapAdminOrder(rs), params.toArray());
    }

    @Override
    public int countAllWithUser(String status, String keyword) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM `Order` o LEFT JOIN `User` u ON u.id = o.userId LEFT JOIN `Product` pr ON pr.id = o.productId WHERE 1=1");
        appendAdminFilters(sql, params, status, keyword);
        return JdbcHelper.count(sql.toString(), params.toArray());
    }

    @Override
    public AdminOrderDto findByIdWithDetails(String id) {
        List<AdminOrderDto> rows = JdbcHelper.executeQuery(
                baseAdminOrderSelect() + " AND o.id = ?",
                rs -> mapAdminOrder(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Map<String, Integer> countByStatus() {
        List<Map.Entry<String, Integer>> rows = JdbcHelper.executeQuery(
                "SELECT status, COUNT(*) AS total FROM `Order` GROUP BY status ORDER BY status",
                rs -> Map.entry(rs.getString("status"), rs.getInt("total")));
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : rows) {
            counts.put(entry.getKey(), entry.getValue());
        }
        return counts;
    }

    @Override
    public void updateStatus(String id, String status) {
        JdbcHelper.executeUpdate("UPDATE `Order` SET status = ? WHERE id = ?", status, id);
    }

    private OrderEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp updatedAt = rs.getTimestamp("updatedAt");
        OrderEntity order = new OrderEntity(rs.getString("id"), rs.getString("userId"), rs.getString("productId"),
                rs.getString("variantId"), rs.getInt("quantity"), rs.getTimestamp("createdAt").toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime(), rs.getString("status"));
        order.setPhone(rs.getString("phone"));
        order.setAddress(rs.getString("address"));
        order.setCustomerName(rs.getString("customerName"));
        order.setEmail(rs.getString("email"));
        order.setNote(rs.getString("note"));
        order.setPaymentMethod(rs.getString("paymentMethod"));
        order.setVoucherId(rs.getString("voucherId"));
        java.math.BigDecimal total = rs.getBigDecimal("totalAmount");
        order.setTotalAmount(total);
        try {
            order.setProductName(rs.getString("productName"));
        } catch (java.sql.SQLException e) {
            // ignore if not joined in some simple select queries
        }
        return order;
    }

    private String baseAdminOrderSelect() {
        return "SELECT o.id, o.userId, COALESCE(NULLIF(o.customerName, ''), u.name) AS customerName, "
                + "COALESCE(NULLIF(o.email, ''), u.email) AS email, o.totalAmount, o.status, o.createdAt, "
                + "o.productId, pr.name AS productName, o.quantity, o.phone, o.address, o.note, o.paymentMethod, "
                + "pay.status AS paymentStatus "
                + "FROM `Order` o "
                + "LEFT JOIN `User` u ON u.id = o.userId "
                + "LEFT JOIN `Product` pr ON pr.id = o.productId "
                + "LEFT JOIN `Payment` pay ON pay.orderId = o.id "
                + "WHERE 1=1";
    }

    private void appendAdminFilters(StringBuilder sql, List<Object> params, String status, String keyword) {
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.id LIKE ? OR COALESCE(NULLIF(o.customerName, ''), u.name) LIKE ? OR COALESCE(NULLIF(o.email, ''), u.email) LIKE ? OR pr.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
    }

    private AdminOrderDto mapAdminOrder(java.sql.ResultSet rs) throws java.sql.SQLException {
        AdminOrderDto dto = new AdminOrderDto();
        dto.setOrderId(rs.getString("id"));
        dto.setUserId(rs.getString("userId"));
        dto.setCustomerName(rs.getString("customerName"));
        dto.setEmail(rs.getString("email"));
        dto.setTotalAmount(rs.getBigDecimal("totalAmount"));
        dto.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        dto.setCreatedAt(createdAt == null ? "" : createdAt.toLocalDateTime().format(DATE_TIME_FORMATTER));
        dto.setProductId(rs.getString("productId"));
        dto.setProductName(rs.getString("productName"));
        dto.setQuantity(rs.getInt("quantity"));
        dto.setPhone(rs.getString("phone"));
        dto.setAddress(rs.getString("address"));
        dto.setNote(rs.getString("note"));
        dto.setPaymentMethod(rs.getString("paymentMethod"));
        dto.setPaymentStatus(rs.getString("paymentStatus"));
        return dto;
    }
}
