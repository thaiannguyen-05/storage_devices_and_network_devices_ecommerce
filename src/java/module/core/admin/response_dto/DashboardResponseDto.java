package module.core.admin.response_dto;

import java.util.List;
import java.util.Map;
import module.core.admin.dto.AdminOrderDto;
import module.core.admin.dto.DashboardStatsDto;
import module.core.admin.dto.RevenuePointDto;
import module.core.admin.dto.TopProductDto;
import module.core.common.BaseResponse;

public class DashboardResponseDto extends BaseResponse {
    private DashboardStatsDto dashboardStats;
    private List<AdminOrderDto> recentOrders;
    private List<TopProductDto> topProducts;
    private List<RevenuePointDto> revenueTrend;
    private Map<String, Integer> orderStatusCounts;

    public DashboardStatsDto getDashboardStats() {
        return dashboardStats;
    }

    public void setDashboardStats(DashboardStatsDto dashboardStats) {
        this.dashboardStats = dashboardStats;
    }

    public List<AdminOrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<AdminOrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<TopProductDto> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDto> topProducts) {
        this.topProducts = topProducts;
    }

    public List<RevenuePointDto> getRevenueTrend() {
        return revenueTrend;
    }

    public void setRevenueTrend(List<RevenuePointDto> revenueTrend) {
        this.revenueTrend = revenueTrend;
    }

    public Map<String, Integer> getOrderStatusCounts() {
        return orderStatusCounts;
    }

    public void setOrderStatusCounts(Map<String, Integer> orderStatusCounts) {
        this.orderStatusCounts = orderStatusCounts;
    }
}
