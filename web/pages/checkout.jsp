<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Thanh toán" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Thanh toán</h1>

<c:if test="${not empty error}">
    <div class="alert alert-error panel">${error}</div>
</c:if>
<c:if test="${not empty success}">
    <div class="alert alert-success panel">${success}</div>
</c:if>

<section class="two-column">
    <form class="panel form-grid" action="${pageContext.request.contextPath}/checkout" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="checkout">
        <c:forEach var="item" items="${checkoutItems}">
            <input type="hidden" name="productId" value="${item.productId}">
            <input type="hidden" name="variantId" value="${item.variantId}">
            <input type="hidden" name="quantity" value="${item.quantity}">
        </c:forEach>
        <div class="field"><label>Họ tên</label><input name="name" required value="${not empty submittedName ? submittedName : ''}"><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" required value="${not empty submittedEmail ? submittedEmail : ''}"><span class="error"></span></div>
        <div class="field"><label>Số điện thoại</label><input name="phone" data-phone="true" required value="${not empty submittedPhone ? submittedPhone : ''}"><span class="error"></span></div>
        <div class="field">
            <label>Voucher</label>
            <select name="voucherId" id="voucherSelect">
                <option value="" data-percent="0">Không dùng voucher</option>
                <c:forEach var="v" items="${vouchers}">
                    <option value="${v.id}" data-percent="${v.percent}" ${submittedVoucherId == v.id ? 'selected' : ''}>
                        Mã: ${v.id} (Giảm ${v.percent}%)
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="field full"><label>Địa chỉ</label><textarea name="address" required>${not empty submittedAddress ? submittedAddress : ''}</textarea><span class="error"></span></div>
        <div class="field full"><label>Ghi chú</label><textarea name="note">${not empty submittedNote ? submittedNote : ''}</textarea></div>
        <div class="field full">
            <span class="payment-method-label">Phương thức thanh toán</span>
            <div class="payment-methods-grid">
                <label class="payment-option">
                    <input type="radio" name="paymentMethod" value="COD" ${empty submittedPaymentMethod || submittedPaymentMethod == 'COD' ? 'checked' : ''}>
                    <span class="payment-option-card">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="payment-icon-svg" aria-hidden="true">
                            <rect x="1" y="3" width="15" height="13"></rect>
                            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
                            <circle cx="5.5" cy="18.5" r="2.5"></circle>
                            <circle cx="18.5" cy="18.5" r="2.5"></circle>
                        </svg>
                        <span class="payment-title">COD (Thanh toán khi nhận hàng)</span>
                    </span>
                </label>
                <label class="payment-option">
                    <input type="radio" name="paymentMethod" value="MOMO" ${submittedPaymentMethod == 'MOMO' ? 'checked' : ''}>
                    <span class="payment-option-card">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="payment-icon-svg" aria-hidden="true">
                            <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                            <line x1="12" y1="18" x2="12.01" y2="18"></line>
                        </svg>
                        <span class="payment-title">Ví điện tử MoMo</span>
                    </span>
                </label>
            </div>
        </div>
        <button class="button" type="submit">Đặt hàng</button>
    </form>
    <aside class="panel">
        <h2>Review đơn hàng</h2>
        <ul class="summary-list">
            <c:forEach var="item" items="${checkoutItems}">
                <li>
                    <span><c:out value="${item.productName}" /> x${item.quantity}</span>
                    <strong style="color: var(--color-text-secondary); font-weight: 500;"><c:out value="${item.lineTotal}" /> đ</strong>
                </li>
            </c:forEach>
            <li>
                <span>Tổng thanh toán</span>
                <strong class="price" id="totalCheckoutPriceDisplay" data-original-total="${totalCheckoutPrice}">
                    <c:out value="${totalCheckoutPrice}" /> đ
                </strong>
            </li>
        </ul>
    </aside>
</section>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const voucherSelect = document.getElementById('voucherSelect');
    const totalDisplay = document.getElementById('totalCheckoutPriceDisplay');
    if (voucherSelect && totalDisplay) {
        const originalTotal = parseFloat(totalDisplay.getAttribute('data-original-total'));
        
        voucherSelect.addEventListener('change', function() {
            const selectedOption = voucherSelect.options[voucherSelect.selectedIndex];
            const discountPercent = parseFloat(selectedOption.getAttribute('data-percent') || '0');
            
            const discountAmount = originalTotal * (discountPercent / 100);
            const finalTotal = Math.max(0, originalTotal - discountAmount);
            
            totalDisplay.textContent = finalTotal.toLocaleString('vi-VN', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + ' đ';
        });
    }
});
</script>

<jsp:include page="../layouts/footer.jsp" />

