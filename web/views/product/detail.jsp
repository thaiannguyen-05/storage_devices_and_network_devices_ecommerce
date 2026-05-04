<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="entity.ProductReviewEntity"%>
<%@page import="entity.ProductVariantEntity"%>
<%@page import="module.bussiness.product.dto.ProductCardView"%>
<%@page import="module.bussiness.product.dto.ProductDetailView"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Chi tiết sản phẩm | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
        <style>
            * { box-sizing: border-box; }
            body { margin: 0; }
            .topbar { display: none; }
            .topbar-inner { display: none; }
            .brand { font-size: 28px; font-weight: 800; }
            .link { color: #fff; text-decoration: none; border: 1px solid #4b5563; border-radius: 999px; padding: 8px 12px; font-weight: 700; }
            .page { max-width: 1440px; margin: 0 auto; padding: 12px 32px 56px; }
            .main { display: grid; grid-template-columns: 1fr 1.02fr; gap: 16px; }
            .card { background: var(--ch-surface-card); border: 1px solid var(--ch-hairline); border-radius: 12px; padding: 14px; }
            .main-img-wrap { width: 100%; height: 360px; overflow: hidden; border-radius: 10px; border: 1px solid var(--ch-hairline); background: #000; cursor: zoom-in; }
            .main-img { width: 100%; height: 360px; object-fit: contain; display: block; transition: transform 0.18s ease; transform-origin: center center; }
            .main-img-wrap:hover .main-img { transform: scale(1.45); }
            .thumbs { margin-top: 8px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
            .thumbs img { width: 100%; height: 62px; object-fit: cover; border-radius: 8px; border: 1px solid var(--ch-hairline); cursor: pointer; }
            .thumbs img.active { border-color: var(--ch-primary); }
            .name { margin: 0 0 6px; font-size: 24px; line-height: 1.25; }
            .rating { display: flex; align-items: center; gap: 8px; color: #fbbf24; font-size: 13px; margin-bottom: 8px; }
            .rating small { color: var(--ch-muted); font-size: 11px; }
            .price { font-size: 36px; color: var(--ch-primary); font-weight: 800; margin: 8px 0; }
            .meta { color: var(--ch-body-strong); margin: 4px 0; font-size: 16px; }
            .stock { color: #34d399; font-weight: 800; margin: 8px 0; font-size: 18px; }
            .variant-wrap { margin: 10px 0; }
            .variant-title { font-size: 15px; color: var(--ch-body-strong); margin-bottom: 6px; }
            .variant-list { display: flex; flex-wrap: wrap; gap: 8px; }
            .variant-chip {
                border: 1px solid var(--ch-hairline);
                background: var(--ch-surface-soft);
                color: var(--ch-ink);
                border-radius: 8px;
                padding: 8px 12px;
                font-weight: 700;
                cursor: pointer;
                font-size: 13px;
            }
            .variant-chip.active {
                border-color: var(--ch-primary);
                color: var(--ch-primary);
                background: rgba(250, 255, 105, 0.08);
            }
            .actions { margin-top: 14px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
            .btn { width: 100%; border: none; border-radius: 9px; padding: 0 14px; font-weight: 800; cursor: pointer; height: 68px; min-height: 68px; font-size: 24px; font-family: "Inter", sans-serif; display: inline-flex; align-items: center; justify-content: center; text-align: center; line-height: 1.1; }
            .btn-buy { background: var(--ch-primary); color: var(--ch-on-primary); border: 1px solid var(--ch-primary-active); }
            .btn-cart { background: transparent; color: var(--ch-ink); border: 1px solid var(--ch-hairline); }

            @media (max-width: 1280px) {
                .name { font-size: 30px; }
                .price { font-size: 46px; }
                .meta { font-size: 18px; }
                .stock { font-size: 20px; }
                .btn { font-size: 21px; height: 62px; min-height: 62px; }
            }
            .section-title { margin: 0 0 12px; font-size: 26px; }
            .desc { color: var(--ch-body); line-height: 1.65; white-space: pre-wrap; font-size: 16px; }
            .sep { height: 20px; }

            .related-slider {
                position: relative;
                margin: 0 -22px;
                padding: 0 22px;
                overflow: visible;
            }
            .related-track {
                display: flex;
                gap: 16px;
                overflow-x: auto;
                scroll-behavior: smooth;
                scrollbar-width: none;
                -ms-overflow-style: none;
                padding-bottom: 4px;
            }
            .related-track::-webkit-scrollbar { display: none; }
            .related-track .home-product-card {
                flex: 0 0 calc((100% - 5 * 16px) / 6);
                min-width: 220px;
            }
            .related-nav {
                position: absolute;
                top: 50%;
                transform: translateY(-50%);
                width: 42px;
                height: 42px;
                border-radius: 9999px;
                border: 1px solid var(--ch-hairline);
                background: rgba(20, 20, 20, 0.92);
                color: var(--ch-ink);
                font-size: 28px;
                line-height: 1;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                z-index: 4;
            }
            .related-nav.prev { left: -16px; }
            .related-nav.next { right: -16px; }
            .related-nav:disabled { opacity: 0.35; cursor: default; }
            .related-nav:hover { border-color: var(--ch-primary); color: var(--ch-primary); }

            .review-summary { display: flex; align-items: center; gap: 16px; padding: 16px; border: 1px solid var(--ch-hairline); border-radius: 10px; margin-bottom: 12px; }
            .review-score { font-size: 48px; color: #f59e0b; font-weight: 800; }
            .review-list .review-item { border-top: 1px solid var(--ch-hairline); padding: 14px 0; }
            .review-list .review-item:first-child { border-top: none; }
            .review-name { font-weight: 700; font-size: 18px; }
            .review-stars { color: #f59e0b; font-size: 18px; }
            .review-meta { color: var(--ch-muted); font-size: 14px; margin-top: 3px; }
            .review-comment { margin-top: 6px; color: var(--ch-body); white-space: pre-wrap; font-size: 16px; }
            .review-form { margin-top: 14px; border: 1px solid var(--ch-hairline); border-radius: 10px; padding: 14px; }
            .review-form input, .review-form textarea, .review-form select { width: 100%; margin-top: 6px; margin-bottom: 10px; padding: 10px 12px; border: 1px solid var(--ch-hairline); border-radius: 8px; background: var(--ch-surface-soft); color: var(--ch-ink); font-size: 16px; font-family: "Inter", sans-serif; }
            .review-form label { font-size: 16px; color: var(--ch-ink); }
            .review-form button { border: none; border-radius: 8px; background: var(--ch-primary); color: var(--ch-on-primary); padding: 10px 16px; font-weight: 700; cursor: pointer; font-size: 20px; font-family: "Inter", sans-serif; letter-spacing: 0; }
            .err { background: #3a1b1f; border: 1px solid #7f1d1d; color: #fecaca; padding: 8px 10px; border-radius: 8px; margin: 8px 0; }
            .img-modal {
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.88);
                display: none;
                align-items: center;
                justify-content: center;
                z-index: 2000;
                padding: 20px;
            }
            .img-modal.show { display: flex; }
            .img-modal img {
                max-width: min(96vw, 1300px);
                max-height: 92vh;
                border-radius: 10px;
                border: 1px solid #374151;
            }
            .img-close {
                position: absolute;
                top: 16px;
                right: 18px;
                border: 1px solid #4b5563;
                background: #111827;
                color: #fff;
                border-radius: 8px;
                padding: 8px 12px;
                cursor: pointer;
                font-weight: 700;
            }

            @media (max-width: 900px) {
                .main { grid-template-columns: 1fr; }
                .main-img-wrap, .main-img { height: 300px; }
            }
        </style>
    </head>
    <body>
        <%
            ProductDetailView p = (ProductDetailView) request.getAttribute("productDetail");
            Integer cartCount = (Integer) request.getAttribute("cartCount");
            int count = cartCount == null ? 0 : cartCount;
            List<String> gallery = (List<String>) request.getAttribute("galleryImages");
            List<ProductVariantEntity> variants = (List<ProductVariantEntity>) request.getAttribute("productVariants");
            List<ProductCardView> related = (List<ProductCardView>) request.getAttribute("relatedProducts");
            List<ProductReviewEntity> reviews = (List<ProductReviewEntity>) request.getAttribute("reviews");
            String reviewError = (String) request.getAttribute("reviewError");
            DateTimeFormatter reviewDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm");
        %>
        <header class="home-header">
            <div class="home-header-top">
                <a class="home-logo-wrap" href="${pageContext.request.contextPath}/product">
                    <div class="home-logo-box">S</div>
                    <div class="home-logo-text">
                        <strong>StoreIT</strong>
                        <span>High Performance</span>
                    </div>
                </a>

                <a class="home-category-btn" href="${pageContext.request.contextPath}/product#categories">
                    <span class="home-menu-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" focusable="false">
                            <path d="M4 7h16a1 1 0 1 0 0-2H4a1 1 0 0 0 0 2zm16 4H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2zm0 6H4a1 1 0 1 0 0 2h16a1 1 0 1 0 0-2z"/>
                        </svg>
                    </span>
                    Danh mục
                </a>

                <form class="home-search" method="get" action="${pageContext.request.contextPath}/product">
                    <input type="text" name="q" placeholder="Tìm theo tên sản phẩm hoặc danh mục..." value="">
                    <button type="submit" aria-label="Tìm kiếm">
                        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <path d="M10.5 3a7.5 7.5 0 1 0 4.76 13.3l4.22 4.22a1 1 0 0 0 1.41-1.41l-4.22-4.22A7.5 7.5 0 0 0 10.5 3zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11z"/>
                        </svg>
                    </button>
                </form>

                <div class="home-header-right">
                    <div class="home-hotline">
                        <span class="home-inline-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24" focusable="false">
                                <path d="M6.62 10.79a15.05 15.05 0 0 0 6.59 6.59l2.2-2.2a1 1 0 0 1 1.01-.24c1.11.37 2.3.56 3.53.56a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1C10.3 21 3 13.7 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.23.19 2.42.56 3.53a1 1 0 0 1-.24 1.01l-2.2 2.25z"/>
                            </svg>
                        </span>
                        HOTLINE <b>1900 9999</b>
                    </div>
                    <%
                        Object authUserName = session.getAttribute("authUserName");
                        if (authUserName != null) {
                    %>
                    <div class="home-account-menu" style="position:relative;">
                        <button type="button" class="home-login" style="background:none;" onclick="toggleAccountMenu(this)">
                            <span class="home-inline-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z"/></svg></span><%= authUserName %>
                        </button>
                        <div class="home-account-dropdown" style="display:none;position:absolute;top:calc(100% + 10px);right:0;min-width:180px;background:#111318;border:1px solid #272a31;border-radius:12px;padding:8px;box-shadow:0 16px 32px rgba(0,0,0,.35);z-index:120;">
                            <a href="${pageContext.request.contextPath}/auth?action=profile" style="display:block;padding:10px 12px;border-radius:8px;color:#f4f4f5;text-decoration:none;font-weight:700;">Trang cá nhân</a>
                            <a href="${pageContext.request.contextPath}/auth?action=logout" style="display:block;padding:10px 12px;border-radius:8px;color:#ef4444;text-decoration:none;font-weight:700;">Đăng xuất</a>
                        </div>
                    </div>
                    <%
                        } else {
                    %>
                    <a href="${pageContext.request.contextPath}/auth?action=signin" class="home-login"><span class="home-inline-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z"/></svg></span>Tài khoản</a>
                    <%
                        }
                    %>
                    <a class="home-cart" href="${pageContext.request.contextPath}/cart">
                        <span class="home-inline-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24" focusable="false">
                                <path d="M7 18a2 2 0 1 0 2 2 2 2 0 0 0-2-2zm10 0a2 2 0 1 0 2 2 2 2 0 0 0-2-2zM7.16 14h9.59a2 2 0 0 0 1.95-1.57L20 6H6.21l-.27-1.37A1 1 0 0 0 4.96 4H3a1 1 0 1 0 0 2h1.14l2.03 10.17A3 3 0 0 0 9.11 19H19a1 1 0 1 0 0-2H9.11a1 1 0 0 1-.98-.8L8 15h-.84z"/>
                            </svg>
                        </span>
                        Giỏ hàng <span><%= count %></span>
                    </a>
                </div>
            </div>
        </header>

        <div class="page">
            <% if (p != null) { %>
            <section class="main">
                <div class="card">
                    <div class="main-img-wrap" onmousemove="zoomByMouse(event)" onmouseleave="resetZoom()" onclick="openImageModal()">
                        <img id="mainImg" class="main-img" src="<%= gallery.get(0) %>" alt="product">
                    </div>
                    <div class="thumbs">
                        <% for (int i = 0; i < gallery.size(); i++) { %>
                            <img class="<%= i == 0 ? "active" : "" %>" src="<%= gallery.get(i) %>" alt="thumb" onclick="swapImage(this)">
                        <% } %>
                    </div>
                </div>

                <div class="card">
                    <h1 class="name"><%= p.getName() %></h1>
                    <div class="rating">
                        <span>★★★★★</span>
                        <small><%= String.format("%.1f", p.getRating()) %> | <%= p.getReviewCount() %> đánh giá</small>
                    </div>
                    <p class="price" id="detailPriceText"><%= p.getPriceText() %></p>
                    <p class="meta">Danh mục: <strong><%= p.getCategory() %></strong></p>
                    <p class="meta">Thương hiệu: <strong><%= p.getBrandName() %></strong></p>
                    <p class="meta">Trạng thái: <strong><%= "ACTIVE".equalsIgnoreCase(p.getStatus()) ? "Còn hàng" : "Ngừng bán" %></strong></p>
                    <p class="stock" id="detailStockText">Số lượng còn: <%= p.getQuantity() %></p>

                    <% if (variants != null && !variants.isEmpty()) { %>
                    <div class="variant-wrap">
                        <div class="variant-title">Phân loại / Dung lượng</div>
                        <div class="variant-list" id="variantList">
                            <% for (int i = 0; i < variants.size(); i++) {
                                   ProductVariantEntity v = variants.get(i);
                                   String sku = v.getSku() == null ? "" : v.getSku();
                                   String normalizedSku = sku.toUpperCase().replace('_', '-');
                                   String label = sku;
                                   java.util.regex.Matcher sizeMatcher = java.util.regex.Pattern.compile("(\\d+)(TB|GB)?(?:-[A-Z0-9]+)?$").matcher(normalizedSku);
                                   if (sizeMatcher.find()) {
                                       String number = sizeMatcher.group(1);
                                       String unit = sizeMatcher.group(2);
                                       label = unit == null ? (number + "GB") : (number + unit);
                                   }
                                   String image = v.getImageUrl() == null ? "" : v.getImageUrl();
                            %>
                            <button type="button"
                                    class="variant-chip <%= i == 0 ? "active" : "" %>"
                                    data-variant-id="<%= v.getId() %>"
                                    data-sku="<%= sku %>"
                                    data-price-value="<%= v.getPrice().longValue() %>"
                                    data-price-text="<%= String.format("%,d VND", v.getPrice().longValue()) %>"
                                    data-stock="<%= v.getQuantity() %>"
                                    data-image="<%= image %>">
                                <%= label %>
                            </button>
                            <% } %>
                        </div>
                    </div>
                    <% } %>

                    <div class="actions">
                        <form method="get" action="${pageContext.request.contextPath}/payment" id="buyNowForm">
                            <input type="hidden" name="source" value="buyNow">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <input type="hidden" name="name" value="<%= p.getName() %>">
                            <input type="hidden" name="category" value="<%= p.getCategory() %>">
                            <input type="hidden" name="brandId" value="<%= p.getBrandId() %>">
                            <input type="hidden" name="imageUrl" value="<%= p.getImageUrl() %>">
                            <input type="hidden" name="priceValue" value="<%= p.getPriceValue() %>">
                            <input type="hidden" name="stock" value="<%= p.getQuantity() %>">
                            <input type="hidden" name="variantId" value="">
                            <input type="hidden" name="sku" value="">
                            <button class="btn btn-buy" type="submit">Mua ngay</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/cart?action=add" id="addCartForm">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <input type="hidden" name="name" value="<%= p.getName() %>">
                            <input type="hidden" name="category" value="<%= p.getCategory() %>">
                            <input type="hidden" name="brandId" value="<%= p.getBrandId() %>">
                            <input type="hidden" name="imageUrl" value="<%= p.getImageUrl() %>">
                            <input type="hidden" name="priceValue" value="<%= p.getPriceValue() %>">
                            <input type="hidden" name="stock" value="<%= p.getQuantity() %>">
                            <input type="hidden" name="variantId" value="">
                            <input type="hidden" name="sku" value="">
                            <button class="btn btn-cart" type="submit">Thêm vào giỏ hàng</button>
                        </form>
                    </div>
                </div>
            </section>

            <div class="sep"></div>
            <section class="card">
                <h2 class="section-title">Chi tiết sản phẩm</h2>
                <div class="desc"><%= p.getDescription() %></div>
            </section>

            <div class="sep"></div>
            <section class="card">
                <h2 class="section-title">Đánh giá sản phẩm</h2>
                <div class="review-summary">
                    <div class="review-score"><%= String.format("%.1f", p.getRating()) %></div>
                    <div>
                        <div class="review-stars">★★★★★</div>
                        <div class="review-meta">Dựa trên <%= p.getReviewCount() %> đánh giá thật từ khách hàng.</div>
                    </div>
                </div>

                <% if (reviewError != null && !reviewError.isEmpty()) { %>
                    <div class="err"><%= reviewError %></div>
                <% } %>

                <div class="review-list">
                    <%
                        if (reviews != null && !reviews.isEmpty()) {
                            for (ProductReviewEntity rv : reviews) {
                    %>
                    <article class="review-item">
                        <%
                            String safeReviewerName = rv.getReviewerName() == null ? "" : rv.getReviewerName();
                            String safeComment = rv.getComment() == null ? "" : rv.getComment();
                            String reviewTime = rv.getCreatedAt() == null ? "" : rv.getCreatedAt().format(reviewDateFormat);
                        %>
                        <div class="review-name"><%= safeReviewerName %></div>
                        <div class="review-stars">
                            <%
                                for (int i = 1; i <= 5; i++) {
                                    out.print(i <= rv.getRating() ? "★" : "☆");
                                }
                            %>
                        </div>
                        <div class="review-meta"><%= reviewTime %></div>
                        <div class="review-comment"><%= safeComment %></div>
                    </article>
                    <%
                            }
                        } else {
                    %>
                    <div class="review-meta">Chưa có đánh giá nào cho sản phẩm này.</div>
                    <% } %>
                </div>

                <form class="review-form" method="post" action="${pageContext.request.contextPath}/product">
                    <input type="hidden" name="action" value="review">
                    <input type="hidden" name="productId" value="<%= p.getId() %>">
                    <label>Số sao</label>
                    <select name="rating">
                        <option value="5">5 sao</option>
                        <option value="4">4 sao</option>
                        <option value="3">3 sao</option>
                        <option value="2">2 sao</option>
                        <option value="1">1 sao</option>
                    </select>
                    <label>Nội dung đánh giá</label>
                    <input name="comment" placeholder="Nhập nhận xét ngắn...">
                    <button type="submit">Gửi đánh giá</button>
                </form>
            </section>

            <div class="sep"></div>
            <section class="card">
                <h2 class="section-title">Sản phẩm liên quan</h2>
                <div class="related-slider">
                    <button type="button" class="related-nav prev" aria-label="Xem sản phẩm trước">‹</button>
                    <div class="related-track" id="related-track">
                        <%
                            if (related != null) {
                                for (ProductCardView r : related) {
                        %>
                        <article class="home-product-card featured-card" role="link" tabindex="0" onclick="window.location.href='${pageContext.request.contextPath}/product?id=<%= r.getId() %>'" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.location.href='${pageContext.request.contextPath}/product?id=<%= r.getId() %>'}">
                            <div class="home-product-img-wrap">
                                <img class="home-product-img" src="<%= r.getImageUrl() %>" alt="related">
                            </div>
                            <div class="home-product-content">
                                <h3><a href="${pageContext.request.contextPath}/product?id=<%= r.getId() %>"><%= r.getName() %></a></h3>
                                <div class="product-extra">
                                    <span>Thương hiệu: <strong><%= r.getBrandName() %></strong></span>
                                    <span>Tồn kho: <strong><%= r.getTotalQuantity() %></strong></span>
                                    <span>Đánh giá: <strong><%= String.format(java.util.Locale.US, "%.1f", r.getRating()) %></strong> ★</span>
                                </div>
                                <p class="home-price"><%= r.getPriceText() %></p>
                                <div class="home-actions" onclick="event.stopPropagation();">
                                    <form method="get" action="${pageContext.request.contextPath}/payment" onclick="event.stopPropagation();">
                                        <input type="hidden" name="source" value="buyNow">
                                        <input type="hidden" name="productId" value="<%= r.getId() %>">
                                        <input type="hidden" name="name" value="<%= r.getName() %>">
                                        <input type="hidden" name="category" value="<%= r.getCategory() %>">
                                        <input type="hidden" name="brandId" value="<%= r.getBrandId() %>">
                                        <input type="hidden" name="imageUrl" value="<%= r.getImageUrl() %>">
                                        <input type="hidden" name="priceValue" value="<%= r.getPriceValue() %>">
                                        <input type="hidden" name="stock" value="<%= r.getTotalQuantity() %>">
                                        <input type="hidden" name="quantity" value="1">
                                        <input type="hidden" name="variantId" value="">
                                        <input type="hidden" name="sku" value="">
                                        <button type="submit" class="btn-buy">MUA NGAY</button>
                                    </form>
                                    <form method="post" action="${pageContext.request.contextPath}/cart?action=add" onclick="event.stopPropagation();">
                                        <input type="hidden" name="productId" value="<%= r.getId() %>">
                                        <input type="hidden" name="name" value="<%= r.getName() %>">
                                        <input type="hidden" name="category" value="<%= r.getCategory() %>">
                                        <input type="hidden" name="brandId" value="<%= r.getBrandId() %>">
                                        <input type="hidden" name="imageUrl" value="<%= r.getImageUrl() %>">
                                        <input type="hidden" name="priceValue" value="<%= r.getPriceValue() %>">
                                        <input type="hidden" name="stock" value="<%= r.getTotalQuantity() %>">
                                        <input type="hidden" name="quantity" value="1">
                                        <input type="hidden" name="variantId" value="">
                                        <input type="hidden" name="sku" value="">
                                        <button type="submit" class="btn-cart">+ GIỎ HÀNG</button>
                                    </form>
                                </div>
                            </div>
                        </article>
                        <%      }
                            }
                        %>
                    </div>
                    <button type="button" class="related-nav next" aria-label="Xem sản phẩm tiếp">›</button>
                </div>
            </section>
            <% } else { %>
            <section class="card">Không tìm thấy sản phẩm.</section>
            <% } %>
        </div>
        <div id="imgModal" class="img-modal" onclick="closeImageModal(event)">
            <button class="img-close" type="button" onclick="closeImageModal(event)">Đóng</button>
            <img id="imgModalPreview" src="" alt="preview">
        </div>
        <script>
            function swapImage(thumbEl) {
                var main = document.getElementById("mainImg");
                main.src = thumbEl.src;
                var all = document.querySelectorAll(".thumbs img");
                all.forEach(function (el) { el.classList.remove("active"); });
                thumbEl.classList.add("active");
            }
            function zoomByMouse(event) {
                var wrapper = event.currentTarget;
                var img = document.getElementById("mainImg");
                var rect = wrapper.getBoundingClientRect();
                var x = ((event.clientX - rect.left) / rect.width) * 100;
                var y = ((event.clientY - rect.top) / rect.height) * 100;
                img.style.transformOrigin = x + "% " + y + "%";
            }
            function resetZoom() {
                var img = document.getElementById("mainImg");
                img.style.transformOrigin = "center center";
            }
            function openImageModal() {
                var modal = document.getElementById("imgModal");
                var main = document.getElementById("mainImg");
                var preview = document.getElementById("imgModalPreview");
                preview.src = main.src;
                modal.classList.add("show");
            }
            function closeImageModal(e) {
                if (e && e.target && e.target.id !== "imgModal" && !e.target.classList.contains("img-close")) {
                    return;
                }
                document.getElementById("imgModal").classList.remove("show");
            }
            function formatVnd(value) {
                try {
                    return Number(value || 0).toLocaleString("en-US") + " VND";
                } catch (e) {
                    return value + " VND";
                }
            }

            function setHiddenValue(formId, name, value) {
                var form = document.getElementById(formId);
                if (!form) return;
                var input = form.querySelector('input[name="' + name + '"]');
                if (input) input.value = value;
            }

            function applyVariant(chip) {
                if (!chip) return;
                var chips = document.querySelectorAll('.variant-chip');
                chips.forEach(function (el) { el.classList.remove('active'); });
                chip.classList.add('active');

                var sku = chip.getAttribute('data-sku') || '';
                var priceValue = chip.getAttribute('data-price-value') || '0';
                var priceText = chip.getAttribute('data-price-text') || formatVnd(priceValue);
                var stock = chip.getAttribute('data-stock') || '0';
                var image = chip.getAttribute('data-image') || '';
                var variantId = chip.getAttribute('data-variant-id') || '';

                var priceEl = document.getElementById('detailPriceText');
                if (priceEl) priceEl.textContent = priceText;


                var stockEl = document.getElementById('detailStockText');
                if (stockEl) stockEl.textContent = 'Số lượng còn: ' + stock;

                if (image) {
                    var main = document.getElementById('mainImg');
                    if (main) main.src = image;
                }

                setHiddenValue('buyNowForm', 'priceValue', priceValue);
                setHiddenValue('buyNowForm', 'stock', stock);
                setHiddenValue('buyNowForm', 'imageUrl', image);
                setHiddenValue('buyNowForm', 'variantId', variantId);
                setHiddenValue('buyNowForm', 'sku', sku);

                setHiddenValue('addCartForm', 'priceValue', priceValue);
                setHiddenValue('addCartForm', 'stock', stock);
                setHiddenValue('addCartForm', 'imageUrl', image);
                setHiddenValue('addCartForm', 'variantId', variantId);
                setHiddenValue('addCartForm', 'sku', sku);
            }

            document.addEventListener('DOMContentLoaded', function () {
                window.toggleAccountMenu = function (button) {
                    var wrapper = button.closest('.home-account-menu');
                    if (!wrapper) return;
                    var dropdown = wrapper.querySelector('.home-account-dropdown');
                    if (!dropdown) return;
                    var isOpen = dropdown.style.display === 'block';
                    document.querySelectorAll('.home-account-dropdown').forEach(function (el) { el.style.display = 'none'; });
                    dropdown.style.display = isOpen ? 'none' : 'block';
                };

                document.addEventListener('click', function (event) {
                    if (!event.target.closest('.home-account-menu')) {
                        document.querySelectorAll('.home-account-dropdown').forEach(function (el) { el.style.display = 'none'; });
                    }
                });

                var chips = document.querySelectorAll('.variant-chip');
                if (chips.length > 0) {
                    chips.forEach(function (chip) {
                        chip.addEventListener('click', function () {
                            applyVariant(chip);
                        });
                    });
                    applyVariant(chips[0]);
                }

                var relatedTrack = document.getElementById('related-track');
                if (relatedTrack) {
                    var relatedSlider = relatedTrack.closest('.related-slider');
                    var prevBtn = relatedSlider ? relatedSlider.querySelector('.related-nav.prev') : null;
                    var nextBtn = relatedSlider ? relatedSlider.querySelector('.related-nav.next') : null;

                    var getStep = function () {
                        var card = relatedTrack.querySelector('.home-product-card');
                        if (!card) return 320;
                        var style = window.getComputedStyle(relatedTrack);
                        var gap = parseFloat(style.columnGap || style.gap || '16') || 16;
                        return card.getBoundingClientRect().width + gap;
                    };

                    var updateButtons = function () {
                        if (!prevBtn || !nextBtn) return;
                        var maxScroll = relatedTrack.scrollWidth - relatedTrack.clientWidth;
                        prevBtn.disabled = relatedTrack.scrollLeft <= 2;
                        nextBtn.disabled = relatedTrack.scrollLeft >= maxScroll - 2;
                    };

                    if (prevBtn) {
                        prevBtn.addEventListener('click', function () {
                            relatedTrack.scrollBy({ left: -getStep(), behavior: 'smooth' });
                        });
                    }

                    if (nextBtn) {
                        nextBtn.addEventListener('click', function () {
                            relatedTrack.scrollBy({ left: getStep(), behavior: 'smooth' });
                        });
                    }

                    relatedTrack.addEventListener('scroll', updateButtons, { passive: true });
                    window.addEventListener('resize', updateButtons);
                    updateButtons();
                }
            });

            document.addEventListener("keydown", function (e) {
                if (e.key === "Escape") {
                    document.getElementById("imgModal").classList.remove("show");
                }
            });
        </script>
    </body>
</html>
