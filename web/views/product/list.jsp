<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@page import="java.util.List" %>
        <%@page import="java.util.ArrayList" %>
            <%@page import="java.util.Set" %>
                <%@page import="module.bussiness.product.dto.ProductCardView" %>
                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>LinhNamStore | Nền tảng thiết bị lưu trữ</title>
                        <link rel="preconnect" href="https://fonts.googleapis.com">
                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                        <link
                            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap"
                            rel="stylesheet">
                        <script defer src="${pageContext.request.contextPath}/assets/js/theme-toggle.js"></script>
                        <link rel="stylesheet"
                            href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
                    </head>

                    <body>
                        <% Boolean isAdmin=(Boolean) request.getAttribute("isAdmin"); boolean adminView=isAdmin !=null
                            && isAdmin; Integer cartCount=(Integer) request.getAttribute("cartCount"); int
                            count=cartCount==null ? 0 : cartCount; String selectedCategory=(String)
                            request.getAttribute("selectedCategory"); Set<String> categories = (Set<String>)
                                request.getAttribute("categories");
                                String error = (String) request.getAttribute("error");
                                String q = request.getParameter("q");
                                if (q == null) q = "";
                                boolean cartAdded = "1".equals(request.getParameter("cartAdded"));
                                String contextPath = request.getContextPath();

                                List<ProductCardView> cards = (List<ProductCardView>)
                                        request.getAttribute("productCards");
                                        if (cards == null) cards = new ArrayList<>();

                                            List<ProductCardView> featuredProducts = (List<ProductCardView>)
                                                    request.getAttribute("featuredProducts");
                                                    if (featuredProducts == null || featuredProducts.isEmpty())
                                                    featuredProducts = cards;

                                                    int matchedCount = 0;
                                                    String normalizedKeyword = q.trim().toLowerCase();
                                                    for (ProductCardView card : cards) {
                                                    if (!normalizedKeyword.isEmpty()) {
                                                    String name = card.getName() == null ? "" :
                                                    card.getName().toLowerCase();
                                                    String category = card.getCategory() == null ? "" :
                                                    card.getCategory().toLowerCase();
                                                    if (!name.contains(normalizedKeyword) && !category.contains(normalizedKeyword)) {
                                                    continue;
                                                    }
                                                    }
                                                    matchedCount++;
                                                    }
                                                    String filterLabel = !q.trim().isEmpty() ? q.trim() :
                                                    ((selectedCategory != null && !selectedCategory.isEmpty()) ?
                                                    selectedCategory : "tất cả");

                                                    java.util.Map<String, Integer> categoryCounts =
                                                        (java.util.Map<String, Integer>) request.getAttribute("categoryCounts");
                                                    if (categoryCounts == null) {
                                                        categoryCounts = new java.util.LinkedHashMap<>();
                                                    }
                                                    %>

                                                    <header class="home-header">
                                                        <div class="home-header-top">
                                                            <a class="home-logo-wrap"
                                                                href="${pageContext.request.contextPath}/product">
                                                                <div class="home-logo-box">L</div>
                                                                <div class="home-logo-text">
                                                                    <strong>LinhNamStore</strong>
                                                                    <span>High Performance</span>
                                                                </div>
                                                            </a>

                                                            <a class="home-category-btn" href="#categories">
                                                                <span class="home-menu-icon" aria-hidden="true">
                                                                    <svg viewBox="0 0 24 24" focusable="false">
                                                                        <path
                                                                            d="M4 7h16a1 1 0 1 0 0-2H4a1 1 0 0 0 0 2zm16 4H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2zm0 6H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2z" />
                                                                    </svg>
                                                                </span>
                                                                Danh mục
                                                            </a>

                                                            <form class="home-search" method="get"
                                                                action="${pageContext.request.contextPath}/product">
                                                                <input type="hidden" name="admin"
                                                                    value="<%= adminView ? " 1" : "0" %>">
                                                                <input type="text" name="q"
                                                                    placeholder="Tìm theo tên sản phẩm hoặc danh mục..."
                                                                    value="<%= q %>">
                                                                <button type="submit" aria-label="Tìm kiếm">
                                                                    <svg viewBox="0 0 24 24" aria-hidden="true"
                                                                        focusable="false">
                                                                        <path
                                                                            d="M10.5 3a7.5 7.5 0 1 0 4.76 13.3l4.22 4.22a1 1 0 0 0 1.41-1.41l-4.22-4.22A7.5 7.5 0 0 0 10.5 3zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11z" />
                                                                    </svg>
                                                                </button>
                                                            </form>

                                                            <div class="home-header-right">
                                                                <div class="home-hotline">
                                                                    <span class="home-inline-icon" aria-hidden="true">
                                                                        <svg viewBox="0 0 24 24" focusable="false">
                                                                            <path
                                                                                d="M6.62 10.79a15.05 15.05 0 0 0 6.59 6.59l2.2-2.2a1 1 0 0 1 1.01-.24c1.11.37 2.3.56 3.53.56a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1C10.3 21 3 13.7 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.23.19 2.42.56 3.53a1 1 0 0 1-.24 1.01l-2.2 2.25z" />
                                                                        </svg>
                                                                    </span>
                                                                    HOTLINE <b>1900 9999</b>
                                                                </div>
                                                                <% Object
                                                                    authUserName=session.getAttribute("authUserName");
                                                                    if (authUserName !=null) { %>
                                                                    <div class="home-account-menu">
                                                                        <button type="button" class="home-login" onclick="toggleAccountMenu(this)">
                                                                            <span class="home-inline-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" /></svg></span>
                                                                            <%= authUserName %>
                                                                        </button>
                                                                        <div class="home-account-dropdown">
                                                                            <a href="${pageContext.request.contextPath}/auth?action=profile">Trang cá nhân</a>
                                                                            <a href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
                                                                        </div>
                                                                    </div>
                                                                    <% } else { %>
                                                                        <a href="${pageContext.request.contextPath}/auth?action=signin"
                                                                            class="home-login"><span
                                                                                class="home-inline-icon"
                                                                                aria-hidden="true"><svg
                                                                                    viewBox="0 0 24 24"
                                                                                    focusable="false">
                                                                                    <path
                                                                                        d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" />
                                                                                </svg></span>Tài khoản</a>
                                                                        <% } %>
                                                                            <a class="home-cart"
                                                                                href="${pageContext.request.contextPath}/cart">
                                                                                <span class="home-inline-icon"
                                                                                    aria-hidden="true">
                                                                                    <svg viewBox="0 0 24 24"
                                                                                        focusable="false">
                                                                                        <path
                                                                                            d="M7 18a2 2 0 1 0 2 2 2 2 0 0 0-2-2zm10 0a2 2 0 1 0 2 2 2 2 0 0 0-2-2zM7.16 14h9.59a2 2 0 0 0 1.95-1.57L20 6H6.21l-.27-1.37A1 1 0 0 0 4.96 4H3a1 1 0 1 0 0 2h1.14l2.03 10.17A3 3 0 0 0 9.11 19H19a1 1 0 1 0 0-2H9.11a1 1 0 0 1-.98-.8L8 15h-.84z" />
                                                                                    </svg>
                                                                                </span>
                                                                                Giỏ hàng <span id="headerCartCount">
                                                                                    <%= count %>
                                                                                </span>
                                                                            </a>
                                                            </div>
                                                        </div>
                                                    </header>

                                                    <main class="home-main">
                                                        <% if (error !=null && !error.isEmpty()) { %>
                                                            <div class="home-alert">
                                                                <%= error %>
                                                            </div>
                                                            <% } %>

                                                                <section class="home-hero">
                                                                    <div class="home-hero-content">
                                                                        <p class="home-hero-tag">HIỆU SUẤT VƯỢT TRỘI -
                                                                            LƯU TRỮ BỀN BỈ</p>
                                                                        <h1>GIẢI PHÁP LƯU TRỮ <br><span>CHO MỌI NHU
                                                                                CẦU</span></h1>
                                                                        <p>Cung cấp thiết bị lưu trữ chính hãng từ các
                                                                            thương hiệu hàng đầu. Hiệu suất cao – An
                                                                            toàn dữ liệu – Bền bỉ theo thời gian.</p>
                                                                        <div class="home-hero-actions">
                                                                            <a href="#featured" class="home-cta">KHÁM
                                                                                PHÁ NGAY</a>
                                                                        </div>
                                                                    </div>
                                                                    <div class="home-hero-image">
                                                                        <img src="${pageContext.request.contextPath}/assets/images/hero_banner.png"
                                                                            alt="Storage Devices"
                                                                            fetchpriority="high">
                                                                    </div>
                                                                </section>

                                                                <section class="home-usp-top home-usp-priority">
                                                                    <div class="home-usp-item">
                                                                        <span class="home-usp-icon" aria-hidden="true">
                                                                            <svg viewBox="0 0 24 24">
                                                                                <path
                                                                                    d="M12 2 4 5v6c0 5.55 3.84 10.74 8 12 4.16-1.26 8-6.45 8-12V5l-8-3zm0 2.18 6 2.25V11c0 4.32-2.86 8.62-6 9.93C8.86 19.62 6 15.32 6 11V6.43l6-2.25zm-1 9.41-1.71-1.7-1.41 1.41L11 16.41l5.12-5.12-1.41-1.41z" />
                                                                            </svg>
                                                                        </span>
                                                                        <div>
                                                                            <strong>Phần cứng chính hãng</strong>
                                                                            <span>Đầy đủ CO/CQ, sẵn sàng cho kiểm định
                                                                                doanh nghiệp.</span>
                                                                        </div>
                                                                    </div>
                                                                    <div class="home-usp-item">
                                                                        <span class="home-usp-icon" aria-hidden="true">
                                                                            <svg viewBox="0 0 24 24">
                                                                                <path
                                                                                    d="M12 1a7 7 0 0 0-7 7v3.59L3.29 13.3a1 1 0 0 0-.29.7V19a2 2 0 0 0 2 2h3v-7H5V8a5 5 0 0 1 10 0v6h-3v7h7a2 2 0 0 0 2-2v-5a1 1 0 0 0-.29-.7L19 11.59V8a7 7 0 0 0-7-7z" />
                                                                            </svg>
                                                                        </span>
                                                                        <div>
                                                                            <strong>Bảo hành nhanh</strong>
                                                                            <span>Hỗ trợ ngày làm việc kế tiếp cho hệ
                                                                                thống quan trọng.</span>
                                                                        </div>
                                                                    </div>
                                                                    <div class="home-usp-item">
                                                                        <span class="home-usp-icon" aria-hidden="true">
                                                                            <svg viewBox="0 0 24 24">
                                                                                <path
                                                                                    d="M20 8h-3V4H1v13h2a3 3 0 0 0 6 0h6a3 3 0 0 0 6 0h2v-5l-3-4zM6 18.5A1.5 1.5 0 1 1 7.5 17 1.5 1.5 0 0 1 6 18.5zM15 8v4h6.46l-2.25-3H15zm3 10.5a1.5 1.5 0 1 1 1.5-1.5 1.5 1.5 0 0 1-1.5 1.5z" />
                                                                            </svg>
                                                                        </span>
                                                                        <div>
                                                                            <strong>Giao hàng ưu tiên</strong>
                                                                            <span>Ưu tiên vận chuyển cho toàn bộ node hạ
                                                                                tầng.</span>
                                                                        </div>
                                                                    </div>
                                                                    <div class="home-usp-item">
                                                                        <span class="home-usp-icon" aria-hidden="true">
                                                                            <svg viewBox="0 0 24 24">
                                                                                <path
                                                                                    d="M12 1a11 11 0 1 0 11 11A11 11 0 0 0 12 1zm0 2a9 9 0 0 1 7.94 4.77l-1.7.98A7 7 0 0 0 5.76 8.75l-1.7-.98A9 9 0 0 1 12 3zm0 18a9 9 0 0 1-7.94-4.77l1.7-.98a7 7 0 0 0 12.48 0l1.7.98A9 9 0 0 1 12 21zm-3-9a3 3 0 1 0 3-3 3 3 0 0 0-3 3z" />
                                                                            </svg>
                                                                        </span>
                                                                        <div>
                                                                            <strong>Tư vấn kỹ thuật</strong>
                                                                            <span>Hỗ trợ chuyên sâu cho cấu hình RAID và
                                                                                ZFS.</span>
                                                                        </div>
                                                                    </div>
                                                                </section>

                                                                <section id="categories" class="home-categories">
                                                                    <div class="home-section-head">
                                                                        <h2>Danh mục sản phẩm</h2>
                                                                        <a href="${pageContext.request.contextPath}/product<%= adminView ? "?admin=1" : "" %>#all-hardware">Xem tất cả</a>
                                                                    </div>
                                                                    <div class="home-category-grid">
                                                                        <% for (java.util.Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                                                                            String categoryKey = entry.getKey();
                                                                            int categoryCount = entry.getValue();
                                                                            String categoryLabel;
                                                                            if ("STORAGE_DEVICE".equalsIgnoreCase(categoryKey)) {
                                                                                categoryLabel = "Thiết bị lưu trữ";
                                                                            } else if ("NETWORK_DEVICE".equalsIgnoreCase(categoryKey)) {
                                                                                categoryLabel = "Thiết bị mạng";
                                                                            } else if ("ACCESSORY".equalsIgnoreCase(categoryKey)) {
                                                                                categoryLabel = "Phụ kiện";
                                                                            } else {
                                                                                categoryLabel = categoryKey.replace('_', ' ');
                                                                            }
                                                                        %>
                                                                            <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=<%= categoryKey %><%= adminView ? "&admin=1" : "" %>#all-hardware">
                                                                                <span class="home-category-name"><%= categoryLabel %></span>
                                                                                <span class="home-category-count"><%= categoryCount %> sản phẩm</span>
                                                                            </a>
                                                                        <% } %>
                                                                    </div>
                                                                </section>

                                                                <section id="featured" class="home-featured">
                                                                    <div class="home-section-head">
                                                                        <h2>Sản phẩm nổi bật</h2>
                                                                        <a href="#all-hardware">Xem tất cả</a>
                                                                    </div>
                                                                    <div class="home-featured-slider">
                                                                        <button type="button" class="home-featured-nav prev" aria-label="Xem sản phẩm trước">‹</button>
                                                                        <div class="home-featured-track" id="featured-track">
                                                                            <% for (ProductCardView card : featuredProducts) { %>
                                                                                <article class="home-product-card featured-card" role="link" tabindex="0" onclick="window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'}">
                                                                                    <div class="home-product-img-wrap">
                                                                                        <img class="home-product-img"
                                                                                            src="<%= card.getImageUrl() %>"
                                                                                            alt="device"
                                                                                            loading="lazy"
                                                                                            decoding="async">
                                                                                    </div>
                                                                                    <div class="home-product-content">
                                                                                        <h3><a
                                                                                                href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>">
                                                                                                <%= card.getName() %>
                                                                                            </a></h3>
                                                                                        <div class="product-extra">
                                                                                            <span>Thương hiệu: <strong><%= card.getBrandName() %></strong></span>
                                                                                            <span>Tồn kho: <strong><%= card.getTotalQuantity() %></strong></span>
                                                                                            <span>Đánh giá: <strong><%= String.format(java.util.Locale.US, "%.1f", card.getRating()) %></strong> ★</span>
                                                                                        </div>
                                                                                        <p class="home-price">
                                                                                            <%= card.getPriceText() %>
                                                                                        </p>
                                                                                        <div class="home-actions">
                                                                                            <button type="button"
                                                                                                class="btn-buy js-open-cart-popup"
                                                                                                onclick="event.stopPropagation();"
                                                                                                data-action="buyNow"
                                                                                                data-product-id="<%= card.getId() %>"
                                                                                                data-name="<%= card.getName() %>"
                                                                                                data-category="<%= card.getCategory() %>"
                                                                                                data-brand-id="<%= card.getBrandId() %>"
                                                                                                data-variants='<%= card.getVariantsJson() %>'>
                                                                                                MUA NGAY</button>
                                                                                            <button type="button"
                                                                                                class="btn-cart js-open-cart-popup"
                                                                                                onclick="event.stopPropagation();"
                                                                                                data-action="addCart"
                                                                                                data-product-id="<%= card.getId() %>"
                                                                                                data-name="<%= card.getName() %>"
                                                                                                data-category="<%= card.getCategory() %>"
                                                                                                data-brand-id="<%= card.getBrandId() %>"
                                                                                                data-variants='<%= card.getVariantsJson() %>'>
                                                                                                + GIỎ HÀNG
                                                                                            </button>
                                                                                        </div>
                                                                                    </div>
                                                                                </article>
                                                                                <% } %>
                                                                        </div>
                                                                        <button type="button" class="home-featured-nav next" aria-label="Xem sản phẩm tiếp">›</button>
                                                                    </div>
                                                                </section>

                                                                <% if (!q.trim().isEmpty() || (selectedCategory != null && !selectedCategory.trim().isEmpty())) { %>
                                                                <section class="home-result">
                                                                    <span>KẾT QUẢ: Tìm thấy <strong>
                                                                            <%= matchedCount %>
                                                                        </strong> sản phẩm cho "<%= filterLabel %>
                                                                            "</span>
                                                                    <span
                                                                        style="opacity: 0.5; font-family: monospace;">trạng
                                                                        thái: OK</span>
                                                                </section>
                                                                <% } %>

                                                                <section id="all-hardware" class="home-product-list">
                                                                    <div class="home-section-head">
                                                                        <h2>Toàn bộ sản phẩm</h2>
                                                                    </div>
                                                                    <div class="home-product-grid">
                                                                        <% if (!cards.isEmpty()) { for (ProductCardView
                                                                            card : cards) { if (!normalizedKeyword.isEmpty()) {
                                                                            String name=card.getName()==null ? "" :
                                                                            card.getName().toLowerCase(); String
                                                                            category=card.getCategory()==null ? "" :
                                                                            card.getCategory().toLowerCase(); if
                                                                            (!name.contains(normalizedKeyword) &&
                                                                            !category.contains(normalizedKeyword)) { continue; } }
                                                                            %>
                                                                            <article class="home-product-card" role="link" tabindex="0" onclick="window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'}">
                                                                                <div class="home-product-img-wrap">
                                                                                    <img class="home-product-img"
                                                                                        src="<%= card.getImageUrl() %>"
                                                                                        alt="device"
                                                                                        loading="lazy"
                                                                                        decoding="async">
                                                                                </div>
                                                                                <div class="home-product-content">
                                                                                    <h3><a
                                                                                            href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>">
                                                                                            <%= card.getName() %>
                                                                                        </a></h3>
                                                                                    <div class="product-extra">
                                                                                        <span>Thương hiệu: <strong><%= card.getBrandName() %></strong></span>
                                                                                        <span>Tồn kho: <strong><%= card.getTotalQuantity() %></strong></span>
                                                                                        <span>Đánh giá: <strong><%= String.format(java.util.Locale.US, "%.1f", card.getRating()) %></strong> ★</span>
                                                                                    </div>
                                                                                    <p class="home-price">
                                                                                        <%= card.getPriceText() %>
                                                                                    </p>
                                                                                    <div class="home-actions">
                                                                                        <button type="button"
                                                                                            class="btn-buy js-open-cart-popup"
                                                                                            onclick="event.stopPropagation();"
                                                                                            data-action="buyNow"
                                                                                            data-product-id="<%= card.getId() %>"
                                                                                            data-name="<%= card.getName() %>"
                                                                                            data-category="<%= card.getCategory() %>"
                                                                                            data-brand-id="<%= card.getBrandId() %>"
                                                                                            data-variants='<%= card.getVariantsJson() %>'>
                                                                                            MUA NGAY</button>
                                                                                        <button type="button"
                                                                                            class="btn-cart js-open-cart-popup"
                                                                                            onclick="event.stopPropagation();"
                                                                                            data-action="addCart"
                                                                                            data-product-id="<%= card.getId() %>"
                                                                                            data-name="<%= card.getName() %>"
                                                                                            data-category="<%= card.getCategory() %>"
                                                                                            data-brand-id="<%= card.getBrandId() %>"
                                                                                            data-variants='<%= card.getVariantsJson() %>'>
                                                                                            + GIỎ HÀNG
                                                                                        </button>
                                                                                    </div>
                                                                                </div>
                                                                            </article>
                                                                            <% } } %>
                                                                    </div>
                                                                </section>

                                                                <!-- High Impact Yellow CTA Band -->
                                                                <section class="cta-band-yellow">
                                                                    <h2>Triển khai hạ tầng của bạn ngay hôm nay.</h2>
                                                                    <p style="margin-bottom: 32px; font-size: 18px;">
                                                                        Tham gia cùng hàng ngàn kỹ sư đang vận hành trên
                                                                        phần cứng LinhNamStore.</p>
                                                                    <a href="#all-hardware" class="btn-black">MUA SẮM
                                                                        NGAY</a>
                                                                </section>

                                                                <section class="home-usp-bottom">
                                                                    <div>Lưu trữ toàn diện</div>
                                                                    <div>An toàn & bền bỉ</div>
                                                                    <div>Tối ưu chi phí vận hành</div>
                                                                    <div>Sẵn sàng 24/7</div>
                                                                </section>
                                                    </main>

                                                    <% if (cartAdded) { %>
                                                    <div id="cartAddedToast" style="position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(22,163,74,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);transition:opacity .35s ease,transform .35s ease;">Thêm vào giỏ hàng thành công</div>
                                                    <% } %>

                                                    <div id="cartVariantModal" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.72);z-index:3000;align-items:center;justify-content:center;padding:16px;">
                                                        <div style="width:min(640px,96vw);background:#121317;border:1px solid #2a2f3a;border-radius:14px;padding:16px;">
                                                            <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;">
                                                                <h3 id="cartVariantTitle" style="margin:0;font-size:20px;">Chọn phân loại</h3>
                                                                <button type="button" id="cartVariantClose" class="btn-cart" style="width:auto;padding:8px 12px;">Đóng</button>
                                                            </div>
                                                            <div style="display:grid;grid-template-columns:130px 1fr;gap:14px;margin-top:12px;align-items:start;">
                                                                <img id="cartVariantImage" src="" alt="variant" style="width:130px;height:130px;object-fit:cover;border:1px solid #2a2f3a;border-radius:10px;background:#0c0d10;">
                                                                <div>
                                                                    <div id="cartVariantList" style="display:flex;flex-wrap:wrap;gap:8px;"></div>
                                                                    <p id="cartVariantPrice" style="margin:12px 0 6px;color:#f5f768;font-weight:800;font-size:24px;">0 VND</p>
                                                                    <p id="cartVariantStock" style="margin:0 0 12px;color:#d1d5db;">Tồn kho: 0</p>
                                                                    <label style="display:block;font-size:14px;color:#d1d5db;margin-bottom:6px;">Số lượng</label>
                                                                    <div style="display:flex;align-items:center;gap:8px;max-width:176px;">
                                                                        <input id="cartVariantQty" type="number" min="1" value="1" style="flex:1;height:32px;border-radius:8px;border:1px solid #2a2f3a;background:#0c0d10;color:#fff;padding:0 8px;font-size:15px;line-height:32px;text-align:center;text-align-last:center;appearance:textfield;-moz-appearance:textfield;">
                                                                        <div style="display:flex;flex-direction:column;gap:4px;">
                                                                            <button type="button" id="cartQtyPlus" class="btn-cart" style="width:32px;height:32px;min-height:32px;padding:0;font-size:18px;line-height:1;display:flex;align-items:center;justify-content:center;">+</button>
                                                                            <button type="button" id="cartQtyMinus" class="btn-cart" style="width:32px;height:32px;min-height:32px;padding:0;font-size:18px;line-height:1;display:flex;align-items:center;justify-content:center;">-</button>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <form id="popupActionForm" method="post" action="${pageContext.request.contextPath}/cart?action=add" style="margin-top:14px;">
                                                                <input type="hidden" name="source" value="buyNow">
                                                                <input type="hidden" name="productId">
                                                                <input type="hidden" name="name">
                                                                <input type="hidden" name="category">
                                                                <input type="hidden" name="brandId">
                                                                <input type="hidden" name="imageUrl">
                                                                <input type="hidden" name="priceValue">
                                                                <input type="hidden" name="stock">
                                                                <input type="hidden" name="quantity">
                                                                <input type="hidden" name="variantId">
                                                                <input type="hidden" name="sku">
                                                                <button type="submit" id="popupSubmitBtn" class="btn-buy" style="width:100%;height:48px;font-size:19px;font-weight:900;font-family:&quot;Inter&quot;,&quot;Segoe UI&quot;,Arial,sans-serif;text-transform:uppercase;letter-spacing:.6px;">THÊM VÀO GIỎ</button>
                                                            </form>
                                                        </div>
                                                    </div>

                                                    <script>
                                                        (function () {
                                                            const track = document.getElementById('featured-track');
                                                            if (!track) return;

                                                            const slider = track.closest('.home-featured-slider');
                                                            const prevBtn = slider ? slider.querySelector('.home-featured-nav.prev') : null;
                                                            const nextBtn = slider ? slider.querySelector('.home-featured-nav.next') : null;

                                                            const getStep = function () {
                                                                const card = track.querySelector('.featured-card');
                                                                if (!card) return 320;
                                                                const style = window.getComputedStyle(track);
                                                                const gap = parseFloat(style.columnGap || style.gap || '16') || 16;
                                                                return card.getBoundingClientRect().width + gap;
                                                            };

                                                            const updateButtons = function () {
                                                                if (!prevBtn || !nextBtn) return;
                                                                const maxScroll = track.scrollWidth - track.clientWidth;
                                                                prevBtn.disabled = track.scrollLeft <= 2;
                                                                nextBtn.disabled = track.scrollLeft >= maxScroll - 2;
                                                            };

                                                            if (prevBtn) {
                                                                prevBtn.addEventListener('click', function () {
                                                                    track.scrollBy({ left: -getStep(), behavior: 'smooth' });
                                                                });
                                                            }

                                                            if (nextBtn) {
                                                                nextBtn.addEventListener('click', function () {
                                                                    track.scrollBy({ left: getStep(), behavior: 'smooth' });
                                                                });
                                                            }

                                                            track.addEventListener('scroll', updateButtons, { passive: true });
                                                            window.addEventListener('resize', updateButtons);
                                                            updateButtons();
                                                        })();

                                                        (function () {
                                                            const hasQuery = "<%= q.trim() %>".length > 0;
                                                            const hasCategoryFilter = "<%= selectedCategory == null ? "" : selectedCategory.trim() %>".length > 0;
                                                            if (!hasQuery && !hasCategoryFilter) return;
                                                            const target = document.getElementById('all-hardware');
                                                            if (target) {
                                                                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                                            }
                                                        })();

                                                        (function () {
                                                            window.toggleAccountMenu = function (button) {
                                                                const wrapper = button.closest('.home-account-menu');
                                                                if (!wrapper) return;
                                                                const dropdown = wrapper.querySelector('.home-account-dropdown');
                                                                if (!dropdown) return;
                                                                const isOpen = dropdown.style.display === 'block';
                                                                document.querySelectorAll('.home-account-dropdown').forEach((el) => { el.style.display = 'none'; });
                                                                dropdown.style.display = isOpen ? 'none' : 'block';
                                                            };

                                                            document.addEventListener('click', function (event) {
                                                                if (!event.target.closest('.home-account-menu')) {
                                                                    document.querySelectorAll('.home-account-dropdown').forEach((el) => { el.style.display = 'none'; });
                                                                }
                                                            });

                                                            const modal = document.getElementById('cartVariantModal');
                                                            const closeBtn = document.getElementById('cartVariantClose');
                                                            const titleEl = document.getElementById('cartVariantTitle');
                                                            const listEl = document.getElementById('cartVariantList');
                                                            const priceEl = document.getElementById('cartVariantPrice');
                                                            const stockEl = document.getElementById('cartVariantStock');
                                                            const qtyEl = document.getElementById('cartVariantQty');
                                                            const qtyPlusBtn = document.getElementById('cartQtyPlus');
                                                            const qtyMinusBtn = document.getElementById('cartQtyMinus');
                                                            const imageEl = document.getElementById('cartVariantImage');
                                                            const form = document.getElementById('popupActionForm');
                                                            const submitBtn = document.getElementById('popupSubmitBtn');
                                                            const openButtons = document.querySelectorAll('.js-open-cart-popup');
                                                            const headerCartCount = document.getElementById('headerCartCount');

                                                            let activeProduct = null;
                                                            let activeVariant = null;
                                                            let activeAction = 'addCart';

                                                            const setField = (name, value) => {
                                                                const input = form.querySelector('input[name="' + name + '"]');
                                                                if (input) input.value = value == null ? '' : value;
                                                            };

                                                            const formatVnd = (value) => {
                                                                try { return Number(value || 0).toLocaleString('en-US') + ' VND'; }
                                                                catch (e) { return (value || 0) + ' VND'; }
                                                            };

                                                            const chooseVariant = (variant, chip) => {
                                                                activeVariant = variant;
                                                                listEl.querySelectorAll('button').forEach(btn => {
                                                                    btn.classList.remove('is-active');
                                                                });
                                                                if (chip) {
                                                                    chip.classList.add('is-active');
                                                                }

                                                                const stock = Number(variant.stock || 0);
                                                                priceEl.textContent = formatVnd(variant.priceValue || 0);
                                                                stockEl.textContent = 'Tồn kho: ' + stock;
                                                                imageEl.src = variant.imageUrl || '';
                                                                qtyEl.min = 1;
                                                                qtyEl.max = Math.max(stock, 1);
                                                                qtyEl.value = stock > 0 ? 1 : 1;
                                                            };

                                                            const resolveProductId = (button) => {
                                                                const direct = (button.dataset && button.dataset.productId) ? button.dataset.productId : (button.getAttribute('data-product-id') || '');
                                                                if (direct && direct.trim() !== '') return direct.trim();
                                                                const card = button.closest('.home-product-card');
                                                                const link = card ? card.querySelector('a[href*="/product?id="]') : null;
                                                                if (!link) return '';
                                                                try {
                                                                    const url = new URL(link.href, window.location.origin);
                                                                    return (url.searchParams.get('id') || '').trim();
                                                                } catch (e) {
                                                                    return '';
                                                                }
                                                            };

                                                            const openModal = (button) => {
                                                                activeAction = button.getAttribute('data-action') || 'addCart';
                                                                const raw = button.getAttribute('data-variants') || '[]';
                                                                let variants = [];
                                                                try { variants = JSON.parse(raw); } catch (e) { variants = []; }
                                                                if (!Array.isArray(variants) || variants.length === 0) {
                                                                    alert('Sản phẩm này chưa có phân loại để thao tác.');
                                                                    return;
                                                                }

                                                                activeProduct = {
                                                                    productId: resolveProductId(button),
                                                                    name: (button.dataset && button.dataset.name) ? button.dataset.name : (button.getAttribute('data-name') || ''),
                                                                    category: (button.dataset && button.dataset.category) ? button.dataset.category : (button.getAttribute('data-category') || ''),
                                                                    brandId: (button.dataset && button.dataset.brandId) ? button.dataset.brandId : (button.getAttribute('data-brand-id') || '')
                                                                };

                                                                titleEl.textContent = activeProduct.name || 'Chọn phân loại';
                                                                submitBtn.textContent = activeAction === 'buyNow' ? 'MUA NGAY' : 'THÊM VÀO GIỎ';
                                                                listEl.innerHTML = '';

                                                                let defaultVariant = variants.find(v => Number(v.stock || 0) > 0) || variants[0];

                                                                variants.forEach((variant) => {
                                                                    const chip = document.createElement('button');
                                                                    chip.type = 'button';
                                                                    chip.className = 'btn-cart';
                                                                    chip.style.width = 'auto';
                                                                    chip.style.padding = '8px 12px';
                                                                    chip.style.transition = 'all .16s ease';
                                                                    if (Number(variant.stock || 0) <= 0) {
                                                                        chip.classList.add('is-disabled');
                                                                    }
                                                                    chip.textContent = variant.sku || (variant.variantId || 'Mặc định');
                                                                    chip.addEventListener('click', () => chooseVariant(variant, chip));
                                                                    listEl.appendChild(chip);
                                                                    if (variant === defaultVariant) {
                                                                        chooseVariant(variant, chip);
                                                                    }
                                                                });

                                                                modal.style.display = 'flex';
                                                            };

                                                            openButtons.forEach((button) => {
                                                                button.addEventListener('click', () => openModal(button));
                                                            });

                                                            closeBtn.addEventListener('click', () => { modal.style.display = 'none'; });
                                                            modal.addEventListener('click', (e) => {
                                                                if (e.target === modal) modal.style.display = 'none';
                                                            });

                                                            qtyPlusBtn.addEventListener('click', () => {
                                                                const max = Number(qtyEl.max || 1);
                                                                let qty = Number(qtyEl.value || 1);
                                                                qty = Math.min(max, qty + 1);
                                                                qtyEl.value = qty;
                                                            });

                                                            qtyMinusBtn.addEventListener('click', () => {
                                                                let qty = Number(qtyEl.value || 1);
                                                                qty = Math.max(1, qty - 1);
                                                                qtyEl.value = qty;
                                                            });

                                                            const showCartToast = function () {
                                                                const oldToast = document.getElementById('cartAddedToast');
                                                                if (oldToast && oldToast.parentNode) {
                                                                    oldToast.parentNode.removeChild(oldToast);
                                                                }
                                                                const toast = document.createElement('div');
                                                                toast.id = 'cartAddedToast';
                                                                toast.style.cssText = 'position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(22,163,74,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);transition:opacity .35s ease,transform .35s ease;';
                                                                toast.textContent = 'Thêm vào giỏ hàng thành công';
                                                                document.body.appendChild(toast);

                                                                let dismissed = false;
                                                                const dismiss = function () {
                                                                    if (dismissed || !toast.parentNode) return;
                                                                    dismissed = true;
                                                                    toast.style.opacity = '0';
                                                                    toast.style.transform = 'translateX(-50%) translateY(-8px)';
                                                                    window.removeEventListener('pointerdown', onPointerDown, true);
                                                                    setTimeout(function () {
                                                                        if (toast.parentNode) toast.parentNode.removeChild(toast);
                                                                    }, 360);
                                                                };
                                                                const onPointerDown = function () { dismiss(); };
                                                                window.addEventListener('pointerdown', onPointerDown, true);
                                                                setTimeout(dismiss, 1000);
                                                            };

                                                            form.addEventListener('submit', async (e) => {
                                                                if (!activeProduct || !activeVariant) {
                                                                    e.preventDefault();
                                                                    return;
                                                                }
                                                                const stock = Number(activeVariant.stock || 0);
                                                                let qty = Number(qtyEl.value || 1);
                                                                if (!Number.isFinite(qty) || qty < 1) qty = 1;
                                                                if (stock > 0) qty = Math.min(qty, stock);

                                                                if (stock <= 0) {
                                                                    e.preventDefault();
                                                                    alert('Phân loại này đã hết hàng. Vui lòng chọn phân loại khác.');
                                                                    return;
                                                                }

                                                                const resolvedProductId = (activeProduct.productId || '').trim() || (activeVariant.productId || '').trim();
                                                                const resolvedVariantId = (activeVariant.variantId || '').trim();
                                                                if (!resolvedVariantId) {
                                                                    e.preventDefault();
                                                                    alert('Không xác định được phân loại sản phẩm. Vui lòng chọn lại.');
                                                                    return;
                                                                }

                                                                const payload = {
                                                                    source: 'buyNow',
                                                                    productId: resolvedProductId,
                                                                    variantId: resolvedVariantId,
                                                                    quantity: String(qty),
                                                                    name: activeProduct.name || '',
                                                                    category: activeProduct.category || '',
                                                                    brandId: activeProduct.brandId || '',
                                                                    imageUrl: activeVariant.imageUrl || '',
                                                                    priceValue: String(activeVariant.priceValue || 0),
                                                                    stock: String(stock),
                                                                    sku: activeVariant.sku || ''
                                                                };

                                                                console.log('[CART_POPUP_SUBMIT]', {
                                                                    action: activeAction,
                                                                    productId: payload.productId,
                                                                    variantId: payload.variantId,
                                                                    quantity: payload.quantity,
                                                                    stock: payload.stock,
                                                                    sku: payload.sku
                                                                });

                                                                if (activeAction === 'buyNow') {
                                                                    setField('source', payload.source);
                                                                    setField('productId', payload.productId);
                                                                    setField('variantId', payload.variantId);
                                                                    setField('quantity', payload.quantity);
                                                                    setField('name', payload.name);
                                                                    setField('category', payload.category);
                                                                    setField('brandId', payload.brandId);
                                                                    setField('imageUrl', payload.imageUrl);
                                                                    setField('priceValue', payload.priceValue);
                                                                    setField('stock', payload.stock);
                                                                    setField('sku', payload.sku);
                                                                    form.method = 'get';
                                                                    form.action = '${pageContext.request.contextPath}/payment';
                                                                    return;
                                                                }

                                                                e.preventDefault();
                                                                form.method = 'post';
                                                                form.action = '${pageContext.request.contextPath}/cart?action=add';

                                                                try {
                                                                    const formData = new URLSearchParams();
                                                                    Object.entries(payload).forEach(([key, value]) => formData.set(key, value == null ? '' : String(value)));
                                                                    const response = await fetch(form.action, {
                                                                        method: 'POST',
                                                                        headers: {
                                                                            'X-Requested-With': 'XMLHttpRequest',
                                                                            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                                                                        },
                                                                        body: formData.toString()
                                                                    });

                                                                    if (response.status === 401) {
                                                                        const unauthorized = await response.json();
                                                                        if (unauthorized && unauthorized.needLogin && unauthorized.loginUrl) {
                                                                            window.location.href = unauthorized.loginUrl;
                                                                            return;
                                                                        }
                                                                        throw new Error('Unauthorized');
                                                                    }

                                                                    const data = await response.json();
                                                                    if (!response.ok || !data || data.success !== true) {
                                                                        throw new Error((data && data.message) ? data.message : 'Không thể thêm vào giỏ lúc này.');
                                                                    }
                                                                    if (headerCartCount && typeof data.cartCount !== 'undefined') {
                                                                        headerCartCount.textContent = String(data.cartCount);
                                                                    }
                                                                    modal.style.display = 'none';
                                                                    showCartToast();
                                                                } catch (err) {
                                                                    alert(err && err.message ? err.message : 'Không thể thêm vào giỏ lúc này. Vui lòng thử lại.');
                                                                }
                                                            });
                                                        })();

                                                        (function () {
                                                            const toast = document.getElementById('cartAddedToast');
                                                            if (!toast) return;

                                                            let dismissed = false;
                                                            const dismiss = function () {
                                                                if (dismissed || !toast.parentNode) return;
                                                                dismissed = true;
                                                                toast.style.opacity = '0';
                                                                toast.style.transform = 'translateX(-50%) translateY(-8px)';
                                                                window.removeEventListener('pointerdown', onPointerDown, true);
                                                                setTimeout(function () {
                                                                    if (toast.parentNode) {
                                                                        toast.parentNode.removeChild(toast);
                                                                    }
                                                                }, 360);
                                                            };

                                                            const onPointerDown = function () {
                                                                dismiss();
                                                            };

                                                            window.addEventListener('pointerdown', onPointerDown, true);
                                                            setTimeout(dismiss, 1000);
                                                        })();
                                                    </script>
                    </body>

                    </html>


