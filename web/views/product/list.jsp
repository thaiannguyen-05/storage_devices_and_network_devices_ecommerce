<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="java.util.List" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Set" %>
<%@page import="module.bussiness.product.dto.ProductCardView" %>
<%
    request.setAttribute("pageTitle", "LinhNamStore | Nền tảng thiết bị lưu trữ");
%>
<%@include file="../includes/layout.jsp" %>
<%
    String pageTitle = "LinhNamStore | Nền tảng thiết bị lưu trữ";

    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    boolean adminView = isAdmin != null && isAdmin;
    Integer cartCount = (Integer) request.getAttribute("cartCount");
    int count = cartCount == null ? 0 : cartCount;
    String selectedCategory = (String) request.getAttribute("selectedCategory");
    Set<String> categories = (Set<String>) request.getAttribute("categories");
    String error = (String) request.getAttribute("error");
    String q = request.getParameter("q");
    if (q == null) q = "";
    boolean cartAdded = "1".equals(request.getParameter("cartAdded"));
    String contextPath = request.getContextPath();

    List<ProductCardView> cards = (List<ProductCardView>) request.getAttribute("productCards");
    if (cards == null) cards = new ArrayList<>();

    List<ProductCardView> featuredProducts = (List<ProductCardView>) request.getAttribute("featuredProducts");
    if (featuredProducts == null || featuredProducts.isEmpty()) featuredProducts = cards;

    int matchedCount = 0;
    String normalizedKeyword = q.trim().toLowerCase();
    for (ProductCardView card : cards) {
        if (!normalizedKeyword.isEmpty()) {
            String name = card.getName() == null ? "" : card.getName().toLowerCase();
            String category = card.getCategory() == null ? "" : card.getCategory().toLowerCase();
            if (!name.contains(normalizedKeyword) && !category.contains(normalizedKeyword)) {
                continue;
            }
        }
        matchedCount++;
    }
    String filterLabel = !q.trim().isEmpty() ? q.trim() : ((selectedCategory != null && !selectedCategory.isEmpty()) ? selectedCategory : "tất cả");

    java.util.Map<String, Integer> categoryCounts = (java.util.Map<String, Integer>) request.getAttribute("categoryCounts");
    if (categoryCounts == null) {
        categoryCounts = new java.util.LinkedHashMap<>();
    }
%>

<% if (error != null && !error.isEmpty()) { %>
    <div class="home-alert"><%= error %></div>
<% } %>

<section class="home-hero">
    <div class="home-hero-content">
        <p class="home-hero-tag">HIỆU SUẤT VƯỢT TRỘI - LƯU TRỮ BỀN BỈ</p>
        <h1>GIẢI PHÁP LƯU TRỮ <br><span>CHO MỌI NHU CẦU</span></h1>
        <p>Cung cấp thiết bị lưu trữ chính hãng từ các thương hiệu hàng đầu. Hiệu suất cao – An toàn dữ liệu – Bền bỉ theo thời gian.</p>
        <div class="home-hero-actions">
            <a href="#featured" class="home-cta">KHÁM PHÁ NGAY</a>
        </div>
    </div>
    <div class="home-hero-image">
        <img src="${pageContext.request.contextPath}/assets/images/hero_banner.png" alt="Storage Devices" fetchpriority="high">
    </div>
</section>

