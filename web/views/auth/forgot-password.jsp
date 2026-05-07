<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Quên mật khẩu | LinhNamStore"; %>

<div class="auth-container">
    <div class="auth-card">
        <h2>Quên mật khẩu</h2>
        <p>Nhập email tài khoản của bạn để đặt lại mật khẩu.</p>
        <form action="${pageContext.request.contextPath}/auth?action=forgotPassword" method="post">
            <div class="ch-form-field">
                <label>Email</label>
                <input type="email" name="email" required placeholder="yourname@gmail.com" value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>">
            </div>
            <button type="submit" class="home-cta btn-full">Quên mật khẩu</button>
            <% if (request.getAttribute("error") != null) { %>
                <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
            <% } %>
            <% if (request.getAttribute("success") != null) { %>
                <p style="margin: 10px 0 0; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
            <% } %>
            <div class="auth-footer">
                <a href="${pageContext.request.contextPath}/auth?action=signin">Quay lại đăng nhập</a>
            </div>
        </form>
    </div>
</div>

<%@include file="../includes/layout-end.jsp" %>
