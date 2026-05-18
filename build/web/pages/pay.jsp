<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Thanh toán qua chuyển khoản QR" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<style>
.pay-container {
    max-width: 800px;
    margin: 40px auto;
    padding: 24px;
}
.pay-grid {
    display: grid;
    grid-template-columns: 1fr 1.2fr;
    gap: 32px;
    margin-top: 24px;
}
@media (max-width: 768px) {
    .pay-grid {
        grid-template-columns: 1fr;
    }
}
.qr-card {
    text-align: center;
    padding: 24px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    background: var(--color-surface);
}
.qr-image {
    width: 250px;
    height: 250px;
    margin: 16px auto;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    padding: 8px;
    background: #fff;
}
.pay-info-card {
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}
.info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px dashed var(--color-border);
    padding-bottom: 12px;
}
.info-row:last-child {
    border: none;
}
.info-label {
    color: var(--color-text-secondary);
    font-size: 0.9rem;
    font-weight: 500;
}
.info-value {
    color: var(--color-text);
    font-weight: 700;
    font-size: 1rem;
    text-align: right;
}
.copy-badge {
    cursor: pointer;
    font-size: 0.72rem;
    padding: 2px 6px;
    background: var(--color-gray-100);
    border-radius: var(--radius-sm);
    color: var(--color-primary);
    margin-left: 6px;
}
.copy-badge:hover {
    background: var(--color-primary);
    color: #fff;
}
.payment-waiting {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: var(--color-text-secondary);
    font-weight: 600;
    margin-top: 16px;
}
.payment-success {
    display: none;
    text-align: center;
    background: #f0faf4;
    border: 1px solid rgba(0, 135, 58, 0.2);
    color: var(--color-success);
    padding: 16px;
    border-radius: var(--radius-md);
    margin-top: 16px;
    animation: fadeIn 0.4s ease;
}
.spinner {
    width: 20px;
    height: 20px;
    border: 3px solid var(--color-gray-200);
    border-top: 3px solid var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}
@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>

<div class="pay-container content">
    <h1 class="page-title" style="text-align: center;">Chuyển khoản thanh toán</h1>
    <p class="muted" style="text-align: center; margin-top: -12px;">Vui lòng quét mã QR hoặc chuyển khoản thủ công theo thông tin dưới đây.</p>

    <div class="pay-grid">
        <!-- QR Code Card -->
        <div class="qr-card panel">
            <h3 style="margin: 0; font-weight: 700;">Quét mã VietQR</h3>
            <p class="muted" style="font-size: 0.82rem; margin: 4px 0 16px;">Sử dụng mọi ứng dụng ngân hàng di động để quét</p>
            <img class="qr-image" src="https://qr.sepay.vn/img?acc=${sepayAccNum}&bank=${sepayBank}&amount=${paymentTotal}&des=${transCode}&template=compact" alt="Mã thanh toán VietQR">
            
            <div class="payment-waiting">
                <div class="spinner"></div>
                <span>Đang chờ chuyển khoản tự động...</span>
            </div>
            
            <div class="payment-success">
                <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round" style="display: inline-block; vertical-align: middle; margin-right: 8px;">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <strong style="vertical-align: middle;">Thanh toán thành công! Hệ thống đang chuyển hướng...</strong>
            </div>
        </div>

        <!-- Bank Details Card -->
        <div class="pay-info-card panel">
            <h3 style="margin: 0 0 12px; font-weight: 700; border-bottom: 1px solid var(--color-border); padding-bottom: 12px;">Thông tin chuyển khoản</h3>
            
            <div class="info-row">
                <span class="info-label">Ngân hàng</span>
                <span class="info-value">${sepayBank} <span class="copy-badge" onclick="copyText('${sepayBank}')">Copy</span></span>
            </div>
            <div class="info-row">
                <span class="info-label">Số tài khoản</span>
                <span class="info-value" style="color: var(--color-primary); font-size: 1.1rem;">${sepayAccNum} <span class="copy-badge" onclick="copyText('${sepayAccNum}')">Copy</span></span>
            </div>
            <div class="info-row">
                <span class="info-label">Chủ tài khoản</span>
                <span class="info-value">${sepayAccName}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Số tiền cần thanh toán</span>
                <span class="info-value" style="color: var(--color-danger); font-size: 1.15rem;"><c:out value="${paymentTotal}" /> đ</span>
            </div>
            <div class="info-row">
                <span class="info-label">Nội dung chuyển khoản</span>
                <span class="info-value" style="color: var(--color-black); background: #fef3c7; border: 1px solid #f59e0b; padding: 4px 8px; border-radius: var(--radius-sm); font-family: monospace;">${transCode} <span class="copy-badge" onclick="copyText('${transCode}')" style="background:#fff">Copy</span></span>
            </div>

            <div style="background: rgba(245, 166, 35, 0.08); border: 1px dashed var(--color-warning); padding: 12px; border-radius: var(--radius-md); font-size: 0.8rem; line-height: 1.4; color: #b45309; margin-top: 8px;">
                <strong>LƯU Ý QUAN TRỌNG:</strong> Bạn phải điền chính xác <strong>nội dung chuyển khoản</strong> ở trên để hệ thống ghi nhận thanh toán tự động ngay lập tức.
            </div>

            <div class="toolbar mt-4" style="justify-content: center; margin-top: 16px;">
                <a href="${pageContext.request.contextPath}/orders?action=history" class="button secondary" style="width: 100%;">Quay lại lịch sử đơn hàng</a>
            </div>
        </div>
    </div>
</div>

<script>
function copyText(text) {
    navigator.clipboard.writeText(text).then(function() {
        alert("Đã sao chép: " + text);
    }, function(err) {
        console.error("Could not copy text: ", err);
    });
}

document.addEventListener('DOMContentLoaded', function() {
    const checkInterval = setInterval(() => {
        fetch('${pageContext.request.contextPath}/checkout?action=check&orderIds=${param.orderIds}')
            .then(res => res.json())
            .then(data => {
                if (data.paid) {
                    clearInterval(checkInterval);
                    // Update UI to show success
                    document.querySelector('.payment-waiting').style.display = 'none';
                    document.querySelector('.payment-success').style.display = 'block';
                    // Redirect to order history after 3.5 seconds
                    setTimeout(() => {
                        window.location.href = '${pageContext.request.contextPath}/orders?action=history';
                    }, 3500);
                }
            })
            .catch(err => console.error("Check status error:", err));
    }, 3000);
});
</script>

<jsp:include page="../layouts/footer.jsp" />
