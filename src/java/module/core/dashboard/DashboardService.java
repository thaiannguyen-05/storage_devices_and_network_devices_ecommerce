package module.core.dashboard;

import entity.OrderEntity;
import java.math.BigDecimal;
import java.util.List;
import module.core.dashboard.dto.DashboardStatsDto;
import module.core.sql.JdbcHelper;

public class DashboardService {
    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalUsers(JdbcHelper.count("SELECT COUNT(*) FROM User"));
        stats.setTotalOrders(JdbcHelper.count("SELECT COUNT(*) FROM `Order`"));
        stats.setTotalProducts(JdbcHelper.count("SELECT COUNT(*) FROM Product"));
        List<BigDecimal> totals = JdbcHelper.executeQuery("SELECT COALESCE(SUM(totalAmount), 0) FROM `Order`",
                rs -> rs.getBigDecimal(1));
        stats.setTotalRevenue(totals.isEmpty() || totals.get(0) == null ? BigDecimal.ZERO : totals.get(0));
        return stats;
    }

    public List<OrderEntity> getRecentOrders(int limit) {
        return JdbcHelper.executeQuery(
                "SELECT o.*, p.name AS productName FROM `Order` o "
                + "LEFT JOIN `Product` p ON p.id = o.productId "
                + "ORDER BY o.createdAt DESC LIMIT ?",
                rs -> {
                    OrderEntity order = new OrderEntity();
                    order.setId(rs.getString("id"));
                    order.setUserId(rs.getString("userId"));
                    order.setProductId(rs.getString("productId"));
                    order.setVariantId(rs.getString("variantId"));
                    order.setQuantity(rs.getInt("quantity"));
                    order.setStatus(rs.getString("status"));
                    java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
                    order.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
                    order.setCustomerName(rs.getString("customerName"));
                    order.setEmail(rs.getString("email"));
                    order.setProductName(rs.getString("productName"));
                    order.setTotalAmount(rs.getBigDecimal("totalAmount"));
                    return order;
                }, limit);
    }

    public List<java.util.Map<String, Object>> getRevenueLast7Days() {
        return JdbcHelper.executeQuery(
                "SELECT DATE(createdAt) AS revenueDate, COALESCE(SUM(totalAmount), 0) AS revenue "
                + "FROM `Order` WHERE createdAt >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY) "
                + "GROUP BY DATE(createdAt) ORDER BY revenueDate ASC",
                rs -> {
                    java.util.Map<String, Object> row = new java.util.HashMap<String, Object>();
                    row.put("date", rs.getDate("revenueDate"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    return row;
                });
    }
}
