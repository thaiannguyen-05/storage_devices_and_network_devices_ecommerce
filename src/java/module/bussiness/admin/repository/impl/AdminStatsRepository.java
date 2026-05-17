package module.bussiness.admin.repository.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.bussiness.admin.dto.AdminDashboardStatsDto;
import module.bussiness.admin.repository.interfaces.IAdminStatsRepository;
import module.core.sql.ConnecDb;

public class AdminStatsRepository implements IAdminStatsRepository {
    private static final Logger LOGGER = Logger.getLogger(AdminStatsRepository.class.getName());

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto();
        stats.setTotalUsers(countValue("SELECT COUNT(*) FROM User", stats, "total users"));
        stats.setTotalProducts(countValue("SELECT COUNT(*) FROM Product", stats, "total products"));
        stats.setTotalOrders(countValue("SELECT COUNT(*) FROM Order", stats, "total orders"));
        stats.setPendingOrders(countValue("SELECT COUNT(*) FROM Order WHERE status = 'PENDING'", stats, "pending orders"));
        loadTotalRevenue(stats);
        loadRevenueTrend(stats, 7);
        loadRevenueTrend(stats, 30);
        loadTopProducts(stats);
        loadLowStockVariants(stats);
        loadUserRegistrationTrend(stats);
        loadRecentOrders(stats);
        return stats;
    }

    private String countValue(String sql, AdminDashboardStatsDto stats, String label) {
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.valueOf(rs.getLong(1));
            }
        } catch (SQLException e) {
            warn(stats, label, e);
        }
        return "N/A";
    }

    private void loadTotalRevenue(AdminDashboardStatsDto stats) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM Payment WHERE status = 'SUCCESS'";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalRevenue(rs.getBigDecimal(1));
                stats.setTotalRevenueAvailable(true);
            }
        } catch (SQLException e) {
            stats.setTotalRevenueAvailable(false);
            warn(stats, "total revenue", e);
        }
    }

    private void loadRevenueTrend(AdminDashboardStatsDto stats, int days) {
        String sql = "SELECT DATE(\"createdAt\") AS day, COALESCE(SUM(amount), 0) AS amount "
                + "FROM Payment WHERE status = 'SUCCESS' AND \"createdAt\" >= NOW() - (? || ' days')::interval "
                + "GROUP BY DATE(\"createdAt\") ORDER BY day ASC";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = toLocalDate(rs.getDate("day"));
                    BigDecimal amount = rs.getBigDecimal("amount");
                    AdminDashboardStatsDto.DateAmountPoint point = new AdminDashboardStatsDto.DateAmountPoint(date, amount);
                    if (days == 7) {
                        stats.getRevenueLast7Days().add(point);
                    } else {
                        stats.getRevenueLast30Days().add(point);
                    }
                }
            }
        } catch (SQLException e) {
            warn(stats, "revenue " + days + "d", e);
        }
    }

    private void loadTopProducts(AdminDashboardStatsDto stats) {
        String sql = "SELECT o.\"productId\", p.name AS productName, SUM(o.quantity) AS soldQuantity "
                + "FROM Order o LEFT JOIN Product p ON p.id = o.\"productId\" "
                + "GROUP BY o.\"productId\", p.name ORDER BY soldQuantity DESC LIMIT 10";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.getTopProducts().add(new AdminDashboardStatsDto.TopProductStat(
                        rs.getString("productId"),
                        rs.getString("productName"),
                        rs.getLong("soldQuantity")
                ));
            }
        } catch (SQLException e) {
            warn(stats, "top selling products", e);
        }
    }

    private void loadLowStockVariants(AdminDashboardStatsDto stats) {
        String sql = "SELECT pv.id, pv.\"productId\", pv.sku, pv.quantity, p.name AS productName "
                + "FROM ProductVariant pv JOIN Product p ON pv.\"productId\" = p.id "
                + "WHERE pv.quantity <= 5 AND pv.status = 'ACTIVE' ORDER BY pv.quantity ASC LIMIT 10";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.getLowStockVariants().add(new AdminDashboardStatsDto.LowStockVariantStat(
                        rs.getString("id"),
                        rs.getString("productId"),
                        rs.getString("productName"),
                        rs.getString("sku"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            warn(stats, "low stock variants", e);
        }
    }

    private void loadUserRegistrationTrend(AdminDashboardStatsDto stats) {
        String sql = "SELECT DATE(\"createdAt\") AS day, COUNT(*) AS countValue "
                + "FROM User WHERE \"createdAt\" >= NOW() - INTERVAL '30 days' "
                + "GROUP BY DATE(\"createdAt\") ORDER BY day ASC";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.getUserRegistrationTrend().add(new AdminDashboardStatsDto.DateCountPoint(
                        toLocalDate(rs.getDate("day")),
                        rs.getLong("countValue")
                ));
            }
        } catch (SQLException e) {
            warn(stats, "user registration trend", e);
        }
    }

    private void loadRecentOrders(AdminDashboardStatsDto stats) {
        String sql = "SELECT o.id, u.name AS userName, p.name AS productName, o.status, o.\"createdAt\" "
                + "FROM Order o LEFT JOIN User u ON u.id = o.\"userId\" "
                + "LEFT JOIN Product p ON p.id = o.\"productId\" ORDER BY o.\"createdAt\" DESC LIMIT 10";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.getRecentOrders().add(new AdminDashboardStatsDto.RecentOrderStat(
                        rs.getString("id"),
                        rs.getString("userName"),
                        rs.getString("productName"),
                        rs.getString("status"),
                        toLocalDateTime(rs.getTimestamp("createdAt"))
                ));
            }
        } catch (SQLException e) {
            warn(stats, "recent orders", e);
        }
    }

    private void warn(AdminDashboardStatsDto stats, String label, Exception e) {
        String message = "Admin dashboard " + label + " unavailable: " + e.getMessage();
        stats.getWarnings().add(message);
        LOGGER.log(Level.WARNING, message, e);
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
