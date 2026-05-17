<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@page import="module.bussiness.admin.response_dto.AdminUserResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<AdminUserResponseDto> usersPage = (AdminPage<AdminUserResponseDto>) request.getAttribute("usersPage");
    String search = (String) request.getAttribute("search");
    String roleFilter = (String) request.getAttribute("roleFilter");
    String statusFilter = (String) request.getAttribute("statusFilter");
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Users</h1>
        </div>
        <a class="admin-primary-link" href="<%= request.getContextPath() %>/admin?action=users-create">Create user</a>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <form class="admin-filter" method="get" action="<%= request.getContextPath() %>/admin">
        <input type="hidden" name="action" value="users">
        <input type="search" name="search" placeholder="Search name or email" value="<%= AdminService.escapeHtml(search) %>">
        <select name="roleFilter">
            <option value="">All roles</option>
            <% for (String role : AdminService.USER_ROLES) { %>
                <option value="<%= role %>" <%= role.equalsIgnoreCase(roleFilter) ? "selected" : "" %>><%= role %></option>
            <% } %>
        </select>
        <select name="statusFilter">
            <option value="">All statuses</option>
            <% for (String status : AdminService.USER_STATUSES) { %>
                <option value="<%= status %>" <%= status.equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= status %></option>
            <% } %>
        </select>
        <button type="submit">Filter</button>
    </form>

    <section class="admin-panel">
        <table class="admin-table">
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (AdminUserResponseDto user : usersPage.getItems()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(user.getId()) %></td>
                    <td><%= AdminService.escapeHtml(user.getName()) %></td>
                    <td><%= AdminService.escapeHtml(user.getEmail()) %></td>
                    <td><span class="admin-badge"><%= AdminService.escapeHtml(user.getRole()) %></span></td>
                    <td><span class="admin-badge"><%= AdminService.escapeHtml(user.getStatus()) %></span></td>
                    <td><%= user.getCreatedAt() == null ? "" : dt.format(user.getCreatedAt()) %></td>
                    <td class="admin-actions">
                        <a href="<%= request.getContextPath() %>/admin?action=users-edit&id=<%= AdminService.escapeHtml(user.getId()) %>">Edit</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=users-activate">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(user.getId()) %>">
                            <button type="submit">Activate</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=users-ban">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(user.getId()) %>">
                            <button type="submit">Ban</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=users-delete">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(user.getId()) %>">
                            <button type="submit" class="danger">Delete</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            <% if (usersPage.getItems().isEmpty()) { %>
                <tr><td colspan="7" class="admin-empty-cell">No users found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <div class="admin-pagination">
        <% if (usersPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=users&page=<%= usersPage.getPage() - 1 %>&search=<%= AdminService.escapeHtml(search) %>&roleFilter=<%= AdminService.escapeHtml(roleFilter) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Previous</a>
        <% } %>
        <span>Page <%= usersPage.getPage() %> / <%= usersPage.getTotalPages() %></span>
        <% if (usersPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=users&page=<%= usersPage.getPage() + 1 %>&search=<%= AdminService.escapeHtml(search) %>&roleFilter=<%= AdminService.escapeHtml(roleFilter) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
