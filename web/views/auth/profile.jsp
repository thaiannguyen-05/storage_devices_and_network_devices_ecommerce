<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="entity.UserEntity"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Hồ sơ tài khoản | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
    </head>
    <body>
        <header class="home-header">
            <div class="home-header-top">
                <a class="home-logo-wrap" href="${pageContext.request.contextPath}/product">
                    <div class="home-logo-box">S</div>
                    <div class="home-logo-text">
                        <strong>StoreIT</strong>
                        <span>High Performance</span>
                    </div>
                </a>
            </div>
        </header>

        <main class="auth-container">
            <div class="auth-card">
                <h2>Hồ sơ tài khoản</h2>
                <%
                    UserEntity user = (UserEntity) request.getAttribute("profileUser");
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss");
                %>
                <% if (request.getAttribute("error") != null) { %>
                    <p style="margin: 0 0 10px; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
                <% } %>
                <% if (request.getAttribute("success") != null) { %>
                    <p style="margin: 0 0 10px; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
                <% } %>

                <% if (user == null) { %>
                    <p style="color:#ef4444;">Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.</p>
                <% } else { %>
                    <form action="${pageContext.request.contextPath}/auth?action=updateProfile" method="post">
                        <div class="ch-form-field">
                            <label>Full Name</label>
                            <input type="text" name="fullname" value="<%= request.getAttribute("profileName") == null ? (user.getName() == null ? "" : user.getName()) : request.getAttribute("profileName") %>" required>
                        </div>
                        <div class="ch-form-field">
                            <label>Email</label>
                            <input type="text" value="<%= user.getEmail() == null ? "" : user.getEmail() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Trạng thái tài khoản</label>
                            <input type="text" value="<%= user.getStatus() == null ? "" : user.getStatus() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Ngày tạo</label>
                            <input type="text" value="<%= user.getCreatedAt() == null ? "" : user.getCreatedAt().format(dtf) %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Cập nhật lần cuối</label>
                            <input type="text" value="<%= user.getUpdatedAt() == null ? "" : user.getUpdatedAt().format(dtf) %>" readonly>
                        </div>

                        <div class="ch-form-field">
                            <div style="display: flex; justify-content: space-between; align-items: baseline;">
                                <label>Mật khẩu hiện tại</label>
                                <a href="${pageContext.request.contextPath}/auth?action=forgotPassword" style="font-size: 11px; color: var(--ch-primary); text-decoration: none;">Quên mật khẩu?</a>
                            </div>
                            <input type="password" name="currentPassword" placeholder="Nhập mật khẩu hiện tại nếu muốn đổi mật khẩu">
                        </div>
                        <div class="ch-form-field">
                            <label>Mật khẩu mới</label>
                            <input type="password" name="newPassword" placeholder="Nhập mật khẩu mới">
                        </div>
                        <div class="ch-form-field">
                            <label>Xác nhận mật khẩu mới</label>
                            <input type="password" name="confirmNewPassword" placeholder="Nhập lại mật khẩu mới">
                        </div>

                        <button type="submit" class="home-cta btn-full">CẬP NHẬT HỒ SƠ</button>
                    </form>
                <% } %>
                <div class="auth-footer" style="display:flex;gap:16px;justify-content:center;">
                    <a href="${pageContext.request.contextPath}/product">Quay lại cửa hàng</a>
                    <a href="${pageContext.request.contextPath}/auth?action=logout" style="color:#ef4444;">Đăng xuất</a>
                </div>
            </div>
        </main>
    </body>
</html>
