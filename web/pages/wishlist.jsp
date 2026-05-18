<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Sản phẩm yêu thích" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<div class="toolbar">
    <h1 class="page-title">Sản phẩm yêu thích</h1>
    <button class="button danger" type="button">Xóa tất cả</button>
</div>
<section class="grid product-grid">
    <article class="card product-card">
        <div class="product-media"><img src="https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=900&q=80" alt="Portable SSD"></div>
        <div class="product-body">
            <h3 class="product-name">SanDisk Extreme Portable SSD</h3>
            <strong class="price">2.490.000 VND</strong>
            <form action="${pageContext.request.contextPath}/cart" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="productId" value="p5555555-5555-5555-5555-555555555555">
                <input type="hidden" name="variantId" value="v5555555-5555-5555-5555-555555555551">
                <input type="hidden" name="quantity" value="1">
                <button class="button" type="submit">Thêm vào giỏ</button>
            </form>
            <button class="button secondary" type="button">Xóa</button>
        </div>
    </article>
</section>
<jsp:include page="../layouts/footer.jsp" />

