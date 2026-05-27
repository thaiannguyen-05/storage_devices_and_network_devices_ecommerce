<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Order Detail" scope="request" />
<c:set var="activePage" value="admin-orders" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-grid-2">
    <section class="admin-panel">
        <h2>Tóm tắt đơn hàng</h2>
        <dl class="admin-detail-list">
            <div><dt>Mã đơn</dt><dd><c:out value="${order.orderId}" /></dd></div>
            <div><dt>Khách hàng</dt><dd><c:out value="${order.customerName}" /></dd></div>
            <div><dt>Email</dt><dd><c:out value="${order.email}" /></dd></div>
            <div><dt>Số điện thoại</dt><dd><c:out value="${order.phone}" /></dd></div>
            <div><dt>Địa chỉ</dt><dd><c:out value="${order.address}" /></dd></div>
            <div><dt>Sản phẩm</dt><dd><c:out value="${order.productName}" /></dd></div>
            <div><dt>Số lượng</dt><dd><c:out value="${order.quantity}" /></dd></div>
            <div><dt>Tổng</dt><dd><c:out value="${order.totalAmount}" /> VND</dd></div>
            <div><dt>Trạng thái</dt><dd><c:out value="${order.status}" /></dd></div>
            <div><dt>Thanh toán</dt><dd><c:out value="${empty order.paymentStatus ? order.paymentMethod : order.paymentStatus}" /></dd></div>
            <div><dt>Ngày tạo</dt><dd><c:out value="${order.createdAt}" /></dd></div>
        </dl>
        <c:if test="${not empty order.note}">
            <div class="admin-note-box">
                <h3>Ghi chú khách hàng</h3>
                <p><c:out value="${order.note}" /></p>
            </div>
        </c:if>
    </section>
    <section class="admin-panel">
        <h2>Update Status</h2>
        <form class="admin-form" action="${pageContext.request.contextPath}/admin/orders" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="updateStatus">
            <input type="hidden" name="id" value="${order.orderId}">
            <label for="status">Order status</label>
            <select id="status" name="status">
                <option value="PENDING" ${order.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                <option value="CONFIRMED" ${order.status == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                <option value="SHIPPING" ${order.status == 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
                <option value="COMPLETED" ${order.status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                <option value="CANCELLED" ${order.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
            <button class="button" type="submit">Save</button>
        </form>
        <form class="admin-form mt-4" action="${pageContext.request.contextPath}/admin/orders" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="cancel">
            <input type="hidden" name="id" value="${order.orderId}">
            <button class="button danger" type="submit">Cancel Order</button>
        </form>
    </section>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