<section class="home-usp-top home-usp-priority">
    <div class="home-usp-item">
        <span class="home-usp-icon" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M12 2 4 5v6c0 5.55 3.84 10.74 8 12 4.16-1.26 8-6.45 8-12V5l-8-3zm0 2.18 6 2.25V11c0 4.32-2.86 8.62-6 9.93C8.86 19.62 6 15.32 6 11V6.43l6-2.25zm-1 9.41-1.71-1.7-1.41 1.41L11 16.41l5.12-5.12-1.41-1.41z" /></svg></span>
        <div><strong>Phần cứng chính hãng</strong><span>Đầy đủ CO/CQ, sẵn sàng cho kiểm định doanh nghiệp.</span></div>
    </div>
    <div class="home-usp-item">
        <span class="home-usp-icon" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M12 1a7 7 0 0 0-7 7v3.59L3.29 13.3a1 1 0 0 0-.29.7V19a2 2 0 0 0 2 2h3v-7H5V8a5 5 0 0 1 10 0v6h-3v7h7a2 2 0 0 0 2-2v-5a1 1 0 0 0-.29-.7L19 11.59V8a7 7 0 0 0-7-7z" /></svg></span>
        <div><strong>Bảo hành nhanh</strong><span>Hỗ trợ ngày làm việc kế tiếp cho hệ thống quan trọng.</span></div>
    </div>
    <div class="home-usp-item">
        <span class="home-usp-icon" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M20 8h-3V4H1v13h2a3 3 0 0 0 6 0h6a3 3 0 0 0 6 0h2v-5l-3-4zM6 18.5A1.5 1.5 0 1 1 7.5 17 1.5 1.5 0 0 1 6 18.5zM15 8v4h6.46l-2.25-3H15zm3 10.5a1.5 1.5 0 1 1 1.5-1.5 1.5 1.5 0 0 1-1.5 1.5z" /></svg></span>
        <div><strong>Giao hàng ưu tiên</strong><span>Ưu tiên vận chuyển cho toàn bộ node hạ tầng.</span></div>
    </div>
    <div class="home-usp-item">
        <span class="home-usp-icon" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M12 1a11 11 0 1 0 11 11A11 11 0 0 0 12 1zm0 2a9 9 0 0 1 7.94 4.77l-1.7.98A7 7 0 0 0 5.76 8.75l-1.7-.98A9 9 0 0 1 12 3zm0 18a9 9 0 0 1-7.94-4.77l1.7-.98a7 7 0 0 0 12.48 0l1.7.98A9 9 0 0 1 12 21zm-3-9a3 3 0 1 0 3-3 3 3 0 0 0-3 3z" /></svg></span>
        <div><strong>Tư vấn kỹ thuật</strong><span>Hỗ trợ chuyên sâu cho cấu hình RAID và ZFS.</span></div>
    </div>
</section>

<section id="categories" class="home-categories">
    <div class="home-section-head">
        <h2>Danh mục sản phẩm</h2>
        <a href="${pageContext.request.contextPath}/product<%= adminView ? "?admin=1" : "" %>#all-hardware">Xem tất cả</a>
    </div>
    <div class="home-category-grid">
        <% for (java.util.Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            String categoryKey = entry.getKey();
            int categoryCount = entry.getValue();
            String categoryLabel;
            if ("STORAGE_DEVICE".equalsIgnoreCase(categoryKey)) {
                categoryLabel = "Thiết bị lưu trữ";
            } else if ("NETWORK_DEVICE".equalsIgnoreCase(categoryKey)) {
                categoryLabel = "Thiết bị mạng";
            } else if ("ACCESSORY".equalsIgnoreCase(categoryKey)) {
                categoryLabel = "Phụ kiện";
            } else {
                categoryLabel = categoryKey.replace('_', ' ');
            }
        %>
        <a class="home-category-card" href="${pageContext.request.contextPath}/product?category=<%= categoryKey %><%= adminView ? "&admin=1" : "" %>#all-hardware">
            <span class="home-category-name"><%= categoryLabel %></span>
            <span class="home-category-count"><%= categoryCount %> sản phẩm</span>
        </a>
        <% } %>
    </div>
</section>

