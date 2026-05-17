<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@page import="module.bussiness.admin.response_dto.AdminPaymentResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<AdminPaymentResponseDto> paymentsPage = (AdminPage<AdminPaymentResponseDto>) request.getAttribute("paymentsPage");
    String search = (String) request.getAttribute("search");
    String statusFilter = (String) request.getAttribute("statusFilter");
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DecimalFormat money = new DecimalFormat("#,##0");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Payments</h1>
        </div>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <form class="admin-filter" method="get" action="<%= request.getContextPath() %>/admin">
        <input type="hidden" name="action" value="payments">
        <input type="search" name="search" placeholder="Search payment/order/user" value="<%= AdminService.escapeHtml(search) %>">
        <select name="statusFilter">
            <option value="">All statuses</option>
            <% for (String status : AdminService.PAYMENT_STATUSES) { %>
                <option value="<%= status %>" <%= status.equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= status %></option>
            <% } %>
        </select>
        <button type="submit">Filter</button>
    </form>

    <section class="admin-panel">
        <table class="admin-table">
            <thead><tr><th>ID</th><th>Order</th><th>User</th><th>Amount</th><th>Method</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (AdminPaymentResponseDto payment : paymentsPage.getItems()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(payment.getId()) %></td>
                    <td><a href="<%= request.getContextPath() %>/admin?action=order-detail&id=<%= AdminService.escapeHtml(payment.getOrderId()) %>"><%= AdminService.escapeHtml(payment.getOrderId()) %></a></td>
                    <td><%= AdminService.escapeHtml(payment.getUserName()) %></td>
                    <td><%= money.format(payment.getAmount()) %> VND</td>
                    <td><%= AdminService.escapeHtml(payment.getMethod()) %></td>
                    <td><span class="admin-badge"><%= AdminService.escapeHtml(payment.getStatus()) %></span></td>
                    <td><%= payment.getCreatedAt() == null ? "" : dt.format(payment.getCreatedAt()) %></td>
                    <td class="admin-actions">
                        <% if ("FAILED".equalsIgnoreCase(payment.getStatus())) { %>
                            <form method="post" action="<%= request.getContextPath() %>/admin?action=payments-retry">
                                <input type="hidden" name="id" value="<%= AdminService.escapeHtml(payment.getId()) %>">
                                <button type="submit">Retry</button>
                            </form>
                        <% } %>
                    </td>
                </tr>
            <% } %>
            <% if (paymentsPage.getItems().isEmpty()) { %>
                <tr><td colspan="8" class="admin-empty-cell">No payments found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <div class="admin-pagination">
        <% if (paymentsPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=payments&page=<%= paymentsPage.getPage() - 1 %>&search=<%= AdminService.escapeHtml(search) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Previous</a>
        <% } %>
        <span>Page <%= paymentsPage.getPage() %> / <%= paymentsPage.getTotalPages() %></span>
        <% if (paymentsPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=payments&page=<%= paymentsPage.getPage() + 1 %>&search=<%= AdminService.escapeHtml(search) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
