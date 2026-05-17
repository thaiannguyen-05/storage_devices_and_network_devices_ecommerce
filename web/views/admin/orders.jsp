<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@page import="module.bussiness.admin.response_dto.AdminOrderResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<AdminOrderResponseDto> ordersPage = (AdminPage<AdminOrderResponseDto>) request.getAttribute("ordersPage");
    String search = (String) request.getAttribute("search");
    String statusFilter = (String) request.getAttribute("statusFilter");
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DecimalFormat money = new DecimalFormat("#,##0");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Orders</h1>
        </div>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <form class="admin-filter" method="get" action="<%= request.getContextPath() %>/admin">
        <input type="hidden" name="action" value="orders">
        <input type="search" name="search" placeholder="Search order/user/product" value="<%= AdminService.escapeHtml(search) %>">
        <select name="statusFilter">
            <option value="">All statuses</option>
            <% for (String status : AdminService.ORDER_STATUSES) { %>
                <option value="<%= status %>" <%= status.equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= status %></option>
            <% } %>
        </select>
        <button type="submit">Filter</button>
    </form>

    <section class="admin-panel">
        <table class="admin-table">
            <thead><tr><th>ID</th><th>User</th><th>Product</th><th>Qty</th><th>Total</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (AdminOrderResponseDto order : ordersPage.getItems()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(order.getId()) %></td>
                    <td><%= AdminService.escapeHtml(order.getUserName()) %></td>
                    <td><%= AdminService.escapeHtml(order.getProductName()) %></td>
                    <td><%= order.getQuantity() %></td>
                    <td><%= money.format(order.getTotal()) %> VND</td>
                    <td><span class="admin-badge"><%= AdminService.escapeHtml(order.getStatus()) %></span></td>
                    <td><%= order.getCreatedAt() == null ? "" : dt.format(order.getCreatedAt()) %></td>
                    <td class="admin-actions">
                        <a href="<%= request.getContextPath() %>/admin?action=order-detail&id=<%= AdminService.escapeHtml(order.getId()) %>">Detail</a>
                    </td>
                </tr>
            <% } %>
            <% if (ordersPage.getItems().isEmpty()) { %>
                <tr><td colspan="8" class="admin-empty-cell">No orders found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <div class="admin-pagination">
        <% if (ordersPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=orders&page=<%= ordersPage.getPage() - 1 %>&search=<%= AdminService.escapeHtml(search) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Previous</a>
        <% } %>
        <span>Page <%= ordersPage.getPage() %> / <%= ordersPage.getTotalPages() %></span>
        <% if (ordersPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=orders&page=<%= ordersPage.getPage() + 1 %>&search=<%= AdminService.escapeHtml(search) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
