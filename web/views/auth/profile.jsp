<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="entity.UserEntity"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Profile | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <script defer src="${pageContext.request.contextPath}/assets/js/theme-toggle.js"></script>
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
                <h2>Trang ca nhan</h2>
                <%
                    UserEntity user = (UserEntity) request.getAttribute("profileUser");
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                %>
                <% if (request.getAttribute("error") != null) { %>
                    <p style="margin: 0 0 10px; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
                <% } %>
                <% if (request.getAttribute("success") != null) { %>
                    <p style="margin: 0 0 10px; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
                <% } %>

                <% if (user == null) { %>
                    <p style="color:#ef4444;">Khong tim thay thong tin tai khoan. Vui long dang nhap lai.</p>
                <% } else { %>
                    <form>
                        <div class="ch-form-field">
                            <label>Full Name</label>
                            <input type="text" value="<%= user.getName() == null ? "" : user.getName() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Email</label>
                            <input type="text" value="<%= user.getEmail() == null ? "" : user.getEmail() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Date Of Birth</label>
                            <input type="text" value="<%= user.getDateOfBirth() == null ? "" : user.getDateOfBirth().format(dateFormatter) %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Status</label>
                            <input type="text" value="<%= user.getStatus() == null ? "" : user.getStatus() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Role</label>
                            <input type="text" value="<%= user.getRole() == null ? "" : user.getRole() %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Created At</label>
                            <input type="text" value="<%= user.getCreatedAt() == null ? "" : user.getCreatedAt().format(dateTimeFormatter) %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Updated At</label>
                            <input type="text" value="<%= user.getUpdatedAt() == null ? "" : user.getUpdatedAt().format(dateTimeFormatter) %>" readonly>
                        </div>
                        <div class="ch-form-field">
                            <label>Security</label>
                            <input type="text" value="Use forgot password flow to change your password." readonly>
                        </div>
                    </form>
                <% } %>
                <div class="auth-footer" style="display:flex;gap:16px;justify-content:center;">
                    <a href="${pageContext.request.contextPath}/product">Quay lai cua hang</a>
                    <a href="${pageContext.request.contextPath}/auth?action=forgotPassword">Quen mat khau?</a>
                    <a href="${pageContext.request.contextPath}/auth?action=logout" style="color:#ef4444;">Dang xuat</a>
                </div>
            </div>
        </main>
    </body>
</html>
