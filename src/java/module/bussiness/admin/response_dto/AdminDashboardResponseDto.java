package module.bussiness.admin.response_dto;

import module.bussiness.admin.dto.AdminDashboardStatsDto;

public class AdminDashboardResponseDto {
    private boolean success;
    private String errorMessage;
    private String successMessage;
    private AdminDashboardStatsDto stats;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public AdminDashboardStatsDto getStats() {
        return stats;
    }

    public void setStats(AdminDashboardStatsDto stats) {
        this.stats = stats;
    }
}
