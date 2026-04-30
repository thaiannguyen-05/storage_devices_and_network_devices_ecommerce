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
                        <title>StoreIT | Nền tảng thiết bị lưu trữ</title>
                        <link rel="preconnect" href="https://fonts.googleapis.com">
                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                        <link
                            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap"
                            rel="stylesheet">
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

                                List<ProductCardView> cards = (List<ProductCardView>)
                                        request.getAttribute("productCards");
                                        if (cards == null) cards = new ArrayList<>();

                                            List<ProductCardView> featuredProducts = (List<ProductCardView>)
                                                    request.getAttribute("featuredProducts");
                                                    if (featuredProducts == null || featuredProducts.isEmpty())
                                                    featuredProducts = cards;

                                                    int matchedCount = 0;
                                                    for (ProductCardView card : cards) {
                                                    if (!q.trim().isEmpty()) {
                                                    String keyword = q.trim().toLowerCase();
                                                    String name = card.getName() == null ? "" :
                                                    card.getName().toLowerCase();
                                                    String category = card.getCategory() == null ? "" :
                                                    card.getCategory().toLowerCase();
                                                    if (!name.contains(keyword) && !category.contains(keyword)) {
                                                    continue;
                                                    }
                                                    }
                                                    matchedCount++;
                                                    }
                                                    String filterLabel = !q.trim().isEmpty() ? q.trim() :
                                                    ((selectedCategory != null && !selectedCategory.isEmpty()) ?
                                                    selectedCategory : "tất cả");
                                                    %>

                                                    <header class="home-header">
                                                        <div class="home-header-top">
                                                            <a class="home-logo-wrap"
                                                                href="${pageContext.request.contextPath}/product">
                                                                <div class="home-logo-box">S</div>
                                                                <div class="home-logo-text">
                                                                    <strong>StoreIT</strong>
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
                                                                    <a href="${pageContext.request.contextPath}/auth?action=profile"
                                                                        class="home-login"><span
                                                                            class="home-inline-icon"
                                                                            aria-hidden="true"><svg viewBox="0 0 24 24"
                                                                                focusable="false">
                                                                                <path
                                                                                    d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z" />
                                                                            </svg></span>
                                                                        <%= authUserName %>
                                                                    </a>
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
                                                                                Giỏ hàng <span>
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
                                                                            alt="Storage Devices">
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
                                                                        <h2>Kiến trúc lưu trữ</h2>
                                                                        <a href="#all-hardware">Xem toàn bộ sản phẩm</a>
                                                                    </div>
                                                                    <div class="home-category-grid">
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=HDD<%= adminView ? "&admin=1" : "" %>">HDD CLUSTER</a>
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=SSD<%= adminView ? "&admin=1" : "" %>">SSD FLASH</a>
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=NAS<%= adminView ? "&admin=1" : "" %>">NAS NODES</a>
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=USB<%= adminView ? "&admin=1" : "" %>">USB MOBILE</a>
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=MEMORY<%= adminView ? "&admin=1" : "" %>">SD CARDS</a>
                                                                        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=ENCLOSURE<%= adminView ? "&admin=1" : "" %>">RACK BOXES</a>
                                                                    </div>
                                                                </section>

                                                                <section id="featured" class="home-featured">
                                                                    <div class="home-section-head">
                                                                        <h2>Sản phẩm nổi bật</h2>
                                                                    </div>
                                                                    <div class="home-product-grid">
                                                                        <% int
                                                                            featuredLimit=Math.min(featuredProducts.size(),
                                                                            8); for (int i=0; i < featuredLimit; i++) {
                                                                            ProductCardView
                                                                            card=featuredProducts.get(i); %>
                                                                            <article class="home-product-card">
                                                                                <div class="home-product-img-wrap">
                                                                                    <img class="home-product-img"
                                                                                        src="<%= card.getImageUrl() %>"
                                                                                        alt="device">
                                                                                </div>
                                                                                <div class="home-product-content">
                                                                                    <h3><a
                                                                                            href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>">
                                                                                            <%= card.getName() %>
                                                                                        </a></h3>
                                                                                    <div class="product-meta">
                                                                                        type: "<%= card.getCategory() %>
                                                                                            "<br>
                                                                                            id: "<%= card.getId() %>"
                                                                                    </div>
                                                                                    <p class="home-price">
                                                                                        <%= card.getPriceText() %>
                                                                                    </p>
                                                                                    <div class="home-actions">
                                                                                        <form method="get"
                                                                                            action="${pageContext.request.contextPath}/payment">
                                                                                            <input type="hidden"
                                                                                                name="productId"
                                                                                                value="<%= card.getId() %>">
                                                                                            <button type="submit"
                                                                                                class="btn-buy">MUA
                                                                                                NGAY</button>
                                                                                        </form>
                                                                                        <form method="post"
                                                                                            action="${pageContext.request.contextPath}/cart?action=add">
                                                                                            <input type="hidden"
                                                                                                name="productId"
                                                                                                value="<%= card.getId() %>">
                                                                                            <button type="submit"
                                                                                                class="btn-cart">+ GIỎ
                                                                                                HÀNG</button>
                                                                                        </form>
                                                                                    </div>
                                                                                </div>
                                                                            </article>
                                                                            <% } %>
                                                                    </div>
                                                                </section>

                                                                <section class="home-result">
                                                                    <span>KẾT QUẢ: Tìm thấy <strong>
                                                                            <%= matchedCount %>
                                                                        </strong> sản phẩm cho "<%= filterLabel %>
                                                                            "</span>
                                                                    <span
                                                                        style="opacity: 0.5; font-family: monospace;">trạng
                                                                        thái: OK</span>
                                                                </section>

                                                                <section id="all-hardware" class="home-product-list">
                                                                    <div class="home-section-head">
                                                                        <h2>Toàn bộ sản phẩm</h2>
                                                                    </div>
                                                                    <div class="home-product-grid">
                                                                        <% if (!cards.isEmpty()) { for (ProductCardView
                                                                            card : cards) { if (!q.trim().isEmpty()) {
                                                                            String keyword=q.trim().toLowerCase();
                                                                            String name=card.getName()==null ? "" :
                                                                            card.getName().toLowerCase(); String
                                                                            category=card.getCategory()==null ? "" :
                                                                            card.getCategory().toLowerCase(); if
                                                                            (!name.contains(keyword) &&
                                                                            !category.contains(keyword)) { continue; } }
                                                                            %>
                                                                            <article class="home-product-card">
                                                                                <div class="home-product-img-wrap">
                                                                                    <img class="home-product-img"
                                                                                        src="<%= card.getImageUrl() %>"
                                                                                        alt="device">
                                                                                </div>
                                                                                <div class="home-product-content">
                                                                                    <h3><a
                                                                                            href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>">
                                                                                            <%= card.getName() %>
                                                                                        </a></h3>
                                                                                    <div class="product-meta">
                                                                                        architecture: "<%=
                                                                                            card.getCategory() %>"<br>
                                                                                            stock_status: "<%= "ACTIVE"
                                                                                                .equalsIgnoreCase(card.getStatus())
                                                                                                ? "AVAILABLE"
                                                                                                : "OUT_OF_STOCK" %>"
                                                                                    </div>
                                                                                    <p class="home-price">
                                                                                        <%= card.getPriceText() %>
                                                                                    </p>
                                                                                    <div class="home-actions">
                                                                                        <form method="get"
                                                                                            action="${pageContext.request.contextPath}/payment">
                                                                                            <input type="hidden"
                                                                                                name="productId"
                                                                                                value="<%= card.getId() %>">
                                                                                            <button type="submit"
                                                                                                class="btn-buy">MUA
                                                                                                NGAY</button>
                                                                                        </form>
                                                                                        <form method="post"
                                                                                            action="${pageContext.request.contextPath}/cart?action=add">
                                                                                            <input type="hidden"
                                                                                                name="productId"
                                                                                                value="<%= card.getId() %>">
                                                                                            <button type="submit"
                                                                                                class="btn-cart">+ GIỎ
                                                                                                HÀNG</button>
                                                                                        </form>
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
                                                                        phần cứng StoreIT.</p>
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
                    </body>

                    </html>