<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="entity.UserEntity"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@page import="module.bussiness.admin.response_dto.AdminVoucherResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<AdminVoucherResponseDto> vouchersPage = (AdminPage<AdminVoucherResponseDto>) request.getAttribute("vouchersPage");
    AdminVoucherResponseDto voucher = (AdminVoucherResponseDto) request.getAttribute("voucher");
    List<UserEntity> users = (List<UserEntity>) request.getAttribute("users");
    boolean editMode = Boolean.TRUE.equals(request.getAttribute("editMode"));
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Vouchers</h1>
        </div>
        <a class="admin-primary-link" href="<%= request.getContextPath() %>/admin?action=vouchers-create">New voucher</a>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <div class="admin-grid-two">
        <form class="admin-form-panel" method="post" action="<%= request.getContextPath() %>/admin?action=vouchers-save">
            <input type="hidden" name="id" value="<%= voucher == null ? "" : AdminService.escapeHtml(voucher.getId()) %>">
            <h2><%= editMode ? "Edit voucher" : "Create voucher" %></h2>
            <label>User
                <select name="userId" required>
                    <% if (users != null) { for (UserEntity user : users) { %>
                        <option value="<%= AdminService.escapeHtml(user.getId()) %>" <%= voucher != null && user.getId().equals(voucher.getUserId()) ? "selected" : "" %>><%= AdminService.escapeHtml(user.getEmail()) %></option>
                    <% }} %>
                </select>
            </label>
            <label>Percent
                <input type="number" step="0.01" name="percent" required value="<%= voucher == null || voucher.getPercent() == null ? "" : voucher.getPercent() %>">
            </label>
            <label>Quantity
                <input type="number" name="quantity" required value="<%= voucher == null || voucher.getQuantity() == null ? "1" : voucher.getQuantity() %>">
            </label>
            <label>Expiry
                <input type="date" name="expTime" required value="<%= voucher == null || voucher.getExpTime() == null ? "" : voucher.getExpTime() %>">
            </label>
            <div class="admin-form-actions">
                <button type="submit">Save voucher</button>
                <% if (editMode) { %>
                    <a href="<%= request.getContextPath() %>/admin?action=vouchers">Cancel</a>
                <% } %>
            </div>
        </form>

        <section class="admin-panel">
            <div class="admin-panel-head"><h2>Voucher list</h2></div>
            <table class="admin-table">
                <thead><tr><th>ID</th><th>User</th><th>Percent</th><th>Qty</th><th>Expiry</th><th>Created</th><th>Actions</th></tr></thead>
                <tbody>
                <% for (AdminVoucherResponseDto item : vouchersPage.getItems()) { %>
                    <tr>
                        <td><%= AdminService.escapeHtml(item.getId()) %></td>
                        <td><%= AdminService.escapeHtml(item.getUserEmail()) %></td>
                        <td><%= item.getPercent() %>%</td>
                        <td><%= item.getQuantity() %></td>
                        <td><%= item.getExpTime() == null ? "" : item.getExpTime() %></td>
                        <td><%= item.getCreatedAt() == null ? "" : dt.format(item.getCreatedAt()) %></td>
                        <td class="admin-actions">
                            <a href="<%= request.getContextPath() %>/admin?action=vouchers-edit&id=<%= AdminService.escapeHtml(item.getId()) %>">Edit</a>
                            <form method="post" action="<%= request.getContextPath() %>/admin?action=vouchers-delete">
                                <input type="hidden" name="id" value="<%= AdminService.escapeHtml(item.getId()) %>">
                                <button type="submit" class="danger">Delete</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                <% if (vouchersPage.getItems().isEmpty()) { %>
                    <tr><td colspan="7" class="admin-empty-cell">No vouchers found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </section>
    </div>

    <div class="admin-pagination">
        <% if (vouchersPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=vouchers&page=<%= vouchersPage.getPage() - 1 %>">Previous</a>
        <% } %>
        <span>Page <%= vouchersPage.getPage() %> / <%= vouchersPage.getTotalPages() %></span>
        <% if (vouchersPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=vouchers&page=<%= vouchersPage.getPage() + 1 %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
