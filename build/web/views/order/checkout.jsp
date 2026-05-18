<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Thanh toán" scope="request" />
<jsp:include page="../../layouts/header.jsp" />
<h1 class="page-title">Thanh toán</h1>
<section class="two-column">
    <form class="panel form-grid" action="${pageContext.request.contextPath}/checkout" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="checkout">
        <input type="hidden" name="productId" value="p1111111-1111-1111-1111-111111111111">
        <input type="hidden" name="variantId" value="v1111111-1111-1111-1111-111111111111">
        <input type="hidden" name="quantity" value="1">
        <div class="field"><label>Họ tên</label><input name="name" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" required><span class="error"></span></div>
        <div class="field"><label>Số điện thoại</label><input name="phone" data-phone="true" required><span class="error"></span></div>
        <div class="field"><label>Voucher</label><select name="voucherId"><option value="">Không dùng voucher</option><option value="voucher10">Giám 10%</option></select></div>
        <div class="field full"><label>Địa chỉ</label><textarea name="address" required></textarea><span class="error"></span></div>
        <div class="field full"><label>Ghi chú</label><textarea name="note"></textarea></div>
        <div class="field full">
            <label><input type="radio" name="paymentMethod" value="COD" checked> COD</label>
            <label><input type="radio" name="paymentMethod" value="SEPAY"> Chuyển khoản QR (Sepay)</label>
        </div>
        <button class="button" type="submit">Đặt hàng</button>
    </form>
    <aside class="panel">
        <h2>Review đơn hàng</h2>
        <ul class="summary-list">
            <li><span>Samsung 990 PRO x1</span><strong>3.490.000 VND</strong></li>
            <li><span>TP-Link AX73 x2</span><strong>5.780.000 VND</strong></li>
            <li><span>Giảm giá</span><strong>0 VND</strong></li>
            <li><span>Tổng tiền</span><strong class="price">9.270.000 VND</strong></li>
        </ul>
    </aside>
</section>
<jsp:include page="../../layouts/footer.jsp" />