<section id="featured" class="home-featured">
    <div class="home-section-head">
        <h2>Sản phẩm nổi bật</h2>
        <a href="#all-hardware">Xem tất cả</a>
    </div>
    <div class="home-featured-slider">
        <button type="button" class="home-featured-nav prev" aria-label="Xem sản phẩm trước">‹</button>
        <div class="home-featured-track" id="featured-track">
            <% for (ProductCardView card : featuredProducts) { %>
            <article class="home-product-card featured-card" role="link" tabindex="0" onclick="window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'}">
                <div class="home-product-img-wrap">
                    <img class="home-product-img" src="<%= card.getImageUrl() %>" alt="device" loading="lazy" decoding="async">
                </div>
                <div class="home-product-content">
                    <h3><a href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>"><%= card.getName() %></a></h3>
                    <div class="product-extra">
                        <span>Thương hiệu: <strong><%= card.getBrandName() %></strong></span>
                        <span>Tồn kho: <strong><%= card.getTotalQuantity() %></strong></span>
                        <span>Đánh giá: <strong><%= String.format(java.util.Locale.US, "%.1f", card.getRating()) %></strong> ★</span>
                    </div>
                    <p class="home-price"><%= card.getPriceText() %></p>
                    <div class="home-actions">
                        <button type="button" class="btn-buy js-open-cart-popup" onclick="event.stopPropagation();" data-action="buyNow" data-product-id="<%= card.getId() %>" data-name="<%= card.getName() %>" data-category="<%= card.getCategory() %>" data-brand-id="<%= card.getBrandId() %>" data-variants='<%= card.getVariantsJson() %>'>MUA NGAY</button>
                        <button type="button" class="btn-cart js-open-cart-popup" onclick="event.stopPropagation();" data-action="addCart" data-product-id="<%= card.getId() %>" data-name="<%= card.getName() %>" data-category="<%= card.getCategory() %>" data-brand-id="<%= card.getBrandId() %>" data-variants='<%= card.getVariantsJson() %>'>+ GIỎ HÀNG</button>
                    </div>
                </div>
            </article>
            <% } %>
        </div>
        <button type="button" class="home-featured-nav next" aria-label="Xem sản phẩm tiếp">›</button>
    </div>
</section>

<% if (!q.trim().isEmpty() || (selectedCategory != null && !selectedCategory.trim().isEmpty())) { %>
<section class="home-result">
    <span>KẾT QUẢ: Tìm thấy <strong><%= matchedCount %></strong> sản phẩm cho "<%= filterLabel %>"</span>
    <span style="opacity: 0.5; font-family: monospace;">trạng thái: OK</span>
</section>
<% } %>

<section id="all-hardware" class="home-product-list">
    <div class="home-section-head">
        <h2>Toàn bộ sản phẩm</h2>
    </div>
    <div class="home-product-grid">
        <% if (!cards.isEmpty()) { for (ProductCardView card : cards) { if (!normalizedKeyword.isEmpty()) {
            String name = card.getName() == null ? "" : card.getName().toLowerCase();
            String category = card.getCategory() == null ? "" : card.getCategory().toLowerCase();
            if (!name.contains(normalizedKeyword) && !category.contains(normalizedKeyword)) { continue; } } %>
        <article class="home-product-card" role="link" tabindex="0" onclick="window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();window.location.href='${pageContext.request.contextPath}/product?id=<%= card.getId() %>'}">
            <div class="home-product-img-wrap">
                <img class="home-product-img" src="<%= card.getImageUrl() %>" alt="device" loading="lazy" decoding="async">
            </div>
            <div class="home-product-content">
                <h3><a href="${pageContext.request.contextPath}/product?id=<%= card.getId() %>"><%= card.getName() %></a></h3>
                <div class="product-extra">
                    <span>Thương hiệu: <strong><%= card.getBrandName() %></strong></span>
                    <span>Tồn kho: <strong><%= card.getTotalQuantity() %></strong></span>
                    <span>Đánh giá: <strong><%= String.format(java.util.Locale.US, "%.1f", card.getRating()) %></strong> ★</span>
                </div>
                <p class="home-price"><%= card.getPriceText() %></p>
                <div class="home-actions">
                    <button type="button" class="btn-buy js-open-cart-popup" onclick="event.stopPropagation();" data-action="buyNow" data-product-id="<%= card.getId() %>" data-name="<%= card.getName() %>" data-category="<%= card.getCategory() %>" data-brand-id="<%= card.getBrandId() %>" data-variants='<%= card.getVariantsJson() %>'>MUA NGAY</button>
                    <button type="button" class="btn-cart js-open-cart-popup" onclick="event.stopPropagation();" data-action="addCart" data-product-id="<%= card.getId() %>" data-name="<%= card.getName() %>" data-category="<%= card.getCategory() %>" data-brand-id="<%= card.getBrandId() %>" data-variants='<%= card.getVariantsJson() %>'>+ GIỎ HÀNG</button>
                </div>
            </div>
        </article>
        <% } } %>
    </div>
