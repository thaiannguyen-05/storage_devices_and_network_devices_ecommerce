<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Chi tiết sản phẩm" scope="request" />
<c:set var="activePage" value="products" scope="request" />
<jsp:include page="../../layouts/header.jsp" />

<section class="product-detail">
    <div>
        <div class="gallery-main">
            <img data-main-image src="${empty productImage ? 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80' : productImage}" alt="Anh sản phẩm">
        </div>
        <div class="thumbs">
            <button type="button" data-thumb="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=900&q=80"><img src="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=200&q=80" alt="Thumb 1"></button>
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
            <select id="variantId" name="variantId" data-variant-select>
                <option value="v1" data-price="3490000" data-quantity="24" data-image="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80">SAM-990PRO-1TB - 24 sản phẩm</option>
                <option value="v2" data-price="5990000" data-quantity="18" data-image="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80">SAM-990PRO-2TB - 18 sản phẩm</option>
            </select>
        </div>
        <p class="price" data-variant-price>3.490.000 VND</p>
        <p class="muted" data-variant-stock>Còn 24 sản phẩm</p>
        <form action="${pageContext.request.contextPath}/cart" method="post" class="form-grid">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="productId" value="${param.id}">
            <div class="field">
                <label for="quantity">Số lượng</label>
                <input id="quantity" data-quantity type="number" name="quantity" min="1" max="24" value="1">
            </div>
            <div class="field full">
                <button class="button" type="submit">Thêm vào giỏ</button>
                <a class="button secondary" href="${pageContext.request.contextPath}/checkout">Mua ngay</a>
                <button class="button secondary" type="button">Lưu sản phẩm</button>
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
    <table><tbody><tr><th>Giáo tiếp</th><td>PCIe 4.0 NVMe</td></tr><tr><th>Dung lượng</th><td>1TB / 2TB / 4TB</td></tr><tr><th>Bảo hành</th><td>60 thang</td></tr></tbody></table>
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

<jsp:include page="../../layouts/footer.jsp" />

