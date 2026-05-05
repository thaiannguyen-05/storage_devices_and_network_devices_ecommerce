<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Reset Password | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <script defer src="${pageContext.request.contextPath}/assets/js/theme-toggle.js"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
    </head>
    <body>
        <header class="home-header"><div class="home-header-top"><a class="home-logo-wrap" href="${pageContext.request.contextPath}/product"><div class="home-logo-box">S</div><div class="home-logo-text"><strong>StoreIT</strong><span>High Performance</span></div></a></div></header>
        <main class="auth-container">
            <div class="auth-card">
                <h2>Reset Password</h2>
                <p>Enter your email, the reset code, and your new password.</p>
                <form action="${pageContext.request.contextPath}/auth?action=resetPassword" method="post">
                    <div class="ch-form-field">
                        <label>Email</label>
                        <input type="email" name="email" required value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>" placeholder="Enter your email">
                    </div>
                    <div class="ch-form-field">
                        <label>Reset code</label>
                        <input type="text" name="code" required value="<%= request.getAttribute("code") == null ? "" : request.getAttribute("code") %>" placeholder="Enter the reset code">
                    </div>
                    <div class="ch-form-field">
                        <label>New password</label>
                        <input type="password" name="newPassword" required placeholder="Enter your new password">
                    </div>
                    <div class="ch-form-field">
                        <label>Confirm new password</label>
                        <input type="password" name="confirmNewPassword" required placeholder="Re-enter your new password">
                    </div>
                    <button type="submit" class="home-cta btn-full">UPDATE PASSWORD</button>
                    <% if (request.getAttribute("error") != null) { %>
                        <p style="margin: 10px 0 0; color: #ef4444; font-size: 13px;"><%= request.getAttribute("error") %></p>
                    <% } %>
                    <% if (request.getAttribute("success") != null) { %>
                        <p style="margin: 10px 0 0; color: #22c55e; font-size: 13px;"><%= request.getAttribute("success") %></p>
                    <% } %>
                </form>
            </div>
        </main>
    </body>
</html>
