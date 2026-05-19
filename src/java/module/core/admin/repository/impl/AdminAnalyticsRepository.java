package module.core.admin.repository.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import module.core.admin.dto.AdminOrderDto;
import module.core.admin.dto.RevenuePointDto;
import module.core.admin.dto.TopProductDto;
import module.core.admin.repository.interfaces.IAdminAnalyticsRepository;
import module.core.sql.JdbcHelper;

public class AdminAnalyticsRepository implements IAdminAnalyticsRepository {
    private static final String REVENUE_STATUSES = "'CONFIRMED','COMPLETED','DELIVERED','PAID'";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public int getTotalUsers() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `User`");
    }

    @Override
    public int getTotalProducts() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `Product`");
    }

    @Override
    public int getTotalOrders() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `Order`");
    }

    @Override
    public BigDecimal getTotalRevenue() {
        List<BigDecimal> rows = JdbcHelper.executeQuery(
                "SELECT COALESCE(SUM(totalAmount), 0) FROM `Order` WHERE status IN (" + REVENUE_STATUSES + ")",
                rs -> rs.getBigDecimal(1));
        return rows.isEmpty() || rows.get(0) == null ? BigDecimal.ZERO : rows.get(0);
    }

    @Override
    public List<RevenuePointDto> getRevenueLast7Days() {
        return JdbcHelper.executeQuery(
                "SELECT DATE(createdAt) AS reportDate, COALESCE(SUM(totalAmount), 0) AS amount "
                + "FROM `Order` "
                + "WHERE createdAt >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
                + "AND status IN (" + REVENUE_STATUSES + ") "
                + "GROUP BY DATE(createdAt) ORDER BY reportDate ASC",
                rs -> {
                    Date reportDate = rs.getDate("reportDate");
                    return new RevenuePointDto(
                            reportDate == null ? "" : reportDate.toLocalDate().toString(),
                            rs.getBigDecimal("amount"));
                });
    }

    @Override
    public Map<String, Integer> getOrdersByStatus() {
        List<Map.Entry<String, Integer>> rows = JdbcHelper.executeQuery(
                "SELECT status, COUNT(*) AS total FROM `Order` GROUP BY status ORDER BY status",
                rs -> Map.entry(rs.getString("status"), rs.getInt("total")));
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : rows) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<AdminOrderDto> getRecentOrders(int limit) {
        return JdbcHelper.executeQuery(
                "SELECT o.id, o.userId, COALESCE(NULLIF(o.customerName, ''), u.name) AS customerName, "
                + "COALESCE(NULLIF(o.email, ''), u.email) AS email, o.totalAmount, o.status, o.createdAt, "
                + "p.id AS productId, p.name AS productName, o.quantity, o.phone, o.address, o.note, "
                + "o.paymentMethod, pay.status AS paymentStatus "
                + "FROM `Order` o "
                + "LEFT JOIN `User` u ON u.id = o.userId "
                + "LEFT JOIN `Product` p ON p.id = o.productId "
                + "LEFT JOIN `Payment` pay ON pay.orderId = o.id "
                + "ORDER BY o.createdAt DESC LIMIT ?",
                rs -> mapAdminOrder(rs), limit);
    }

    @Override
    public List<TopProductDto> getTopProducts(int limit) {
        return JdbcHelper.executeQuery(
                "SELECT p.id AS productId, p.name AS productName, COALESCE(SUM(o.quantity), 0) AS totalSold, "
                + "COALESCE(SUM(o.totalAmount), 0) AS revenue "
                + "FROM `Order` o "
                + "JOIN `Product` p ON p.id = o.productId "
                + "GROUP BY p.id, p.name "
                + "ORDER BY totalSold DESC, revenue DESC "
                + "LIMIT ?",
                rs -> {
                    TopProductDto dto = new TopProductDto();
                    dto.setProductId(rs.getString("productId"));
                    dto.setProductName(rs.getString("productName"));
                    dto.setTotalSold(rs.getInt("totalSold"));
                    dto.setRevenue(rs.getBigDecimal("revenue"));
                    return dto;
                }, limit);
    }

    @Override
    public Map<String, Integer> getUserGrowthLast30Days() {
        List<Map.Entry<String, Integer>> rows = JdbcHelper.executeQuery(
                "SELECT DATE(createdAt) AS reportDate, COUNT(*) AS total "
                + "FROM `User` "
                + "WHERE createdAt >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) "
                + "GROUP BY DATE(createdAt) ORDER BY reportDate ASC",
                rs -> Map.entry(rs.getDate("reportDate").toLocalDate().toString(), rs.getInt("total")));
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : rows) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
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
