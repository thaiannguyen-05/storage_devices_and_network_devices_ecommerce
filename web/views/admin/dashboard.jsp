<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.dto.AdminDashboardStatsDto"%>
<%@page import="module.bussiness.admin.response_dto.AdminDashboardResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminDashboardResponseDto dashboard = (AdminDashboardResponseDto) request.getAttribute("dashboard");
    AdminDashboardStatsDto stats = dashboard == null ? new AdminDashboardStatsDto() : dashboard.getStats();
    DecimalFormat money = new DecimalFormat("#,##0");
    DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Dashboard</h1>
        </div>
        <a class="admin-primary-link" href="<%= request.getContextPath() %>/admin?action=orders">Quản lý đơn hàng</a>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <div class="admin-stat-grid">
        <div class="admin-stat-card">
            <span>Users</span>
            <strong><%= AdminService.escapeHtml(stats.getTotalUsers()) %></strong>
        </div>
        <div class="admin-stat-card">
            <span>Products</span>
            <strong><%= AdminService.escapeHtml(stats.getTotalProducts()) %></strong>
        </div>
        <div class="admin-stat-card">
            <span>Orders</span>
            <strong><%= AdminService.escapeHtml(stats.getTotalOrders()) %></strong>
        </div>
        <div class="admin-stat-card">
            <span>Revenue</span>
            <strong><%= stats.isTotalRevenueAvailable() ? money.format(stats.getTotalRevenue()) + " VND" : "N/A" %></strong>
        </div>
    </div>

    <% if (!stats.getWarnings().isEmpty()) { %>
        <div class="admin-alert admin-alert-warning">
            Một số thống kê chưa khả dụng vì schema hiện tại chưa đủ cột/table.
        </div>
    <% } %>

    <div class="admin-grid-two">
        <section class="admin-panel">
            <div class="admin-panel-head">
                <h2>Revenue 7 days</h2>
            </div>
            <div class="admin-bars">
                <% for (AdminDashboardStatsDto.DateAmountPoint point : stats.getRevenueLast7Days()) {
                    long amount = point.getAmount() == null ? 0 : point.getAmount().longValue();
                    int height = (int) Math.max(8, Math.min(100, amount / 100000));
                %>
                <div class="admin-bar-item">
                    <span class="admin-bar" style="height:<%= height %>px"></span>
                    <small><%= point.getDate() == null ? "" : point.getDate().getDayOfMonth() + "/" + point.getDate().getMonthValue() %></small>
                </div>
                <% } %>
                <% if (stats.getRevenueLast7Days().isEmpty()) { %>
                    <p class="admin-empty">No revenue data.</p>
                <% } %>
            </div>
        </section>

        <section class="admin-panel">
            <div class="admin-panel-head">
                <h2>User registrations</h2>
            </div>
            <div class="admin-bars">
                <% for (AdminDashboardStatsDto.DateCountPoint point : stats.getUserRegistrationTrend()) {
                    int height = (int) Math.max(8, Math.min(100, point.getCount() * 12));
                %>
                <div class="admin-bar-item">
                    <span class="admin-bar admin-bar-green" style="height:<%= height %>px"></span>
                    <small><%= point.getDate() == null ? "" : point.getDate().getDayOfMonth() + "/" + point.getDate().getMonthValue() %></small>
                </div>
                <% } %>
                <% if (stats.getUserRegistrationTrend().isEmpty()) { %>
                    <p class="admin-empty">No registration data.</p>
                <% } %>
            </div>
        </section>
    </div>

    <div class="admin-grid-two">
        <section class="admin-panel">
            <div class="admin-panel-head">
                <h2>Recent orders</h2>
                <a href="<%= request.getContextPath() %>/admin?action=orders">View all</a>
            </div>
            <table class="admin-table">
                <thead><tr><th>ID</th><th>User</th><th>Product</th><th>Status</th><th>Created</th></tr></thead>
                <tbody>
                <% for (AdminDashboardStatsDto.RecentOrderStat order : stats.getRecentOrders()) { %>
                    <tr>
                        <td><a href="<%= request.getContextPath() %>/admin?action=order-detail&id=<%= AdminService.escapeHtml(order.getId()) %>"><%= AdminService.escapeHtml(order.getId()) %></a></td>
                        <td><%= AdminService.escapeHtml(order.getUserName()) %></td>
                        <td><%= AdminService.escapeHtml(order.getProductName()) %></td>
                        <td><span class="admin-badge"><%= AdminService.escapeHtml(order.getStatus()) %></span></td>
                        <td><%= order.getCreatedAt() == null ? "" : dateTime.format(order.getCreatedAt()) %></td>
                    </tr>
                <% } %>
                <% if (stats.getRecentOrders().isEmpty()) { %>
                    <tr><td colspan="5" class="admin-empty-cell">No recent orders.</td></tr>
                <% } %>
                </tbody>
            </table>
        </section>

        <section class="admin-panel">
            <div class="admin-panel-head">
                <h2>Top products</h2>
            </div>
            <table class="admin-table">
                <thead><tr><th>Product</th><th>Sold</th></tr></thead>
                <tbody>
                <% for (AdminDashboardStatsDto.TopProductStat item : stats.getTopProducts()) { %>
                    <tr>
                        <td><%= AdminService.escapeHtml(item.getProductName()) %></td>
                        <td><%= item.getQuantity() %></td>
                    </tr>
                <% } %>
                <% if (stats.getTopProducts().isEmpty()) { %>
                    <tr><td colspan="2" class="admin-empty-cell">Top product data unavailable.</td></tr>
                <% } %>
                </tbody>
            </table>
        </section>
    </div>

    <section class="admin-panel">
        <div class="admin-panel-head">
            <h2>Low stock variants</h2>
            <a href="<%= request.getContextPath() %>/admin?action=products">Manage products</a>
        </div>
        <table class="admin-table">
            <thead><tr><th>Product</th><th>SKU</th><th>Quantity</th></tr></thead>
            <tbody>
            <% for (AdminDashboardStatsDto.LowStockVariantStat item : stats.getLowStockVariants()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(item.getProductName()) %></td>
                    <td><%= AdminService.escapeHtml(item.getSku()) %></td>
                    <td><span class="admin-badge admin-badge-warn"><%= item.getQuantity() %></span></td>
                </tr>
            <% } %>
            <% if (stats.getLowStockVariants().isEmpty()) { %>
                <tr><td colspan="3" class="admin-empty-cell">No low-stock variants.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>
</section>

<%@include file="../includes/layout-end.jsp" %>
