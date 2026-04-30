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
                <img src="<%= item.getImageUrl() %>" alt="item">
                <div>
                    <p class="title"><%= item.getName() %></p>
                    <p class="meta">DANH MỤC: <%= item.getCategory() %></p>
                    <p class="meta">MÃ SP: <%= item.getProductId() %></p>
                    <p class="meta">TỒN KHO: <%= item.getStock() %></p>
                </div>
                <div style="text-align: right;">
                    <p class="home-price" style="margin-bottom: 16px !important;"><%= String.format("%,d VND", item.getUnitPrice()) %></p>
                    
                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 12px;">
                        <form class="cart-qty-form" method="post" action="${pageContext.request.contextPath}/cart?action=update">
                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
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
                        
                        <form method="post" action="${pageContext.request.contextPath}/cart?action=remove" onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?');">
                            <input type="hidden" name="productId" value="<%= item.getProductId() %>">
                            <button class="btn-cart" style="color: #ff4444; border-color: #442222 !important;" type="submit">XÓA</button>
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
                    <form method="post" action="${pageContext.request.contextPath}/cart?action=clear" onsubmit="return confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?');">
                        <button class="btn-cart" style="height: 48px; padding: 0 24px;" type="submit">XÓA TẤT CẢ</button>
                    </form>
                    <a class="home-cta" style="height: 48px; padding: 0 40px; display: flex; align-items: center;" href="${pageContext.request.contextPath}/payment?source=cart">THANH TOÁN</a>
                </div>
            </div>
            <%
                }
            %>
        </main>
    </body>
</html>
