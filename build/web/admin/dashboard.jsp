<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Admin Dashboard" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Admin dashboard</h1>
<section class="grid stats-grid mb-4">
    <article class="card stat-card">Tổng user<strong>128</strong></article>
    <article class="card stat-card">Tổng đơn hàng<strong>64</strong></article>
    <article class="card stat-card">Doanh thu<strong>86.4M</strong></article>
    <article class="card stat-card">Sản phẩm<strong>30+</strong></article>
</section>
<section class="panel mb-4">
    <h2>Doanh thu 7 ngày</h2>
    <div class="bar-chart"><span style="height:40%"></span><span style="height:55%"></span><span style="height:35%"></span><span style="height:76%"></span><span style="height:66%"></span><span style="height:88%"></span><span style="height:72%"></span></div>
</section>
<section class="table-wrap">
    <table><thead><tr><th>Mã đơn</th><th>User</th><th>Tổng tiền</th><th>Trạng thái</th></tr></thead><tbody><tr><td data-label="Mã đơn">ORD-0001</td><td data-label="User">admin@linhnamstore.local</td><td data-label="Tổng tiền">3.490.000 VND</td><td data-label="Trạng thái"><span class="badge warning">PENDING</span></td></tr></tbody></table>
</section>
<jsp:include page="../layouts/footer.jsp" />

