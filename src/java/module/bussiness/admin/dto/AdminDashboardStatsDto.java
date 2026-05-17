package module.bussiness.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardStatsDto {
    private String totalUsers = "N/A";
    private String totalProducts = "N/A";
    private String totalOrders = "N/A";
    private String pendingOrders = "N/A";
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private boolean totalRevenueAvailable = true;
    private final List<DateAmountPoint> revenueLast7Days = new ArrayList<>();
    private final List<DateAmountPoint> revenueLast30Days = new ArrayList<>();
    private final List<TopProductStat> topProducts = new ArrayList<>();
    private final List<LowStockVariantStat> lowStockVariants = new ArrayList<>();
    private final List<DateCountPoint> userRegistrationTrend = new ArrayList<>();
    private final List<RecentOrderStat> recentOrders = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public String getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(String totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(String totalProducts) {
        this.totalProducts = totalProducts;
    }

    public String getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(String totalOrders) {
        this.totalOrders = totalOrders;
    }

    public String getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(String pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public boolean isTotalRevenueAvailable() {
        return totalRevenueAvailable;
    }

    public void setTotalRevenueAvailable(boolean totalRevenueAvailable) {
        this.totalRevenueAvailable = totalRevenueAvailable;
    }

    public List<DateAmountPoint> getRevenueLast7Days() {
        return revenueLast7Days;
    }

    public List<DateAmountPoint> getRevenueLast30Days() {
        return revenueLast30Days;
    }

    public List<TopProductStat> getTopProducts() {
        return topProducts;
    }

    public List<LowStockVariantStat> getLowStockVariants() {
        return lowStockVariants;
    }

    public List<DateCountPoint> getUserRegistrationTrend() {
        return userRegistrationTrend;
    }

    public List<RecentOrderStat> getRecentOrders() {
        return recentOrders;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static class DateAmountPoint {
        private LocalDate date;
        private BigDecimal amount;

        public DateAmountPoint(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    public static class DateCountPoint {
        private LocalDate date;
        private long count;

        public DateCountPoint(LocalDate date, long count) {
            this.date = date;
            this.count = count;
        }

        public LocalDate getDate() {
            return date;
        }

        public long getCount() {
            return count;
        }
    }

    public static class TopProductStat {
        private String productId;
        private String productName;
        private long quantity;

        public TopProductStat(String productId, String productName, long quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public long getQuantity() {
            return quantity;
        }
    }

    public static class LowStockVariantStat {
        private String variantId;
        private String productId;
        private String productName;
        private String sku;
        private int quantity;

        public LowStockVariantStat(String variantId, String productId, String productName, String sku, int quantity) {
            this.variantId = variantId;
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.quantity = quantity;
        }

        public String getVariantId() {
            return variantId;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getSku() {
            return sku;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class RecentOrderStat {
        private String id;
        private String userName;
        private String productName;
        private String status;
        private LocalDateTime createdAt;

        public RecentOrderStat(String id, String userName, String productName, String status, LocalDateTime createdAt) {
            this.id = id;
            this.userName = userName;
            this.productName = productName;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public String getUserName() {
            return userName;
        }

        public String getProductName() {
            return productName;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