</section>

<!-- High Impact Yellow CTA Band -->
<section class="cta-band-yellow">
    <h2>Triển khai hạ tầng của bạn ngay hôm nay.</h2>
    <p style="margin-bottom: 32px; font-size: 18px;">Tham gia cùng hàng ngàn kỹ sư đang vận hành trên phần cứng LinhNamStore.</p>
    <a href="#all-hardware" class="btn-black">MUA SẮM NGAY</a>
</section>

<section class="home-usp-bottom">
    <div>Lưu trữ toàn diện</div>
    <div>An toàn & bền bỉ</div>
    <div>Tối ưu chi phí vận hành</div>
    <div>Sẵn sàng 24/7</div>
</section>

<% if (cartAdded) { %>
<div id="cartAddedToast" style="position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(22,163,74,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);transition:opacity .35s ease,transform .35s ease;">Thêm vào giỏ hàng thành công</div>
<% } %>

<div id="cartVariantModal" class="cart-variant-overlay">
    <div class="cart-variant-dialog">
        <div class="cart-variant-head">
            <h3 id="cartVariantTitle" class="cart-variant-title">Chọn phân loại</h3>
            <button type="button" id="cartVariantClose" class="cart-variant-close">Đóng</button>
        </div>
        <div class="cart-variant-body">
            <img id="cartVariantImage" class="cart-variant-image" src="" alt="variant">
            <div>
                <div id="cartVariantList" class="cart-variant-list"></div>
                <p id="cartVariantPrice" class="cart-variant-price">0 VND</p>
                <p id="cartVariantStock" class="cart-variant-stock">Tồn kho: 0</p>
                <label class="cart-variant-label">Số lượng</label>
                <div class="cart-variant-qty-wrap">
                    <input id="cartVariantQty" class="cart-variant-qty" type="number" min="1" value="1">
                    <div class="cart-variant-qty-buttons">
                        <button type="button" id="cartQtyPlus" class="cart-variant-qty-btn">+</button>
                        <button type="button" id="cartQtyMinus" class="cart-variant-qty-btn">-</button>
                    </div>
                </div>
            </div>
        </div>
        <form id="popupActionForm" class="cart-variant-form" method="post" action="${pageContext.request.contextPath}/cart?action=add">
            <input type="hidden" name="source" value="buyNow">
            <input type="hidden" name="productId">
            <input type="hidden" name="name">
            <input type="hidden" name="category">
            <input type="hidden" name="brandId">
            <input type="hidden" name="imageUrl">
            <input type="hidden" name="priceValue">
            <input type="hidden" name="stock">
            <input type="hidden" name="quantity">
            <input type="hidden" name="variantId">
            <input type="hidden" name="sku">
            <button type="submit" id="popupSubmitBtn" class="btn-buy cart-variant-submit">THÊM VÀO GIỎ</button>
        </form>
    </div>
</div>

