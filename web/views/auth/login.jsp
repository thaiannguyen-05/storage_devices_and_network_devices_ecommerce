<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng nhập | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
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
        </main>
    </body>
</html>
