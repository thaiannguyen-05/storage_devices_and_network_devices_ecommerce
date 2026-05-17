<%@page contentType="text/html" pageEncoding="UTF-8"%>
<nav class="top-menu">
    <div class="top-menu-inner">
        <a href="${pageContext.request.contextPath}/product" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
            </span>
            Trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/product#all-hardware" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
                    <path d="M16 3h-8l-2 4h12z"/>
                </svg>
            </span>
            Sản phẩm
        </a>
        <a href="${pageContext.request.contextPath}/contact" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                </svg>
            </span>
            Liên hệ
        </a>
        <a href="${pageContext.request.contextPath}/cart" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                </svg>
            </span>
            Giỏ hàng
        </a>
        <% if (session.getAttribute("authUserName") != null) { %>
        <% if ("ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("authUserRole")))) { %>
        <a href="${pageContext.request.contextPath}/admin" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <path d="M8 9h8M8 13h8M8 17h5"/>
                </svg>
            </span>
            Admin
        </a>
        <% } %>
        <a href="${pageContext.request.contextPath}/auth?action=profile" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
            </span>
            Tài khoản
        </a>
        <% } else { %>
        <a href="${pageContext.request.contextPath}/auth?action=signin" class="top-menu-item">
            <span class="top-menu-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                    <polyline points="10 17 15 12 10 7"/>
                    <line x1="15" y1="12" x2="3" y2="12"/>
                </svg>
            </span>
            Đăng nhập
        </a>
        <% } %>
    </div>
</nav>