<script>
(function () {
    const track = document.getElementById('featured-track');
    if (!track) return;
    const slider = track.closest('.home-featured-slider');
    const prevBtn = slider ? slider.querySelector('.home-featured-nav.prev') : null;
    const nextBtn = slider ? slider.querySelector('.home-featured-nav.next') : null;
    const getStep = function () { const card = track.querySelector('.featured-card'); if (!card) return 320; const style = window.getComputedStyle(track); const gap = parseFloat(style.columnGap || style.gap || '16') || 16; return card.getBoundingClientRect().width + gap; };
    const updateButtons = function () { if (!prevBtn || !nextBtn) return; const maxScroll = track.scrollWidth - track.clientWidth; prevBtn.disabled = track.scrollLeft <= 2; nextBtn.disabled = track.scrollLeft >= maxScroll - 2; };
    if (prevBtn) prevBtn.addEventListener('click', function () { track.scrollBy({ left: -getStep(), behavior: 'smooth' }); });
    if (nextBtn) nextBtn.addEventListener('click', function () { track.scrollBy({ left: getStep(), behavior: 'smooth' }); });
    track.addEventListener('scroll', updateButtons, { passive: true });
    window.addEventListener('resize', updateButtons);
    updateButtons();
})();

(function () {
    const hasQuery = "<%= q.trim() %>".length > 0;
    const hasCategoryFilter = "<%= selectedCategory == null ? "" : selectedCategory.trim() %>".length > 0;
    if (!hasQuery && !hasCategoryFilter) return;
    const target = document.getElementById('all-hardware');
    if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
})();

