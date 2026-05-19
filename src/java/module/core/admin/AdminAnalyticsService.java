package module.core.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import module.core.admin.dto.AdminOrderDto;
import module.core.admin.dto.DashboardStatsDto;
import module.core.admin.dto.RevenuePointDto;
import module.core.admin.dto.TopProductDto;
import module.core.admin.repository.impl.AdminAnalyticsRepository;
import module.core.admin.repository.interfaces.IAdminAnalyticsRepository;
import module.core.admin.response_dto.DashboardResponseDto;

public class AdminAnalyticsService {
    private final IAdminAnalyticsRepository repository = new AdminAnalyticsRepository();

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalUsers(repository.getTotalUsers());
        dto.setTotalProducts(repository.getTotalProducts());
        dto.setTotalOrders(repository.getTotalOrders());
        dto.setTotalRevenue(defaultAmount(repository.getTotalRevenue()));
        dto.setActiveOrders(sumActiveOrders(repository.getOrdersByStatus()));
        return dto;
    }

    public List<AdminOrderDto> getRecentOrders(int limit) {
        return repository.getRecentOrders(limit);
    }

    public List<TopProductDto> getTopSellingProducts(int limit) {
        return repository.getTopProducts(limit);
    }

    public List<RevenuePointDto> getRevenueTrend() {
        List<RevenuePointDto> raw = repository.getRevenueLast7Days();
        Map<String, BigDecimal> amountsByDate = new LinkedHashMap<String, BigDecimal>();
        for (RevenuePointDto point : raw) {
            amountsByDate.put(point.getDate(), defaultAmount(point.getAmount()));
        }
        List<RevenuePointDto> trend = new ArrayList<RevenuePointDto>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String key = date.toString();
            trend.add(new RevenuePointDto(key, amountsByDate.getOrDefault(key, BigDecimal.ZERO)));
        }
        return trend;
    }

    public DashboardResponseDto getDashboardData() {
        DashboardResponseDto response = new DashboardResponseDto();
        response.setDashboardStats(getDashboardStats());
        response.setRecentOrders(getRecentOrders(8));
        response.setTopProducts(getTopSellingProducts(5));
        response.setRevenueTrend(getRevenueTrend());
        response.setOrderStatusCounts(repository.getOrdersByStatus());
        response.setSuccess(true);
        return response;
    }

    private int sumActiveOrders(Map<String, Integer> counts) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String status = entry.getKey() == null ? "" : entry.getKey().trim().toUpperCase();
            if (!"COMPLETED".equals(status) && !"CANCELLED".equals(status) && !"DELIVERED".equals(status)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
