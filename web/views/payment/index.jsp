<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="module.bussiness.cart.dto.CartItemView"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Thanh toán | LinhNamStore"; %>

<style>
    .checkout-grid { display: grid; grid-template-columns: 1fr 450px; gap: 32px; align-items: start; }
    @media (max-width: 1024px) { .checkout-grid { grid-template-columns: 1fr; } }
</style>

<%
    List<CartItemView> checkoutItems = (List<CartItemView>) request.getAttribute("checkoutItems");
    String totalPriceText = (String) request.getAttribute("totalPriceText");
    Integer checkoutCount = (Integer) request.getAttribute("checkoutCount");
    int count = checkoutCount == null ? 0 : checkoutCount;
    String source = (String) request.getAttribute("source");
    if (source == null) source = "cart";
    String success = (String) request.getAttribute("paymentSuccess");
    String error = (String) request.getAttribute("paymentError");
    String fullName = (String) request.getAttribute("fullName");
    if (fullName == null) fullName = "";
    String email = (String) request.getAttribute("email");
    if (email == null) email = "";
    String voucherCode = (String) request.getAttribute("voucherCode");
    if (voucherCode == null) voucherCode = "";
    String subtotalText = (String) request.getAttribute("subtotalText");
    String discountText = (String) request.getAttribute("discountText");
    CartItemView firstItem = (checkoutItems != null && !checkoutItems.isEmpty()) ? checkoutItems.get(0) : null;
%>

<h2 style="margin-bottom: 32px;">Thanh toán đơn hàng</h2>

<% if (success != null) { %>
    <div class="home-alert" style="background: #112211; border-color: #44ff44; color: #aaffaa;"><%= success %></div>
<% } %>
<% if (request.getAttribute("paymentDoneFlash") != null) { %>
    <div id="paymentDoneToast" style="position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(22,163,74,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);">Thanh toán thành công</div>
<% } %>
<% if (error != null) { %>
    <div class="home-alert"><%= error %></div>
<% } %>