(function () {
    window.toggleAccountMenu = function (button) {
        const wrapper = button.closest('.home-account-menu');
        if (!wrapper) return;
        const dropdown = wrapper.querySelector('.home-account-dropdown');
        if (!dropdown) return;
        const isOpen = dropdown.style.display === 'block';
        document.querySelectorAll('.home-account-dropdown').forEach((el) => { el.style.display = 'none'; });
        dropdown.style.display = isOpen ? 'none' : 'block';
    };
    document.addEventListener('click', function (event) {
        if (!event.target.closest('.home-account-menu')) {
            document.querySelectorAll('.home-account-dropdown').forEach((el) => { el.style.display = 'none'; });
        }
    });

    const modal = document.getElementById('cartVariantModal');
    const closeBtn = document.getElementById('cartVariantClose');
    const titleEl = document.getElementById('cartVariantTitle');
    const listEl = document.getElementById('cartVariantList');
    const priceEl = document.getElementById('cartVariantPrice');
    const stockEl = document.getElementById('cartVariantStock');
    const qtyEl = document.getElementById('cartVariantQty');
    const qtyPlusBtn = document.getElementById('cartQtyPlus');
    const qtyMinusBtn = document.getElementById('cartQtyMinus');
    const imageEl = document.getElementById('cartVariantImage');
    const form = document.getElementById('popupActionForm');
    const submitBtn = document.getElementById('popupSubmitBtn');
    const openButtons = document.querySelectorAll('.js-open-cart-popup');
    const headerCartCount = document.getElementById('headerCartCount');

    let activeProduct = null;
    let activeVariant = null;
    let activeAction = 'addCart';

    const setField = (name, value) => { const input = form.querySelector('input[name="' + name + '"]'); if (input) input.value = value; };

    const resolveProductId = (button) => { if (button.dataset && button.dataset.productId) return button.dataset.productId; return button.getAttribute('data-product-id') || ''; };

    const chooseVariant = (variant, chip) => {
        activeVariant = variant;
        listEl.querySelectorAll('.cart-variant-chip').forEach((el) => { el.classList.remove('is-active'); });
        chip.classList.add('is-active');
        priceEl.textContent = variant.priceText || '0 VND';
        stockEl.textContent = 'Tồn kho: ' + (variant.stock || 0);
        qtyEl.max = Math.max(variant.stock || 1, 1);
        qtyEl.value = 1;
        setField('variantId', variant.variantId || '');
        setField('sku', variant.sku || '');
        setField('priceValue', variant.priceValue || '');
        setField('stock', variant.stock || 0);
        if (variant.imageUrl) { imageEl.src = variant.imageUrl; imageEl.style.display = 'block'; } else { imageEl.style.display = 'none'; }
    };

    const openModal = (button) => {
        activeAction = button.getAttribute('data-action') || 'addCart';
        const raw = button.getAttribute('data-variants') || '[]';
        let variants = [];
        try { variants = JSON.parse(raw); } catch (e) { variants = []; }
        if (!Array.isArray(variants) || variants.length === 0) { alert('Sản phẩm này chưa có phân loại để thao tác.'); return; }

        activeProduct = { productId: resolveProductId(button), name: (button.dataset && button.dataset.name) ? button.dataset.name : (button.getAttribute('data-name') || ''), category: (button.dataset && button.dataset.category) ? button.dataset.category : (button.getAttribute('data-category') || ''), brandId: (button.dataset && button.dataset.brandId) ? button.dataset.brandId : (button.getAttribute('data-brand-id') || '') };

        titleEl.textContent = activeProduct.name || 'Chọn phân loại';
        submitBtn.textContent = activeAction === 'buyNow' ? 'MUA NGAY' : 'THÊM VÀO GIỎ';
        listEl.innerHTML = '';

        let defaultVariant = variants.find(v => Number(v.stock || 0) > 0) || variants[0];
        variants.forEach((variant) => {
            const chip = document.createElement('button');
            chip.type = 'button';
            chip.className = 'cart-variant-chip';
            if (Number(variant.stock || 0) <= 0) chip.classList.add('is-disabled');
            chip.textContent = variant.sku || (variant.variantId || 'Mặc định');
            chip.addEventListener('click', () => chooseVariant(variant, chip));
            listEl.appendChild(chip);
            if (variant === defaultVariant) chooseVariant(variant, chip);
        });

        modal.style.display = 'flex';
    };

    openButtons.forEach((button) => { button.addEventListener('click', () => openModal(button)); });
    closeBtn.addEventListener('click', () => { modal.style.display = 'none'; });
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.style.display = 'none'; });

    qtyPlusBtn.addEventListener('click', () => { const max = Number(qtyEl.max || 1); let qty = Number(qtyEl.value || 1); qty = Math.min(max, qty + 1); qtyEl.value = qty; });
    qtyMinusBtn.addEventListener('click', () => { let qty = Number(qtyEl.value || 1); qty = Math.max(1, qty - 1); qtyEl.value = qty; });

    const showCartToast = function () {
        const oldToast = document.getElementById('cartAddedToast');
        if (oldToast && oldToast.parentNode) oldToast.parentNode.removeChild(oldToast);
        const toast = document.createElement('div');
        toast.id = 'cartAddedToast';
        toast.style.cssText = 'position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(22,163,74,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);transition:opacity .35s ease,transform .35s ease;';
        toast.textContent = 'Thêm vào giỏ hàng thành công';
        document.body.appendChild(toast);
        let dismissed = false;
        const dismiss = function () { if (dismissed || !toast.parentNode) return; dismissed = true; toast.style.opacity = '0'; toast.style.transform = 'translateX(-50%) translateY(-8px)'; window.removeEventListener('pointerdown', onPointerDown, true); setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 360); };
        const onPointerDown = function () { dismiss(); };
        window.addEventListener('pointerdown', onPointerDown, true);
        setTimeout(dismiss, 1000);
    };

    const showCartErrorToast = function (message) {
        const oldToast = document.getElementById('cartAddedToast');
        if (oldToast && oldToast.parentNode) oldToast.parentNode.removeChild(oldToast);
        const toast = document.createElement('div');
        toast.id = 'cartAddedToast';
        toast.style.cssText = 'position:fixed;top:22px;left:50%;transform:translateX(-50%);z-index:4000;background:rgba(220,38,38,.96);color:#fff;padding:12px 18px;border-radius:10px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.35);transition:opacity .35s ease,transform .35s ease;';
        toast.textContent = message || 'Không thể thêm vào giỏ hàng';
        document.body.appendChild(toast);
        let dismissed = false;
        const dismiss = function () { if (dismissed || !toast.parentNode) return; dismissed = true; toast.style.opacity = '0'; toast.style.transform = 'translateX(-50%) translateY(-8px)'; window.removeEventListener('pointerdown', onPointerDown, true); setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 360); };
        const onPointerDown = function () { dismiss(); };
        window.addEventListener('pointerdown', onPointerDown, true);
        setTimeout(dismiss, 1600);
    };

    form.addEventListener('submit', async (e) => {
        if (!activeProduct || !activeVariant) { e.preventDefault(); return; }
        const stock = Number(activeVariant.stock || 0);
        let qty = Number(qtyEl.value || 1);
        if (!Number.isFinite(qty) || qty < 1) qty = 1;
        if (stock > 0) qty = Math.min(qty, stock);

        setField('productId', activeProduct.productId);
        setField('name', activeProduct.name);
        setField('category', activeProduct.category);
        setField('brandId', activeProduct.brandId);
        setField('imageUrl', activeVariant.imageUrl || '');
        setField('quantity', qty);

        if (activeAction === 'buyNow') {
            e.preventDefault();
            modal.style.display = 'none';
            try {
                const params = new URLSearchParams();
                params.set('productId', activeProduct.productId);
                params.set('variantId', activeVariant.variantId || '');
                params.set('name', activeProduct.name);
                params.set('category', activeProduct.category);
                params.set('brandId', activeProduct.brandId);
                params.set('imageUrl', activeVariant.imageUrl || '');
                params.set('priceValue', activeVariant.priceValue || '');
                params.set('stock', stock);
                params.set('quantity', qty);
                params.set('sku', activeVariant.sku || '');
                const resp = await fetch('${pageContext.request.contextPath}/cart?action=buyNow', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' }, body: params.toString() });
                const data = resp.headers.get('content-type') && resp.headers.get('content-type').includes('application/json') ? await resp.json() : {};
                if (resp.redirected) {
                    window.location.href = resp.url;
                } else if (resp.ok && data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                } else if (data.loginUrl) {
                    window.location.href = data.loginUrl;
                } else {
                    showCartErrorToast(data.message || 'Không thể chuyển đến thanh toán');
                }
            } catch (err) { showCartErrorToast('Không thể chuyển đến thanh toán'); }
        } else {
            try {
                const resp = await fetch('${pageContext.request.contextPath}/cart?action=add', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' }, body: new URLSearchParams(new FormData(form)) });
                const data = resp.headers.get('content-type') && resp.headers.get('content-type').includes('application/json') ? await resp.json() : {};
                if (resp.ok) {
                    modal.style.display = 'none';
                    showCartToast();
                    if (headerCartCount) { headerCartCount.textContent = data.cartCount || (Number(headerCartCount.textContent || 0) + qty); }
                } else if (data.loginUrl) {
                    window.location.href = data.loginUrl;
                } else { showCartErrorToast(data.message || 'Không thể thêm vào giỏ hàng'); }
            } catch (err) { showCartErrorToast('Không thể thêm vào giỏ hàng'); }
            e.preventDefault();
        }
    });

    const dismissCartToast = function () {
        const toast = document.getElementById('cartAddedToast');
        if (!toast || !toast.parentNode) return;
        let dismissed = false;
        const dismiss = function () { if (dismissed || !toast.parentNode) return; dismissed = true; toast.style.opacity = '0'; toast.style.transform = 'translateX(-50%) translateY(-8px)'; window.removeEventListener('pointerdown', onPointerDown, true); setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 360); };
        const onPointerDown = function () { dismiss(); };
        window.addEventListener('pointerdown', onPointerDown, true);
        setTimeout(dismiss, 1000);
    };
    dismissCartToast();
})();
</script>

<%@include file="../includes/layout-end.jsp" %>
