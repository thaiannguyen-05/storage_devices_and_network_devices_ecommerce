<%@page contentType="text/html" pageEncoding="UTF-8"%>
<header class="home-header">
    <div class="home-header-top">
        <a class="home-logo-wrap" href="${pageContext.request.contextPath}/product">
            <div class="home-logo-box">L</div>
            <div class="home-logo-text">
                <strong>LinhNamStore</strong>
                <span>High Performance</span>
            </div>
        </a>

        <a class="home-category-btn" href="#categories">
            <span class="home-menu-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                    <path d="M4 7h16a1 1 0 1 0 0-2H4a1 1 0 0 0 0 2zm16 4H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2zm0 6H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2z" />
                </svg>
            </span>
            Danh mục
        </a>

        <form class="home-search" method="get" action="${pageContext.request.contextPath}/product">
            <input type="text" name="q" placeholder="Tìm theo tên sản phẩm hoặc danh mục..." value="">
            <button type="submit" aria-label="Tìm kiếm">
                <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M10.5 3a7.5 7.5 0 1 0 4.76 13.3l4.22 4.22a1 1 0 0 0 1.41-1.41l-4.22-4.22A7.5 7.5 0 0 0 10.5 3zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11z" />
                </svg>
            </button>
        </form>

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
                Object authUserName = session.getAttribute("authUserName");
                Integer cartCount = (Integer) request.getAttribute("cartCount");
                int count = cartCount == null ? 0 : cartCount;
                if (authUserName != null) {
            %>
            <div class="home-account-menu">
                <button type="button" class="home-login" onclick="toggleAccountMenu(this)">
                    <span class="home-inline-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" focusable="false">
                            <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" />
                        </svg>
                    </span>
                    <%= authUserName %>
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
            <a class="home-cart" href="${pageContext.request.contextPath}/cart">
                <span class="home-inline-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M7 18a2 2 0 1 0 2 2 2 2 0 0 0-2-2zm10 0a2 2 0 1 0 2 2 2 2 0 0 0-2-2zM7.16 14h9.59a2 2 0 0 0 1.95-1.57L20 6H6.21l-.27-1.37A1 1 0 0 0 4.96 4H3a1 1 0 1 0 0 2h1.14l2.03 10.17A3 3 0 0 0 9.11 19H19a1 1 0 1 0 0-2H9.11a1 1 0 0 1-.98-.8L8 15h-.84z" />
                    </svg>
                </span>
                Giỏ hàng <span id="headerCartCount"><%= count %></span>
            </a>
        </div>
    </div>
</header>
