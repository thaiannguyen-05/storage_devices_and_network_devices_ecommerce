<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="LinhNamStore - Trang chủ" scope="request" />
<c:set var="activePage" value="home" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<section class="hero">
    <div class="hero-copy">
        <h1>LinhNamStore</h1>
        <p>SSD, HDD, NAS, router và phụ kiện mạng được sắp xếp rõ ràng để dễ tìm, dễ so sánh và dễ mua.</p>
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
                                <span class="product-code">Mã: <c:out value="${item.id}" /></span>
                                <strong class="price"><c:out value="${item.price}" /> VND</strong>
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
                                <span class="product-code">Mã: DEMO-00${i}</span>
                                <strong class="price">${i + 1}.490.000 VND</strong>
                            </div>
                        </a>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section>
    <div class="section-title">
        <div>
            <h2>Bán chạy</h2>
            <p>Sản phẩm có số lượng đơn hàng cao.</p>
        </div>
    </div>
    <div class="grid product-grid">
        <article class="card product-card">
            <a href="${pageContext.request.contextPath}/product?id=p4444444-4444-4444-4444-444444444444">
                <div class="product-media"><img src="https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=900&q=80" alt="Router Wi-Fi 6"></div>
                <div class="product-body"><span class="badge warning">Bán chạy</span><h3 class="product-name">TP-Link Archer AX73 Wi-Fi 6</h3><span class="product-code">Mã: 44444444</span><strong class="price">2.890.000 VND</strong></div>
            </a>
        </article>
        <article class="card product-card">
            <a href="${pageContext.request.contextPath}/product?id=p3333333-3333-3333-3333-333333333333">
                <div class="product-media"><img src="https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=900&q=80" alt="NAS Synology"></div>
                <div class="product-body"><span class="badge warning">Bán chạy</span><h3 class="product-name">Synology DiskStation DS923+</h3><span class="product-code">Mã: 33333333</span><strong class="price">16.890.000 VND</strong></div>
            </a>
        </article>
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
                                <span class="badge success">Sản phẩm</span>
                                <h3 class="product-name"><c:out value="${item.name}" /></h3>
                                <span class="product-code">Mã: <c:out value="${item.id}" /></span>
                                <strong class="price"><c:out value="${item.price}" /> VND</strong>
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

<jsp:include page="../layouts/footer.jsp" />

