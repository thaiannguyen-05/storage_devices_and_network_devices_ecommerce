package module.core.admin.repository.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import module.core.admin.dto.AdminOrderDto;
import module.core.admin.dto.RevenuePointDto;
import module.core.admin.dto.TopProductDto;

public interface IAdminAnalyticsRepository {
    int getTotalUsers();
    int getTotalProducts();
    int getTotalOrders();
    BigDecimal getTotalRevenue();
    List<RevenuePointDto> getRevenueLast7Days();
    Map<String, Integer> getOrdersByStatus();
    List<AdminOrderDto> getRecentOrders(int limit);
    List<TopProductDto> getTopProducts(int limit);
    Map<String, Integer> getUserGrowthLast30Days();
}
