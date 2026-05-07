<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Xác thực email | LinhNamStore"; %>

<div class="auth-container">
    <div class="auth-card">
        <h2>Xác thực email</h2>
        <p>Nhập mã xác thực đã gửi tới email của bạn.</p>

        <form action="${pageContext.request.contextPath}/auth?action=verifyEmail" method="post">
            <div class="ch-form-field">
                <label>Email</label>
                <input type="email" name="email" required value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>">
            </div>

            <div class="ch-form-field">
                <label>Mã xác thực</label>
                <input type="text" name="code" required placeholder="Nhập mã 6 số" maxlength="6">
            </div>

            <button type="submit" class="home-cta btn-full">XÁC THỰC</button>

            <% if (request.getAttribute("error") != null) { %>
            <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
            <% } %>
            <% if (request.getAttribute("success") != null) { %>
            <p style="margin: 10px 0 0; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
            <% } %>

            <div class="auth-footer">
                Đã xác thực xong? <a href="${pageContext.request.contextPath}/auth?action=signin">Đăng nhập</a>
            </div>
        </form>
    </div>
</div>

<%@include file="../includes/layout-end.jsp" %>
