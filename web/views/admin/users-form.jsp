<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.response_dto.AdminUserResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminUserResponseDto user = (AdminUserResponseDto) request.getAttribute("user");
    boolean editMode = Boolean.TRUE.equals(request.getAttribute("editMode"));
%>

<section class="admin-shell admin-narrow">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Users</p>
            <h1><%= editMode ? "Edit user" : "Create user" %></h1>
        </div>
        <a class="admin-secondary-link" href="<%= request.getContextPath() %>/admin?action=users">Back</a>
    </div>

    <form class="admin-form-panel" method="post" action="<%= request.getContextPath() %>/admin?action=users-save">
        <input type="hidden" name="id" value="<%= user == null ? "" : AdminService.escapeHtml(user.getId()) %>">
        <label>Name
            <input type="text" name="name" required value="<%= user == null ? "" : AdminService.escapeHtml(user.getName()) %>">
        </label>
        <label>Email
            <input type="email" name="email" required value="<%= user == null ? "" : AdminService.escapeHtml(user.getEmail()) %>">
        </label>
        <label>Role
            <select name="role">
                <% for (String role : AdminService.USER_ROLES) { %>
                    <option value="<%= role %>" <%= user != null && role.equalsIgnoreCase(user.getRole()) ? "selected" : "" %>><%= role %></option>
                <% } %>
            </select>
        </label>
        <label>Status
            <select name="status">
                <% for (String status : AdminService.USER_STATUSES) { %>
                    <option value="<%= status %>" <%= user != null && status.equalsIgnoreCase(user.getStatus()) ? "selected" : "" %>><%= status %></option>
                <% } %>
            </select>
        </label>
        <label>Date of birth
            <input type="date" name="dateOfBirth" value="<%= user == null || user.getDateOfBirth() == null ? "" : user.getDateOfBirth() %>">
        </label>
        <% if (!editMode) { %>
            <p class="admin-help">New accounts get a temporary random password. Use reset password before first sign-in.</p>
        <% } %>
        <div class="admin-form-actions">
            <button type="submit">Save</button>
            <a href="<%= request.getContextPath() %>/admin?action=users">Cancel</a>
        </div>
    </form>
</section>

<%@include file="../includes/layout-end.jsp" %>
