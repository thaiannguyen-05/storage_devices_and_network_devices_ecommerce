<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Thanh toán SePay | LinhNamStore"; %>

<%
    String status = request.getParameter("status");
    String message = request.getParameter("message");
    String redirectUrl = request.getParameter("redirect");
    String orderId = request.getParameter("orderId");
    if (status == null) status = "pending";
    if (message == null) message = "";
    if (redirectUrl == null) redirectUrl = "/product";
    if (orderId == null) orderId = "";
%>

<style>
    .sepay-status-card {
        max-width: 560px;
        margin: 60px auto;
        padding: 48px 36px;
        text-align: center;
        background: var(--ch-surface-soft);
        border: 1px solid var(--ch-hairline);
        border-radius: 16px;
    }
    .sepay-icon {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
    }
    .sepay-icon.success {
        background: rgba(22, 163, 74, 0.16);
        border: 2px solid rgba(22, 163, 74, 0.45);
    }
    .sepay-icon.failed {
        background: rgba(220, 38, 38, 0.16);
        border: 2px solid rgba(220, 38, 38, 0.45);
    }
    .sepay-icon.pending {
        background: rgba(234, 179, 8, 0.16);
        border: 2px solid rgba(234, 179, 8, 0.45);
    }
    .sepay-order-id {
        font-family: monospace;
        font-size: 13px;
        color: var(--ch-muted);
        margin-top: 12px;
        word-break: break-all;
    }
    .sepay-actions {
        display: flex;
        gap: 12px;
        justify-content: center;
        margin-top: 28px;
        flex-wrap: wrap;
    }
    .sepay-actions a {
        min-width: 180px;
    }
    .sepay-timer {
        font-size: 14px;
        color: var(--ch-muted);
        margin-top: 16px;
    }
    .sepay-spinner {
        width: 32px;
        height: 32px;
        border: 3px solid var(--ch-hairline);
        border-top-color: var(--ch-primary);
        border-radius: 50%;
        animation: sepaySpin 0.8s linear infinite;
        margin: 0 auto 16px;
    }
    @keyframes sepaySpin {
        to { transform: rotate(360deg); }
    }
</style>

<div class="sepay-status-card">
    <% if ("success".equalsIgnoreCase(status)) { %>
        <div class="sepay-icon success">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
                <path d="M20 6 9 17l-5-5" stroke="#4ade80" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </div>
        <h2 style="margin: 0 0 8px; font-size: 28px;">Thanh toán thành công</h2>
        <p style="margin: 0 0 8px; color: var(--ch-muted); font-size: 16px;">Giao dịch đã được xác nhận.</p>
        <% if (!orderId.isEmpty()) { %>
            <div class="sepay-order-id">Mã đơn hàng: <%= orderId %></div>
        <% } %>
        <div class="sepay-actions">
            <a class="home-cta" href="/product">Tiếp tục mua sắm</a>
            <a class="home-cta" style="text-decoration:none;" href="/cart">Về giỏ hàng</a>
        </div>
    <% } else if ("failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) { %>
        <div class="sepay-icon failed">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
                <path d="M18 6 6 18M6 6l12 12" stroke="#ef4444" stroke-width="2.8" stroke-linecap="round"/>
            </svg>
        </div>
        <h2 style="margin: 0 0 8px; font-size: 28px;">
            <%= "cancelled".equalsIgnoreCase(status) ? "Đã hủy thanh toán" : "Thanh toán thất bại" %>
        </h2>
        <% if (!message.isEmpty()) { %>
            <p style="margin: 0 0 8px; color: var(--ch-muted); font-size: 15px;"><%= message %></p>
        <% } %>
        <% if (!orderId.isEmpty()) { %>
            <div class="sepay-order-id">Mã đơn hàng: <%= orderId %></div>
        <% } %>
        <div class="sepay-actions">
            <a class="home-cta" href="/payment">Thử lại</a>
            <a class="home-cta" style="text-decoration:none;" href="/product">Về trang chủ</a>
        </div>
    <% } else { %>
        <div class="sepay-icon pending">
            <div class="sepay-spinner"></div>
        </div>
        <h2 style="margin: 0 0 8px; font-size: 28px;">Đang xử lý thanh toán</h2>
        <p style="margin: 0 0 8px; color: var(--ch-muted); font-size: 15px;">Vui lòng chờ trong giây lát...</p>
        <% if (!orderId.isEmpty()) { %>
            <div class="sepay-order-id">Mã đơn hàng: <%= orderId %></div>
        <% } %>
        <div class="sepay-timer">Tự động chuyển hướng sau <span id="countdown">5</span> giây</div>
        <div class="sepay-actions">
            <a class="home-cta" href="/product">Về trang chủ</a>
        </div>
    <% } %>
</div>

<% if ("pending".equalsIgnoreCase(status)) { %>
<script>
(function() {
    var count = 5;
    var el = document.getElementById('countdown');
    var timer = setInterval(function() {
        count--;
        if (el) el.textContent = count;
        if (count <= 0) {
            clearInterval(timer);
            window.location.href = '<%= redirectUrl %>';
        }
    }, 1000);
})();
</script>
<% } %>

<%@include file="../includes/layout-end.jsp" %>
