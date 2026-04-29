<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng ký | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
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
                <h2>Tạo tài khoản</h2>
                <p>Tham gia hệ sinh thái lưu trữ hiệu năng cao.</p>
                
                <form action="${pageContext.request.contextPath}/auth?action=signup" method="post">
                    <div class="ch-form-field">
                        <label>Họ và tên</label>
                        <input type="text" name="fullname" required placeholder="Nguyễn Văn A" value="<%= request.getAttribute("fullname") == null ? "" : request.getAttribute("fullname") %>">
                    </div>

                    <div class="ch-form-field">
                        <label>Email</label>
                        <input type="email" name="email" required placeholder="yourname@gmail.com" pattern="^[A-Za-z0-9._%+-]+@gmail\.com$" title="Email phải có dạng yourname@gmail.com" value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>">
                        <p style="margin:6px 0 0;font-size:12px;opacity:.8;">Email bắt buộc có đuôi @gmail.com</p>
                    </div>

                    <div class="ch-form-field">
                        <label>Mật khẩu</label>
                        <input type="password" name="password" required placeholder="••••••••" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$" title="Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt">
                        <p style="margin:6px 0 0;font-size:12px;opacity:.8;">Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.</p>
                    </div>

                    <div class="ch-form-field">
                        <label>Xác nhận mật khẩu</label>
                        <input type="password" name="confirm_password" required placeholder="••••••••">
                    </div>
                    
                    <div class="ch-form-field" style="margin-top:4px;">
                        <label style="display:flex;gap:8px;align-items:flex-start;font-size:13px;text-transform:none;letter-spacing:0;">
                            <input type="checkbox" name="acceptTerms" value="1" required style="width:auto;min-width:0;height:auto;box-shadow:none;accent-color:#a855f7;" <%= request.getAttribute("acceptTerms") != null ? "checked" : "" %>>
                            <span>Tôi đồng ý với điều khoản và quy định của StoreIT.</span>
                        </label>
                    </div>
                    <button type="submit" class="home-cta btn-full">ĐĂNG KÝ</button>
                    <% if (request.getAttribute("error") != null) { %>
                        <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
                    <% } %>
                    <% if (request.getAttribute("success") != null) { %>
                        <p style="margin: 10px 0 0; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
                    <% } %>

                    <div class="auth-footer">
                        Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth?action=signin">Đăng nhập</a>
                    </div>
                </form>
            </div>
        </main>
    </body>
</html>
