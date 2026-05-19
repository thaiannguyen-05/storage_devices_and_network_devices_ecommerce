<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Order Management" scope="request" />
<c:set var="activePage" value="admin-orders" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <form class="admin-filterbar" action="${pageContext.request.contextPath}/admin/orders" method="get">
        <input type="hidden" name="action" value="list">
        <input type="search" name="keyword" value="${keyword}" placeholder="Search order, customer, email">
        <select name="status">
            <option value="">All status</option>
            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
            <option value="CONFIRMED" ${selectedStatus == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
            <option value="SHIPPING" ${selectedStatus == 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
        </select>
        <button class="button" type="submit">Filter</button>
    </form>
    <div class="admin-kpis">
        <c:forEach var="entry" items="${statusCounts}">
            <span class="admin-pill"><strong><c:out value="${entry.key}" /></strong> <c:out value="${entry.value}" /></span>
        </c:forEach>
    </div>
    <div class="table-wrap">
        <table>
            <thead><tr><th>ID</th><th>Customer</th><th>Email</th><th>Product</th><th>Total</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead>
            <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td><c:out value="${order.orderId}" /></td>
                        <td><c:out value="${order.customerName}" /></td>
                        <td><c:out value="${order.email}" /></td>
                        <td><c:out value="${order.productName}" /></td>
                        <td><c:out value="${order.totalAmount}" /> VND</td>
                        <td>
                            <span class="badge ${order.status == 'COMPLETED' ? 'success' : order.status == 'CONFIRMED' || order.status == 'SHIPPING' ? 'info' : order.status == 'CANCELLED' ? 'danger' : 'warning'}"><c:out value="${order.status}" /></span>
                        </td>
                        <td><c:out value="${order.createdAt}" /></td>
                        <td class="admin-actions">
                            <a class="button secondary" href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${order.orderId}">Detail</a>
                            <form action="${pageContext.request.contextPath}/admin/orders" method="post">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="action" value="cancel">
                                <input type="hidden" name="id" value="${order.orderId}">
                                <button class="button danger" type="submit">Cancel</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="admin-pagination">
        <c:forEach var="pageNo" begin="1" end="${totalPages}">
            <a class="${pageNo == currentPage ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders?action=list&page=${pageNo}&status=${selectedStatus}&keyword=${keyword}"><c:out value="${pageNo}" /></a>
        </c:forEach>
    </div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
