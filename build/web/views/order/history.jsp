<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Lịch sử đơn hàng" scope="request" />
<c:set var="activePage" value="orders" scope="request" />
<jsp:include page="../../layouts/header.jsp" />
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
            <c:forEach var="order" items="${ordersResult.orders}">
                <tr>
                    <td>${order.id.substring(0, 8)}</td>
                    <td>${order.createdAt}</td>
                    <td><c:out value="${order.productName}" /> x${order.quantity}</td>
                    <td><c:out value="${order.totalAmount}" /> đ</td>
                    <td>
                        <c:choose>
                            <c:when test="${order.status == 'PENDING'}">
                                <span class="badge warning">PENDING</span>
                            </c:when>
                            <c:when test="${order.status == 'PAID'}">
                                <span class="badge success" style="background: #10b981; color: #fff;">PAID</span>
                            </c:when>
                            <c:when test="${order.status == 'COMPLETED'}">
                                <span class="badge success">COMPLETED</span>
                            </c:when>
                            <c:when test="${order.status == 'CANCELLED'}">
                                <span class="badge danger">CANCELLED</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge secondary">${order.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="button secondary" href="${pageContext.request.contextPath}/order/detail?id=${order.id}">Xem chi tiết</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty ordersResult.orders}">
                <tr>
                    <td colspan="6" style="text-align: center; padding: 24px; color: var(--color-text-secondary);">Bạn chưa có đơn hàng nào.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</section>
<jsp:include page="../../layouts/footer.jsp" />

