<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Chi tiết sản phẩm" scope="request" />
<c:set var="activePage" value="products" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<c:if test="${not empty sessionScope.flashError}">
    <div class="panel" style="margin-bottom:16px; border-color:#dc2626; color:#991b1b;">
        <c:out value="${sessionScope.flashError}" />
    </div>
    <c:remove var="flashError" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="panel" style="margin-bottom:16px; border-color:#16a34a; color:#166534;">
        <c:out value="${sessionScope.flashSuccess}" />
    </div>
    <c:remove var="flashSuccess" scope="session" />
</c:if>

<section class="product-detail">
    <div>
        <div class="gallery-main">
            <img data-main-image src="${not empty variants ? variants[0].imageUrl : (empty productImage ? 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80' : productImage)}" alt="Anh sản phẩm">
        </div>
        <div class="thumbs">
            <button type="button" data-thumb="${not empty variants ? variants[0].imageUrl : 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=900&q=80'}"><img src="${not empty variants ? variants[0].imageUrl : 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=200&q=80'}" alt="Thumb 1"></button>
            <button type="button" data-thumb="https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=900&q=80"><img src="https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=200&q=80" alt="Thumb 2"></button>
        </div>
    </div>
    <div class="panel">
        <span class="badge">Storage device</span>
        <h1 class="page-title"><c:out value="${empty product.name ? 'Samsung 990 PRO NVMe SSD' : product.name}" /></h1>
        <p class="muted">Mã SP: <c:out value="${empty param.id ? 'DEMO-990PRO' : param.id}" /> - Thương hiệu: <a href="${pageContext.request.contextPath}/home?brand=Samsung">Samsung</a></p>
        <p><c:out value="${empty product.description ? 'PCIe 4.0 NVMe SSD cho gaming, workstation va creator workloads.' : product.description}" /></p>
        <div class="field">
            <label for="variantId">Chọn variant</label>
            <select id="variantId" name="variantId" form="addToCartForm" data-variant-select>
                <c:choose>
                    <c:when test="${not empty variants}">
                        <c:forEach var="variant" items="${variants}">
                            <option value="${variant.id}" data-price="${variant.price}" data-quantity="${variant.quantity}" data-image="${variant.imageUrl}"><c:out value="${variant.sku}" /> - <c:out value="${variant.quantity}" /> sản phẩm</option>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <option value="v1" data-price="3490000" data-quantity="24" data-image="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80">SAM-990PRO-1TB - 24 sản phẩm</option>
                    </c:otherwise>
                </c:choose>
            </select>
        </div>
        <p class="price" data-variant-price>
            <c:choose>
                <c:when test="${not empty variants}">
                    <c:out value="${variants[0].price}" /> VND
                </c:when>
                <c:otherwise>
                    3.490.000 VND
                </c:otherwise>
            </c:choose>
        </p>
        <p class="muted" data-variant-stock>
            <c:choose>
                <c:when test="${not empty variants}">
                    Còn <c:out value="${variants[0].quantity}" /> sản phẩm
                </c:when>
                <c:otherwise>
                    Còn 24 sản phẩm
                </c:otherwise>
            </c:choose>
        </p>
        <form id="addToCartForm" action="${pageContext.request.contextPath}/cart" method="post" class="form-grid">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="productId" value="${param.id}">
            <input type="hidden" name="variantId" value="${not empty variants ? variants[0].id : 'v1'}" data-variant-input>
            <div class="field">
                <label for="quantity">Số lượng</label>
                <div class="quantity-control">
                    <button type="button" class="qty-btn" data-qty-change="-1" aria-label="Giảm số lượng">−</button>
                    <input id="quantity" data-quantity type="number" name="quantity" min="1" max="24" value="1" readonly>
                    <button type="button" class="qty-btn" data-qty-change="1" aria-label="Tăng số lượng">+</button>
                </div>
            </div>
            <div class="field full">
                <button class="button" type="submit">Thêm vào giỏ</button>
                <a class="button secondary" href="${pageContext.request.contextPath}/checkout">Mua ngay</a>
            </div>
        </form>
    </div>
</section>

<div class="tabs" role="tablist">
    <button class="tab-button active" type="button" data-tab="desc">Mô tả chi tiết</button>
    <button class="tab-button" type="button" data-tab="spec">Thông số kỹ thuật</button>
    <button class="tab-button" type="button" data-tab="reviews">Đánh giá</button>
</div>
<section class="panel tab-panel active" data-tab-panel="desc">
    <p>Sản phẩm được tối ưu cho tốc độ đọc ghi cao, độ bền tốt và bảo hành chính hãng.</p>
</section>
<section class="panel tab-panel" data-tab-panel="spec">
    <table><tbody><tr><th>Giáo tiếp</th><td data-label="Giáo tiếp">PCIe 4.0 NVMe</td></tr><tr><th>Dung lượng</th><td data-label="Dung lượng">1TB / 2TB / 4TB</td></tr><tr><th>Bảo hành</th><td data-label="Bảo hành">60 thang</td></tr></tbody></table>
</section>
<section class="panel tab-panel" data-tab-panel="reviews">
    <form action="${pageContext.request.contextPath}/product" method="post" data-validate class="form-grid">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="review">
        <input type="hidden" name="productId" value="${param.id}">
        <div class="field"><label>Tên</label><input name="reviewerName" required><span class="error"></span></div>
        <div class="field"><label>Rating</label><select name="rating"><option>5</option><option>4</option><option>3</option><option>2</option><option>1</option></select></div>
        <div class="field full"><label>Nhận xét</label><textarea name="comment" required></textarea><span class="error"></span></div>
        <button class="button" type="submit">Gửi đánh giá</button>
    </form>
</section>

<jsp:include page="../layouts/footer.jsp" />