<div class="checkout-grid">
    <section>
        <h3 style="margin-bottom: 24px; font-size: 14px; text-transform: uppercase; letter-spacing: 1px; color: var(--ch-muted);">Danh sách sản phẩm (<%= count %>)</h3>
        <% if (checkoutItems == null || checkoutItems.isEmpty()) { %>
        <div class="empty-state">Chưa có sản phẩm nào để thanh toán.</div>
        <% } else {
            for (CartItemView item : checkoutItems) { %>
        <article class="payment-card">
            <img src="<%= item.getImageUrl() %>" alt="product" loading="lazy" decoding="async">
            <div>
                <p class="name"><%= item.getName() %></p>
                <p class="meta">DANH MỤC: <%= item.getCategory() %> | SL: <%= item.getQuantity() %></p>
            </div>
            <div class="home-price" style="margin: 0 !important;"><%= String.format("%,d VND", item.getUnitPrice() * item.getQuantity()) %></div>
        </article>
        <% } } %>
    </section>

    <section class="summary-card">
        <h3 style="margin-bottom: 24px;">Thông tin nhận hàng</h3>
        <form method="post" action="${pageContext.request.contextPath}/payment">
            <input type="hidden" name="source" value="<%= source %>">
            <% if ("buyNow".equalsIgnoreCase(source) && firstItem != null) { %>
                <input type="hidden" name="productId" value="<%= firstItem.getProductId() %>">
                <input type="hidden" name="variantId" value="<%= firstItem.getVariantId() %>">
                <input type="hidden" name="name" value="<%= firstItem.getName() %>">
                <input type="hidden" name="category" value="<%= firstItem.getCategory() %>">
                <input type="hidden" name="brandId" value="<%= firstItem.getBrandId() %>">
                <input type="hidden" name="imageUrl" value="<%= firstItem.getImageUrl() %>">
                <input type="hidden" name="priceValue" value="<%= firstItem.getUnitPrice() %>">
                <input type="hidden" name="stock" value="<%= firstItem.getStock() %>">
                <input type="hidden" name="quantity" value="<%= firstItem.getQuantity() %>">
            <% } %>

            <div class="ch-form-field"><label>Họ và tên</label><input name="fullName" required value="<%= fullName %>" readonly></div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="ch-form-field"><label>Số điện thoại</label><input name="phone" required placeholder="+84 9xx"></div>
                <div class="ch-form-field"><label>Email</label><input name="email" type="email" value="<%= email %>" readonly></div>
            </div>
            <div class="ch-form-field"><label>Địa chỉ nhận hàng</label><textarea name="address" required style="min-height: 80px;"></textarea></div>
            <div class="ch-form-field"><label>Tỉnh / Thành phố</label><input name="city" required placeholder="TP. Hồ Chí Minh"></div>
            <div class="ch-form-field">
                <label>Phương thức thanh toán</label>
                <select id="paymentMethod" name="paymentMethod">
                    <option value="COD">Thanh toán khi nhận hàng (COD)</option>
                    <option value="SEPAY">Thanh toán online qua SePay</option>
                    <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
                </select>
            </div>

            <div id="sepayInfoBox" style="display: none; background: var(--ch-surface-soft); padding: 16px; border-radius: 8px; margin-bottom: 24px; border: 1px solid var(--ch-hairline);">
                <p style="margin: 0; font-size: 14px; color: var(--ch-muted);">Bạn sẽ được chuyển đến trang thanh toán SePay để hoàn tất giao dịch.</p>
            </div>

            <div id="cardPaymentBox" style="display: none; background: var(--ch-surface-soft); padding: 16px; border-radius: 8px; margin-bottom: 24px; border: 1px solid var(--ch-hairline);">
                <div class="ch-form-field"><label>Tên chủ thẻ</label><input id="cardHolder" name="cardHolder" placeholder="NGUYEN VAN A"></div>
                <div class="ch-form-field"><label>Số thẻ</label><input id="cardNumber" name="cardNumber" placeholder="xxxx xxxx xxxx xxxx"></div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                    <div class="ch-form-field"><label>Ngày hết hạn</label><input id="cardExpiry" name="cardExpiry" placeholder="MM/YY"></div>
                    <div class="ch-form-field"><label>Mã CVC</label><input id="cardCvv" name="cardCvv" placeholder="123"></div>
                </div>
            </div>

            <div class="ch-form-field">
                <label>Mã giảm giá</label>
                <div style="display: flex; gap: 8px;">
                    <input name="voucherCode" value="<%= voucherCode %>" placeholder="NHẬP MÃ GIẢM GIÁ">
                    <button class="btn-cart" style="width: auto; padding: 0 16px;" type="submit" name="actionType" value="applyVoucher" formnovalidate>ÁP DỤNG</button>
                </div>
                <% String voucherSummary = (String) request.getAttribute("voucherSummary");
                   if (voucherSummary != null && !voucherSummary.isEmpty()) { %>
                    <p style="font-size: 12px; color: var(--ch-primary); margin-top: 8px;">Đã áp dụng giảm giá: -<%= voucherSummary %></p>
                <% } %>
            </div>

            <div class="summary-line"><span>TẠM TÍNH</span><span><%= subtotalText == null ? "0 VND" : subtotalText %></span></div>
            <div class="summary-line"><span>PHÍ VẬN CHUYỂN</span><span>0 VND</span></div>
            <div class="summary-line"><span>GIẢM GIÁ</span><span style="color: var(--ch-primary);"><%= discountText == null ? "-0 VND" : discountText %></span></div>
            <div class="summary-line total"><span>TỔNG CỘNG</span><span><%= totalPriceText == null ? "0 VND" : totalPriceText %></span></div>

            <button class="home-cta btn-full" type="submit" name="actionType" value="placeOrder">HOÀN TẤT ĐẶT HÀNG</button>
        </form>
    </section>
</div>

<script>
(function () { var toast = document.getElementById('paymentDoneToast'); if (!toast) return; var dismissed = false; var dismiss = function () { if (dismissed || !toast.parentNode) return; dismissed = true; toast.style.opacity = '0'; toast.style.transform = 'translateX(-50%) translateY(-8px)'; window.removeEventListener('pointerdown', onPointerDown, true); setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); window.location.href = '${pageContext.request.contextPath}/payment/done'; }, 360); }; var onPointerDown = function () { dismiss(); }; window.addEventListener('pointerdown', onPointerDown, true); setTimeout(dismiss, 2000); })();
(function () { var methodEl = document.getElementById("paymentMethod"); var boxEl = document.getElementById("cardPaymentBox"); var sepayEl = document.getElementById("sepayInfoBox"); var cardInputs = [document.getElementById("cardHolder"), document.getElementById("cardNumber"), document.getElementById("cardExpiry"), document.getElementById("cardCvv")]; function isCardMethod(value) { return value === "VISA" || value === "ATM"; } function toggleCardFields() { var show = isCardMethod(methodEl.value); boxEl.style.display = show ? "block" : "none"; for (var i = 0; i < cardInputs.length; i++) { if (cardInputs[i]) cardInputs[i].required = show; } if (sepayEl) sepayEl.style.display = methodEl.value === "SEPAY" ? "block" : "none"; } methodEl.addEventListener("change", toggleCardFields); toggleCardFields(); })();
</script>

<%@include file="../includes/layout-end.jsp" %>
