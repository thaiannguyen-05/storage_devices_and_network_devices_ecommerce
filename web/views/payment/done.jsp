<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Thanh toán thành công | LinhNamStore"; %>

<div style="min-height:72vh;display:flex;align-items:center;justify-content:center;">
    <section class="summary-card" style="max-width:680px;width:100%;text-align:center;padding:42px 28px;">
        <div style="width:96px;height:96px;border-radius:50%;background:rgba(22,163,74,.16);border:2px solid rgba(22,163,74,.45);display:flex;align-items:center;justify-content:center;margin:0 auto 18px;box-shadow:0 0 28px rgba(22,163,74,.22);">
            <svg width="52" height="52" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 6 9 17l-5-5" stroke="#4ade80" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
        </div>
        <h1 style="margin:0 0 10px;font-size:36px;line-height:1.2;">Thanh toán thành công</h1>
        <p style="margin:0 0 26px;color:var(--ch-muted);font-size:17px;line-height:1.7;">
            Đơn hàng của bạn đã được ghi nhận thành công. Chúng tôi sẽ sớm liên hệ để xác nhận và giao hàng.
        </p>
        <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;">
            <a class="home-cta" style="width:260px;" href="${pageContext.request.contextPath}/product">Tiếp tục mua sắm</a>
            <a class="home-cta" style="width:260px;text-decoration:none;" href="${pageContext.request.contextPath}/cart">Về giỏ hàng</a>
        </div>
    </section>
</div>

<%@include file="../includes/layout-end.jsp" %>
