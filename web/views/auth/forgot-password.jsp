<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quên mật khẩu | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
    </head>
    <body>
        <header class="home-header"><div class="home-header-top"><a class="home-logo-wrap" href="${pageContext.request.contextPath}/product"><div class="home-logo-box">S</div><div class="home-logo-text"><strong>StoreIT</strong><span>High Performance</span></div></a></div></header>
        <main class="auth-container">
            <div class="auth-card">
                <h2>Quên mật khẩu</h2>
                <p>Nhập email tài khoản để nhận link đặt lại mật khẩu.</p>
                <form action="${pageContext.request.contextPath}/auth?action=forgotPassword" method="post">
                    <div class="ch-form-field">
                        <label>Email</label>
                        <input type="email" name="email" required placeholder="you@example.com" value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>">
                    </div>
                    <button type="submit" class="home-cta btn-full">GỬI LINK ĐẶT LẠI MẬT KHẨU</button>
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
        </main>
    </body>
</html>
