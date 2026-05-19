<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:choose>
    <c:when test="${param.part == 'start'}">
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${empty pageTitle ? 'Admin' : pageTitle}" /></title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
</head>
<body class="admin-body">
    <div class="admin-shell">
        <aside class="admin-sidebar" id="adminSidebar">
            <a class="admin-brand" href="${ctx}/admin/dashboard">
                <span class="admin-brand-mark">LN</span>
                <span>
                    <strong>LinhNamStore</strong>
                    <small>Admin Console</small>
                </span>
            </a>
            <nav class="admin-nav" aria-label="Admin navigation">
                <a class="${activePage == 'admin-dashboard' ? 'active' : ''}" href="${ctx}/admin/dashboard">Dashboard</a>
                <a class="${activePage == 'admin-orders' ? 'active' : ''}" href="${ctx}/admin/orders?action=list">Don hang</a>
                <a class="${activePage == 'admin-users' ? 'active' : ''}" href="${ctx}/admin/users?action=list">User</a>
                <a class="${activePage == 'admin-products' ? 'active' : ''}" href="${ctx}/admin/products?action=list">San pham</a>
                <a class="${activePage == 'admin-brands' ? 'active' : ''}" href="${ctx}/admin/brands?action=list">Brand</a>
                <a class="${activePage == 'admin-vouchers' ? 'active' : ''}" href="${ctx}/admin/vouchers?action=list">Voucher</a>
            </nav>
        </aside>
        <div class="admin-main">
            <header class="admin-topbar">
                <button class="admin-menu-toggle" type="button" data-admin-sidebar-toggle aria-label="Open menu">☰</button>
                <div class="admin-topbar-copy">
                    <h1><c:out value="${empty pageTitle ? 'Admin' : pageTitle}" /></h1>
                    <p>Role: <c:out value="${sessionScope.currentUser.role}" /></p>
                </div>
                <div class="admin-userbox">
                    <strong><c:out value="${sessionScope.currentUser.name}" /></strong>
                    <span><c:out value="${sessionScope.currentUser.email}" /></span>
                    <form action="${ctx}/auth" method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="logout">
                        <button class="button secondary" type="submit">Logout</button>
                    </form>
                </div>
            </header>
            <main class="admin-content">
    </c:when>
    <c:otherwise>
            </main>
            <footer class="admin-footer">Admin module - real-time analytics and management tools</footer>
        </div>
    </div>
    <script>
        (function () {
            var button = document.querySelector("[data-admin-sidebar-toggle]");
            var sidebar = document.getElementById("adminSidebar");
            if (!button || !sidebar) {
                return;
            }
            button.addEventListener("click", function () {
                sidebar.classList.toggle("is-open");
            });
        }());
    </script>
</body>
</html>
    </c:otherwise>
</c:choose>
