<%@page contentType="text/html" pageEncoding="UTF-8"%>
<header class="home-header">
    <div class="home-header-top">
        <a class="home-logo-wrap" href="${pageContext.request.contextPath}/">
            <div class="home-logo-box">L</div>
            <div class="home-logo-text">
                <strong>LinhNamStore</strong>
                <span>High Performance</span>
            </div>
        </a>

        <% if (session.getAttribute("authUserName") != null) { %>
        <form class="home-search" method="get" action="${pageContext.request.contextPath}/product">
            <input type="text" name="q" placeholder="Tìm theo tên sản phẩm hoặc danh mục..." value="<%= request.getParameter("q") == null ? "" : request.getParameter("q") %>">
            <button type="submit" aria-label="Tìm kiếm">
                <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M10.5 3a7.5 7.5 0 1 0 4.76 13.3l4.22 4.22a1 1 0 0 0 1.41-1.41l-4.22-4.22A7.5 7.5 0 0 0 10.5 3zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11z" />
                </svg>
            </button>
        </form>
        <% } %>

        <div class="home-header-right">
            <div class="home-hotline">
                <span class="home-inline-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M6.62 10.79a15.05 15.05 0 0 0 6.59 6.59l2.2-2.2a1 1 0 0 1 1.01-.24c1.11.37 2.3.56 3.53.56a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1C10.3 21 3 13.7 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.23.19 2.42.56 3.53a1 1 0 0 1-.24 1.01l-2.2 2.25z" />
                    </svg>
                </span>
                HOTLINE <b>1900 9999</b>
            </div>
            <%
                Object bannerAuthUserName = session.getAttribute("authUserName");
                if (bannerAuthUserName != null) {
            %>
            <div class="home-account-menu">
                <button type="button" class="home-login" onclick="toggleAccountMenu(this)">
                    <span class="home-inline-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" focusable="false">
                            <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" />
                        </svg>
                    </span>
                    <%= bannerAuthUserName %>
                </button>
                <div class="home-account-dropdown">
                    <a href="${pageContext.request.contextPath}/auth?action=profile">Trang cá nhân</a>
                    <a href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
                </div>
            </div>
            <% } else { %>
            <a href="${pageContext.request.contextPath}/auth?action=signin" class="home-login">
                <span class="home-inline-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" />
                    </svg>
                </span>
                Tài khoản
            </a>
            <% } %>
        </div>
    </div>
</header>
