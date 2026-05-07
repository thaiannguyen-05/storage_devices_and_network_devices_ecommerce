<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Đăng nhập | LinhNamStore"; %>

<div class="auth-container">
    <div class="auth-card">
        <h2>Chào mừng bạn quay lại</h2>
        <p>Vui lòng nhập thông tin để đăng nhập.</p>

        <form action="${pageContext.request.contextPath}/auth?action=signin" method="post">
            <div class="ch-form-field">
                <label>Email</label>
                <input type="text" name="username" required placeholder="yourname@gmail.com" value="<%= request.getAttribute("username") == null ? "" : request.getAttribute("username") %>" autofocus>
            </div>

            <div class="ch-form-field">
                <div style="display: flex; justify-content: space-between; align-items: baseline;">
                    <label>Mật khẩu</label>
                    <a href="${pageContext.request.contextPath}/auth?action=forgotPassword" style="font-size: 11px; color: var(--ch-primary); text-decoration: none;">Quên mật khẩu?</a>
                </div>
                <input type="password" name="password" required placeholder="••••••••">
            </div>

            <button type="submit" class="home-cta btn-full">ĐĂNG NHẬP</button>
            <% if (request.getAttribute("error") != null) { %>
                <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
            <% } %>

            <div class="auth-footer">
                Chưa có tài khoản? <a href="${pageContext.request.contextPath}/auth?action=signup">Đăng ký</a>
            </div>
        </form>
    </div>
</div>

<%@include file="../includes/layout-end.jsp" %>
