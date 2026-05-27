<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%!
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0 ₫";
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN")).format(price) + " ₫";
    }
%>
<c:set var="pageTitle" value="LinhNamStore - Trang chủ" scope="request" />
<c:set var="activePage" value="home" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<section class="hero">
    <div class="hero-copy">
        <h1>LinhNamStore</h1>
        <p>Chuỗi hệ thống bán lẻ thiết bị mạng và lưu trữ hàng đầu Việt Nam</p>
        <div>
            <a class="button" href="#products">Xem sản phẩm</a>
            <a class="button secondary" href="${pageContext.request.contextPath}/contact">Cần tư vấn</a>
        </div>
    </div>
    <div class="hero-media" aria-hidden="true"></div>
</section>

<c:if test="${not empty success}">
    <div class="panel mb-4"><span class="badge success"><c:out value="${success}" /></span></div>
</c:if>

<section>
    <div class="section-title">
        <div>
            <h2>Hàng mới về</h2>
            <p>Cập nhật theo ngày tạo sản phẩm.</p>
        </div>
    </div>
    <div class="grid product-grid">
        <c:choose>
            <c:when test="${not empty newProducts}">
                <c:forEach var="item" items="${newProducts}">
                    <article class="card product-card">
                        <a href="${pageContext.request.contextPath}/product?id=${item.id}">
                            <div class="product-media"><img src="${item.imageUrl}" alt="${item.name}"></div>
                            <div class="product-body">
                                <span class="badge success">Mới</span>
                                <h3 class="product-name"><c:out value="${item.name}" /></h3>
                                <strong class="price"><%= formatPrice(((module.bussiness.product.ProductCardView)pageContext.getAttribute("item")).getPrice()) %></strong>
                            </div>
                        </a>
                    </article>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <c:set var="demoImage" value="https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=900&q=80" />
                <c:forEach begin="1" end="4" var="i">
                    <article class="card product-card">
                        <a href="${pageContext.request.contextPath}/product?id=demo-${i}">
                            <div class="product-media"><img src="${demoImage}" alt="SSD LinhNamStore"></div>
                            <div class="product-body">
                                <span class="badge success">Mới</span>
                                <h3 class="product-name">LinhNamStore NVMe SSD Gen4 ${i}TB</h3>
                                <strong class="price">${i + 1}.490.000 ₫</strong>
                            </div>
                        </a>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section id="products">
    <div class="section-title">
        <div>
            <h2><c:out value="${isFiltered ? 'Kết quả bộ lọc' : 'Tất cả sản phẩm'}" /></h2>
            <c:choose>
                <c:when test="${isFiltered}">
                    <p>Tìm thấy <c:out value="${not empty products ? products.size() : 0}" /> sản phẩm phù hợp.</p>
                </c:when>
                <c:otherwise>
                    <p>Khám phá bộ sưu tập đầy đủ các thiết bị lưu trữ và thiết bị mạng chất lượng cao.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="grid product-grid">
        <c:choose>
            <c:when test="${not empty products}">
                <c:forEach var="item" items="${products}">
                    <article class="card product-card">
                        <a href="${pageContext.request.contextPath}/product?id=${item.id}">
                            <div class="product-media">
                                <img src="${empty item.imageUrl ? 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=600&q=80' : item.imageUrl}" alt="${item.name}">
                            </div>
                            <div class="product-body">
                                <h3 class="product-name"><c:out value="${item.name}" /></h3>
                                <strong class="price"><%= formatPrice(((module.bussiness.product.ProductCardView)pageContext.getAttribute("item")).getPrice()) %></strong>
                            </div>
                        </a>
                    </article>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="field full empty-state">
                    <p class="muted">Không có sản phẩm nào phù hợp với bộ lọc của bạn.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%-- Pagination --%>
<%
    module.bussiness.product.FilterResult fr = (module.bussiness.product.FilterResult) request.getAttribute("filterResult");
    boolean isFiltered = request.getAttribute("isFiltered") != null && (Boolean)request.getAttribute("isFiltered");
    Integer pgTotalPages = null;
    Integer pgCurrentPage = null;
    if (isFiltered && fr != null) {
        pgTotalPages = fr.getTotalPages();
        pgCurrentPage = fr.getPage();
    } else {
        Integer productsTotal = (Integer) request.getAttribute("productsTotal");
        if (productsTotal != null && productsTotal > 0) {
            pgTotalPages = (productsTotal + 9) / 10;
            String pageParam = request.getParameter("page");
            pgCurrentPage = pageParam != null ? Integer.parseInt(pageParam) : 1;
        }
    }
    if (pgTotalPages != null && pgTotalPages > 1) {
        request.setAttribute("pg_totalPages", pgTotalPages);
        request.setAttribute("pg_currentPage", pgCurrentPage);
%>
<jsp:include page="../layouts/pagination.jsp" />
<% } %>

<jsp:include page="../layouts/footer.jsp" />

