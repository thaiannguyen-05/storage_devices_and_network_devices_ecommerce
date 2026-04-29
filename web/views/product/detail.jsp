<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="entity.ProductReviewEntity"%>
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
            .page { max-width: 1440px; margin: 0 auto; padding: 20px 32px 64px; }
            .home-categories .home-category-card {
                border-radius: 0;
                padding: 28px 16px;
                border-left: 1px solid var(--ch-hairline);
                border-right: 1px solid var(--ch-hairline);
            }
            .home-categories .home-category-grid {
                gap: 0;
            }
            .main { display: grid; grid-template-columns: 1fr 1.05fr; gap: 24px; }
            .card { background: var(--ch-surface-card); border: 1px solid var(--ch-hairline); border-radius: 12px; padding: 16px; }
            .main-img-wrap { width: 100%; height: 420px; overflow: hidden; border-radius: 10px; border: 1px solid var(--ch-hairline); background: #000; cursor: zoom-in; }
            .main-img { width: 100%; height: 420px; object-fit: contain; display: block; transition: transform 0.18s ease; transform-origin: center center; }
            .main-img-wrap:hover .main-img { transform: scale(1.65); }
            .thumbs { margin-top: 10px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
            .thumbs img { width: 100%; height: 76px; object-fit: cover; border-radius: 8px; border: 1px solid var(--ch-hairline); cursor: pointer; }
            .thumbs img.active { border-color: var(--ch-primary); }
            .name { margin: 0 0 8px; font-size: 34px; line-height: 1.2; }
            .rating { display: flex; align-items: center; gap: 8px; color: #fbbf24; font-size: 16px; margin-bottom: 10px; }
            .rating small { color: var(--ch-muted); font-size: 13px; }
            .price { font-size: 58px; color: var(--ch-primary); font-weight: 800; margin: 10px 0; }
            .meta { color: var(--ch-body-strong); margin: 5px 0; font-size: 20px; }
            .stock { color: #34d399; font-weight: 800; margin: 10px 0; font-size: 22px; }
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
            .section-title { margin: 0 0 14px; font-size: 36px; }
            .desc { color: var(--ch-body); line-height: 1.7; white-space: pre-wrap; font-size: 18px; }
            .sep { height: 20px; }

            .related-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px; }
            .related-card { background: var(--ch-surface-soft); border: 1px solid var(--ch-hairline); border-radius: 10px; overflow: hidden; }
            .related-card img { width: 100%; height: 160px; object-fit: cover; }
            .related-card .c { padding: 12px; }
            .related-card .n { color: var(--ch-ink); text-decoration: none; font-weight: 700; font-size: 24px; display: block; min-height: 52px; }
            .related-card .p { color: var(--ch-primary); margin-top: 8px; font-weight: 800; font-size: 34px; }

            .review-summary { display: flex; align-items: center; gap: 16px; padding: 16px; border: 1px solid var(--ch-hairline); border-radius: 10px; margin-bottom: 12px; }
            .review-score { font-size: 60px; color: #f59e0b; font-weight: 800; }
            .review-list .review-item { border-top: 1px solid var(--ch-hairline); padding: 14px 0; }
            .review-list .review-item:first-child { border-top: none; }
            .review-name { font-weight: 700; font-size: 22px; }
            .review-stars { color: #f59e0b; font-size: 24px; }
            .review-meta { color: var(--ch-muted); font-size: 16px; margin-top: 3px; }
            .review-comment { margin-top: 6px; color: var(--ch-body); white-space: pre-wrap; font-size: 18px; }
            .review-form { margin-top: 14px; border: 1px solid var(--ch-hairline); border-radius: 10px; padding: 14px; }
            .review-form input, .review-form textarea, .review-form select { width: 100%; margin-top: 6px; margin-bottom: 10px; padding: 12px; border: 1px solid var(--ch-hairline); border-radius: 8px; background: var(--ch-surface-soft); color: var(--ch-ink); font-size: 18px; }
            .review-form label { font-size: 30px; color: var(--ch-ink); }
            .review-form button { border: none; border-radius: 8px; background: var(--ch-primary); color: var(--ch-on-primary); padding: 12px 16px; font-weight: 800; cursor: pointer; font-size: 24px; }
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
            List<ProductCardView> related = (List<ProductCardView>) request.getAttribute("relatedProducts");
            List<ProductReviewEntity> reviews = (List<ProductReviewEntity>) request.getAttribute("reviews");
            String reviewError = (String) request.getAttribute("reviewError");
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

                <a class="home-category-btn" href="${pageContext.request.contextPath}/product">
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
                        <a href="${pageContext.request.contextPath}/auth?action=profile" class="home-login"><span class="home-inline-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5z"/></svg></span><%= authUserName %></a>
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

        <section class="home-categories" style="max-width: 100%; padding: 0 0 8px; margin: 0;">
            <div class="home-category-grid" style="margin: 0; grid-template-columns: repeat(6, 1fr);">
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=HDD">HDD CLUSTER</a>
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=SSD">SSD FLASH</a>
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=NAS">NAS NODES</a>
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=USB">USB MOBILE</a>
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=MEMORY">SD CARDS</a>
                <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=ENCLOSURE">RACK BOXES</a>
            </div>
        </section>

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
                    <p class="price"><%= p.getPriceText() %></p>
                    <p class="meta">Danh mục: <strong><%= p.getCategory() %></strong></p>
                    <p class="meta">Thương hiệu: <strong><%= p.getBrandId() %></strong></p>
                    <p class="meta">Trạng thái: <strong><%= "ACTIVE".equalsIgnoreCase(p.getStatus()) ? "Còn hàng" : "Ngừng bán" %></strong></p>
                    <p class="stock">Số lượng còn: <%= p.getQuantity() %></p>

                    <div class="actions">
                        <form method="get" action="${pageContext.request.contextPath}/payment">
                            <input type="hidden" name="source" value="buyNow">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <input type="hidden" name="name" value="<%= p.getName() %>">
                            <input type="hidden" name="category" value="<%= p.getCategory() %>">
                            <input type="hidden" name="brandId" value="<%= p.getBrandId() %>">
                            <input type="hidden" name="imageUrl" value="<%= p.getImageUrl() %>">
                            <input type="hidden" name="priceValue" value="<%= p.getPriceValue() %>">
                            <input type="hidden" name="stock" value="<%= p.getQuantity() %>">
                            <button class="btn btn-buy" type="submit">Mua ngay</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/cart?action=add">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <input type="hidden" name="name" value="<%= p.getName() %>">
                            <input type="hidden" name="category" value="<%= p.getCategory() %>">
                            <input type="hidden" name="brandId" value="<%= p.getBrandId() %>">
                            <input type="hidden" name="imageUrl" value="<%= p.getImageUrl() %>">
                            <input type="hidden" name="priceValue" value="<%= p.getPriceValue() %>">
                            <input type="hidden" name="stock" value="<%= p.getQuantity() %>">
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
                <h2 class="section-title">Sản phẩm liên quan</h2>
                <div class="related-grid">
                    <%
                        if (related != null) {
                            for (ProductCardView r : related) {
                    %>
                    <article class="related-card">
                        <a href="${pageContext.request.contextPath}/product?id=<%= r.getId() %>">
                            <img src="<%= r.getImageUrl() %>" alt="related">
                        </a>
                        <div class="c">
                            <a class="n" href="${pageContext.request.contextPath}/product?id=<%= r.getId() %>"><%= r.getName() %></a>
                            <div class="p"><%= r.getPriceText() %></div>
                        </div>
                    </article>
                    <%      }
                        }
                    %>
                </div>
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
                        <div class="review-name"><%= rv.getReviewerName() %></div>
                        <div class="review-stars">
                            <%
                                for (int i = 1; i <= 5; i++) {
                                    out.print(i <= rv.getRating() ? "★" : "☆");
                                }
                            %>
                        </div>
                        <div class="review-meta"><%= rv.getCreatedAt() %></div>
                        <div class="review-comment"><%= rv.getComment() == null ? "" : rv.getComment() %></div>
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
                    <label>Tên của bạn</label>
                    <input name="reviewerName" required placeholder="Nhập tên">
                    <label>Số sao</label>
                    <select name="rating">
                        <option value="5">5 sao</option>
                        <option value="4">4 sao</option>
                        <option value="3">3 sao</option>
                        <option value="2">2 sao</option>
                        <option value="1">1 sao</option>
                    </select>
                    <label>Nội dung đánh giá</label>
                    <textarea name="comment" rows="4" placeholder="Chia sẻ trải nghiệm của bạn..."></textarea>
                    <button type="submit">Gửi đánh giá</button>
                </form>
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
            document.addEventListener("keydown", function (e) {
                if (e.key === "Escape") {
                    document.getElementById("imgModal").classList.remove("show");
                }
            });
        </script>
    </body>
</html>
