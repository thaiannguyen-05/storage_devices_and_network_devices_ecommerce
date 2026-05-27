<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
    </main>
</div>
<footer class="site-footer">
    <div class="footer-grid">
        <section>
            <h3>LinhNamStore</h3>
            <p>Thiết bị lưu trữ, NAS và network cho học tập, làm việc và vận hành cửa hàng nhỏ.</p>
        </section>
        <section>
            <h3>Link nhanh</h3>
            <ul>
                <li><a href="${ctx}/home">Trang chủ</a></li>
                <li><a href="${ctx}/contact">Liên hệ</a></li>
                <li><a href="${ctx}/about">Giới thiệu</a></li>
                <li><a href="${ctx}/home#policy">Chính sách</a></li>
            </ul>
        </section>
        <section>
            <h3>Danh mục</h3>
            <ul>
                <li><a href="${ctx}/home#products">Storage device</a></li>
                <li><a href="${ctx}/home#products">Network device</a></li>
                <li><a href="${ctx}/home#products">Accessory</a></li>
                <li><a href="${ctx}/cart">Giỏ hàng</a></li>
            </ul>
        </section>
        <section>
            <h3>Nhóm thực hiện</h3>
            <ul>
                <li>Nguyễn Thái An</li>
                <li>Lê Hữu Hoàng</li>
                <li>Nguyễn Phúc Hưng</li>
            </ul>
        </section>
    </div>
    <div class="footer-bottom">2026 LinhNamStore. Jakarta EE JSP/Servlet project.</div>
</footer>

<!-- Floating AI Chat Widget -->
<div class="chat-widget" data-chat-widget data-endpoint="${ctx}/ai-chat" data-csrf-token="${sessionScope.csrfToken}">
    <button class="chat-widget-toggle" type="button" data-chat-toggle aria-label="Mở trợ lý AI">
        <svg class="chat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <svg class="close-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" hidden>
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
    </button>
    <div class="chat-panel" data-chat-panel hidden>
        <div class="chat-panel-header">
            <h3>Trợ lý AI LinhNamStore</h3>
            <button class="chat-panel-close" type="button" data-chat-close aria-label="Đóng">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
            </button>
        </div>
        <div class="chat-panel-body">
            <div class="chat-messages" id="chatMessages" aria-live="polite">
                <article class="chat-message chat-message--model">
                    <div class="chat-bubble">Xin chào, mình có thể giúp bạn tìm sản phẩm phù hợp?</div>
                </article>
            </div>
            <div class="chat-status" data-chat-status></div>
        </div>
        <form class="chat-composer" data-chat-form>
            <textarea id="chatInput" name="prompt" rows="1" maxlength="4000" placeholder="Nhập câu hỏi..." data-chat-input></textarea>
            <div class="chat-actions">
                <button class="chat-btn" type="button" data-chat-stop hidden>Dừng</button>
                <button class="chat-btn chat-btn--primary" type="submit" data-chat-send>Gửi</button>
            </div>
        </form>
    </div>
</div>

<script defer src="${ctx}/assets/js/ai-chat.js"></script>
</body>
</html>