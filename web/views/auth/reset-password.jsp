<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Đặt lại mật khẩu | LinhNamStore"; %>

<div class="auth-container">
    <div class="auth-card">
        <h2>Đặt lại mật khẩu</h2>
        <p>Nhập email, mã xác nhận và mật khẩu mới.</p>
        <form action="${pageContext.request.contextPath}/auth?action=resetPassword" method="post">
            <div class="ch-form-field">
                <label>Email</label>
                <input type="email" name="email" required value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>" placeholder="yourname@gmail.com">
            </div>
            <div class="ch-form-field">
                <label>Mã xác nhận</label>
                <input type="text" name="code" required value="<%= request.getAttribute("code") == null ? "" : request.getAttribute("code") %>" placeholder="Nhập mã xác nhận">
            </div>
            <div class="ch-form-field">
                <label>Mật khẩu mới</label>
                <input type="password" name="newPassword" required placeholder="Nhập mật khẩu mới">
            </div>
            <div class="ch-form-field">
                <label>Xác nhận mật khẩu mới</label>
                <input type="password" name="confirmNewPassword" required placeholder="Nhập lại mật khẩu mới">
            </div>
            <button type="submit" class="home-cta btn-full">CẬP NHẬT MẬT KHẨU</button>
            <% if (request.getAttribute("error") != null) { %>
                <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
            <% } %>
            <% if (request.getAttribute("success") != null) { %>
                <p style="margin: 10px 0 0; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
            <% } %>
        </form>
    </div>
</div>

<%@include file="../includes/layout-end.jsp" %>
