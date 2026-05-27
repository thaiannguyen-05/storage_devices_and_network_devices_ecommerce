<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%!
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0 ₫";
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN")).format(price) + " ₫";
    }
%>
<c:set var="pageTitle" value="San pham yeu thich" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<c:if test="${not empty sessionScope.flashSuccess}"><div class="panel mb-4"><span class="badge success"><c:out value="${sessionScope.flashSuccess}" /></span></div><c:remove var="flashSuccess" scope="session" /></c:if>
<c:if test="${not empty sessionScope.flashError}"><div class="panel mb-4"><span class="badge danger"><c:out value="${sessionScope.flashError}" /></span></div><c:remove var="flashError" scope="session" /></c:if>
<div class="toolbar">
    <h1 class="page-title">San pham yeu thich</h1>
    <form action="${pageContext.request.contextPath}/wishlist" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="clear">
        <button class="button danger" type="submit">Xoa tat ca</button>
    </form>
</div>
<section class="grid product-grid">
    <c:choose>
        <c:when test="${not empty wishlistProducts}">
            <c:forEach var="item" items="${wishlistProducts}">
                <article class="card product-card">
                    <div class="product-media"><img src="${item.imageUrl}" alt="${item.productName}"></div>
                    <div class="product-body">
                        <h3 class="product-name"><c:out value="${item.productName}" /></h3>
                        <strong class="price"><%= formatPrice(((module.bussiness.wishlist.WishlistItemView)pageContext.getAttribute("item")).getPrice()) %></strong>
                        <form action="${pageContext.request.contextPath}/cart" method="post">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="${item.productId}">
                            <input type="hidden" name="variantId" value="${item.variantId}">
                            <input type="hidden" name="quantity" value="1">
                            <button class="button" type="submit">Them vao gio</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/wishlist" method="post">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="productId" value="${item.productId}">
                            <button class="button secondary" type="submit">Xoa</button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div class="panel empty-state">
                <p class="muted">Chua co san pham yeu thich.</p>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<jsp:include page="../layouts/footer.jsp" />
