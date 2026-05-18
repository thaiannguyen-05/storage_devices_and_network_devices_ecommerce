<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Lịch sử đơn hàng" scope="request" />
<c:set var="activePage" value="orders" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<div class="toolbar">
    <h1 class="page-title">Lịch sử đơn hàng</h1>
    <form action="${pageContext.request.contextPath}/orders" method="get" class="field">
        <select name="status"><option value="">Tất cả trạng thái</option><option>PENDING</option><option>CONFIRMED</option><option>SHIPPING</option><option>COMPLETED</option><option>CANCELLED</option></select>
    </form>
</div>
<section class="table-wrap">
    <table>
        <thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Sản phẩm</th><th>Tổng tiền</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
        <tbody>
            <tr><td data-label="Mã đơn">ORD-0001</td><td data-label="Ngày đặt">18/05/2026</td><td data-label="Sản phẩm">Samsung 990 PRO</td><td data-label="Tổng tiền">3.490.000 VND</td><td data-label="Trạng thái"><span class="badge warning">PENDING</span></td><td data-label="Hành động"><a class="button secondary" href="${pageContext.request.contextPath}/order/detail?id=ORD-0001">Xem chi tiết</a></td></tr>
            <tr><td data-label="Mã đơn">ORD-0002</td><td data-label="Ngày đặt">17/05/2026</td><td data-label="Sản phẩm">TP-Link AX73</td><td data-label="Tổng tiền">2.890.000 VND</td><td data-label="Trạng thái"><span class="badge success">COMPLETED</span></td><td data-label="Hành động"><a class="button secondary" href="${pageContext.request.contextPath}/order/detail?id=ORD-0002">Xem chi tiết</a></td></tr>
        </tbody>
    </table>
</section>
<jsp:include page="../layouts/footer.jsp" />

