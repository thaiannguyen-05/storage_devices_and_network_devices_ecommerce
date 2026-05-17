<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.response_dto.AdminOrderResponseDto"%>
<%@page import="module.bussiness.admin.response_dto.AdminPaymentResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminOrderResponseDto order = (AdminOrderResponseDto) request.getAttribute("order");
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DecimalFormat money = new DecimalFormat("#,##0");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Orders</p>
            <h1>Order detail</h1>
        </div>
        <a class="admin-secondary-link" href="<%= request.getContextPath() %>/admin?action=orders">Back</a>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <% if (order == null) { %>
        <div class="admin-alert admin-alert-warning">Order not found.</div>
    <% } else { %>
        <% if (order.isSchemaLimited()) { %>
            <div class="admin-alert admin-alert-warning">Schema migration pending: variant, quantity, phone, and address columns are not available.</div>
        <% } %>

        <div class="admin-grid-two">
            <section class="admin-panel">
                <div class="admin-panel-head"><h2>Order</h2></div>
                <dl class="admin-detail-list">
                    <dt>ID</dt><dd><%= AdminService.escapeHtml(order.getId()) %></dd>
                    <dt>User</dt><dd><%= AdminService.escapeHtml(order.getUserName()) %> (<%= AdminService.escapeHtml(order.getUserEmail()) %>)</dd>
                    <dt>Product</dt><dd><%= AdminService.escapeHtml(order.getProductName()) %></dd>
                    <dt>SKU</dt><dd><%= AdminService.escapeHtml(order.getSku()) %></dd>
                    <dt>Quantity</dt><dd><%= order.getQuantity() %></dd>
                    <dt>Total</dt><dd><%= money.format(order.getTotal()) %> VND</dd>
                    <dt>Created</dt><dd><%= order.getCreatedAt() == null ? "" : dt.format(order.getCreatedAt()) %></dd>
                </dl>
            </section>

            <section class="admin-panel">
                <div class="admin-panel-head"><h2>Delivery</h2></div>
                <dl class="admin-detail-list">
                    <dt>Phone</dt><dd><%= AdminService.escapeHtml(order.getPhone()) %></dd>
                    <dt>Address</dt><dd><%= AdminService.escapeHtml(order.getAddress()) %></dd>
                    <dt>Status</dt><dd><span class="admin-badge"><%= AdminService.escapeHtml(order.getStatus()) %></span></dd>
                </dl>
                <form class="admin-inline-form" method="post" action="<%= request.getContextPath() %>/admin?action=orders-update-status">
                    <input type="hidden" name="id" value="<%= AdminService.escapeHtml(order.getId()) %>">
                    <select name="status">
                        <% for (String status : AdminService.ORDER_STATUSES) { %>
                            <option value="<%= status %>" <%= status.equalsIgnoreCase(order.getStatus()) ? "selected" : "" %>><%= status %></option>
                        <% } %>
                    </select>
                    <button type="submit">Update</button>
                </form>
                <% if (!"CANCELLED".equalsIgnoreCase(order.getStatus()) && !"COMPLETED".equalsIgnoreCase(order.getStatus())) { %>
                    <form class="admin-inline-form" method="post" action="<%= request.getContextPath() %>/admin?action=orders-cancel">
                        <input type="hidden" name="id" value="<%= AdminService.escapeHtml(order.getId()) %>">
                        <button type="submit" class="danger">Cancel order</button>
                    </form>
                <% } %>
            </section>
        </div>

        <section class="admin-panel">
            <div class="admin-panel-head"><h2>Payments</h2></div>
            <table class="admin-table">
                <thead><tr><th>ID</th><th>Amount</th><th>Method</th><th>Status</th><th>Created</th></tr></thead>
                <tbody>
                <% for (AdminPaymentResponseDto payment : order.getPayments()) { %>
                    <tr>
                        <td><%= AdminService.escapeHtml(payment.getId()) %></td>
                        <td><%= money.format(payment.getAmount()) %> VND</td>
                        <td><%= AdminService.escapeHtml(payment.getMethod()) %></td>
                        <td><span class="admin-badge"><%= AdminService.escapeHtml(payment.getStatus()) %></span></td>
                        <td><%= payment.getCreatedAt() == null ? "" : dt.format(payment.getCreatedAt()) %></td>
                    </tr>
                <% } %>
                <% if (order.getPayments().isEmpty()) { %>
                    <tr><td colspan="5" class="admin-empty-cell">No payments for this order.</td></tr>
                <% } %>
                </tbody>
            </table>
        </section>
    <% } %>
</section>

<%@include file="../includes/layout-end.jsp" %>
