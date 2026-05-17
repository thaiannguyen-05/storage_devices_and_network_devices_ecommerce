package module.bussiness.admin.repository.interfaces;

import module.bussiness.admin.dto.AdminDashboardStatsDto;

public interface IAdminStatsRepository {
    AdminDashboardStatsDto getDashboardStats();
}
