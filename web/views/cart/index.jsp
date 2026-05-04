<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="module.bussiness.cart.dto.CartItemView"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Giỏ hàng | StoreIT</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
    </head>
    <body>
        <%
            Integer cartCount = (Integer) request.getAttribute("cartCount");
            int count = cartCount == null ? 0 : cartCount;
            String totalPriceText = (String) request.getAttribute("totalPriceText");
            if (totalPriceText == null) totalPriceText = "0 VND";
            List<CartItemView> items = (List<CartItemView>) request.getAttribute("cartItems");
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
                <a class="home-category-btn" href="${pageContext.request.contextPath}/product">Tiếp tục mua sắm</a>
                <div style="flex: 1;"></div>
                <div class="home-header-right">
                    <div class="home-hotline">HOTLINE <b>1900 9999</b></div>
                </div>
            </div>
        </header>

        <main class="home-main">
            <h2 style="margin-bottom: 32px;">Giỏ hàng (<%= count %> sản phẩm)</h2>

            <%
                if (items == null || items.isEmpty()) {
            %>
            <div class="empty-state">
                <p>Giỏ hàng của bạn đang trống.</p>
                <a href="${pageContext.request.contextPath}/product" class="home-cta" style="margin-top: 24px;">Mua sắm ngay</a>
            </div>
            <%
                } else {
                    for (CartItemView item : items) {
            %>
            <article class="cart-card">
                <img src="<%= item.getImageUrl() %>" alt="item" loading="lazy" decoding="async">
                <div>
                    <p class="title"><%= item.getName() %></p>
                    <p class="meta">DANH MỤC: <%= item.getCategory() %></p>
                    <p class="meta">PHÂN LOẠI: <%= item.getSku() == null || item.getSku().isBlank() ? "Mặc định" : item.getSku() %></p>
                    <p class="meta">TỒN KHO: <%= item.getStock() %></p>
                </div>
                <div style="text-align: right;">
                    <p class="home-price" style="margin-bottom: 16px !important;"><%= String.format("%,d VND", item.getUnitPrice()) %></p>
                    
                    <div class="cart-actions-wrap">
                        <form class="cart-qty-form" method="post" action="${pageContext.request.contextPath}/cart?action=update">
                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
                            <input type="hidden" name="variantId" value="<%= item.getVariantId() == null ? "" : item.getVariantId() %>">
                            <button class="btn-qty" type="submit" name="op" value="dec">-</button>
                            <input
                                type="number"
                                min="1"
                                max="<%= Math.max(item.getStock(), 1) %>"
                                name="quantity"
                                value="<%= item.getQuantity() %>"
                                onchange="this.form.submit()">
                            <button class="btn-qty" type="submit" name="op" value="inc">+</button>
                        </form>

                        <form method="post" action="${pageContext.request.contextPath}/cart?action=remove" class="js-confirm-form" data-confirm-title="Xóa sản phẩm" data-confirm-message="Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?">
                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
                            <input type="hidden" name="variantId" value="<%= item.getVariantId() == null ? "" : item.getVariantId() %>">
                            <button class="btn-cart-remove" type="submit">XÓA</button>
                        </form>
                    </div>
                </div>
            </article>
            <%
                    }
            %>
            <div class="summary-card">
                <div class="summary-line total">
                    <span>TỔNG CỘNG DỰ KIẾN</span>
                    <span><%= totalPriceText %></span>
                </div>
                <div style="display: flex; gap: 16px; margin-top: 32px; justify-content: flex-end;">
                    <form method="post" action="${pageContext.request.contextPath}/cart?action=clear" class="js-confirm-form" data-confirm-title="Xóa toàn bộ giỏ hàng" data-confirm-message="Bạn có chắc muốn xóa toàn bộ giỏ hàng?">
                        <button class="btn-cart" type="submit">XÓA TẤT CẢ</button>
                    </form>
                    <a class="home-cta" style="height: 48px; padding: 0 40px; display: flex; align-items: center;" href="${pageContext.request.contextPath}/payment?source=cart">THANH TOÁN</a>
                </div>
            </div>
            <%
                }
            %>
        </main>

        <div id="confirmOverlay" class="cart-confirm-overlay is-hidden">
            <div class="cart-confirm-modal" role="dialog" aria-modal="true" aria-labelledby="confirmTitle" aria-describedby="confirmMessage">
                <h3 id="confirmTitle">Xác nhận</h3>
                <p id="confirmMessage">Bạn có chắc muốn thực hiện thao tác này?</p>
                <div class="cart-confirm-actions">
                    <button id="confirmCancel" type="button" class="btn-confirm-cancel">Hủy</button>
                    <button id="confirmOk" type="button" class="btn-confirm-ok">Xóa</button>
                </div>
            </div>
        </div>

        <script>
            (function () {
                const overlay = document.getElementById('confirmOverlay');
                const titleEl = document.getElementById('confirmTitle');
                const msgEl = document.getElementById('confirmMessage');
                const okBtn = document.getElementById('confirmOk');
                const cancelBtn = document.getElementById('confirmCancel');
                const forms = document.querySelectorAll('.js-confirm-form');
                let pendingForm = null;

                function closeModal() {
                    overlay.classList.add('is-hidden');
                    pendingForm = null;
                }

                function openModal(form) {
                    pendingForm = form;
                    titleEl.textContent = form.dataset.confirmTitle || 'Xác nhận';
                    msgEl.textContent = form.dataset.confirmMessage || 'Bạn có chắc muốn thực hiện thao tác này?';
                    overlay.classList.remove('is-hidden');
                }

                forms.forEach(function (form) {
                    form.addEventListener('submit', function (event) {
                        if (form.dataset.confirmed === '1') {
                            form.dataset.confirmed = '0';
                            return;
                        }
                        event.preventDefault();
                        openModal(form);
                    });
                });

                okBtn.addEventListener('click', function () {
                    if (!pendingForm) return;
                    pendingForm.dataset.confirmed = '1';
                    const form = pendingForm;
                    closeModal();
                    form.requestSubmit();
                });

                cancelBtn.addEventListener('click', closeModal);

                overlay.addEventListener('click', function (event) {
                    if (event.target === overlay) {
                        closeModal();
                    }
                });

                document.addEventListener('keydown', function (event) {
                    if (event.key === 'Escape' && !overlay.classList.contains('is-hidden')) {
                        closeModal();
                    }
                });
            })();
        </script>
    </body>
</html>
