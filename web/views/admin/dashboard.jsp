<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Admin Dashboard" scope="request" />
<c:set var="activePage" value="admin-dashboard" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<script>
    document.body.classList.add("admin-dashboard-page");
    document.body.setAttribute("data-dashboard-api", "${pageContext.request.contextPath}/admin/dashboard/api/stats");
</script>
<section class="admin-stats-grid">
    <article class="admin-stat-card"><span>Tổng người dùng</span><strong id="statTotalUsers"><c:out value="${dashboardStats.totalUsers}" /></strong></article>
    <article class="admin-stat-card"><span>Tổng sản phẩm</span><strong id="statTotalProducts"><c:out value="${dashboardStats.totalProducts}" /></strong></article>
    <article class="admin-stat-card"><span>Tổng đơn hàng</span><strong id="statTotalOrders"><c:out value="${dashboardStats.totalOrders}" /></strong></article>
    <article class="admin-stat-card"><span>Tổng doanh thu</span><strong id="statTotalRevenue"><c:out value="${dashboardStats.totalRevenue}" /></strong></article>
    <article class="admin-stat-card"><span>Đơn hàng hoạt động</span><strong id="statActiveOrders"><c:out value="${dashboardStats.activeOrders}" /></strong></article>
</section>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Xu hướng doanh thu</h2>
        <span>Tự động cập nhật mỗi 30s</span>
    </div>
    <div class="admin-chart" id="adminRevenueChart"></div>
</section>
<section class="admin-grid-2">
    <section class="admin-panel">
        <div class="admin-panel-head">
            <h2>Đơn hàng gần đây</h2>
            <a class="button secondary" href="${pageContext.request.contextPath}/admin/orders?action=list">Xem tất cả</a>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Mã</th><th>Khách hàng</th><th>Tổng</th><th>Trạng thái</th><th>Ngày tạo</th></tr></thead>
                <tbody id="adminRecentOrdersBody">
                    <c:forEach var="order" items="${recentOrders}">
                        <tr>
                            <td><c:out value="${order.orderId}" /></td>
                            <td><c:out value="${order.customerName}" /></td>
                            <td><c:out value="${order.totalAmount}" /> VND</td>
                            <td><span class="badge"><c:out value="${order.status}" /></span></td>
                            <td><c:out value="${order.createdAt}" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
    <section class="admin-panel">
        <div class="admin-panel-head">
            <h2>Sản phẩm bán chạy</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Sản phẩm</th><th>Đã bán</th><th>Doanh thu</th></tr></thead>
                <tbody id="adminTopProductsBody">
                    <c:forEach var="product" items="${topProducts}">
                        <tr>
                            <td><c:out value="${product.productName}" /></td>
                            <td><c:out value="${product.totalSold}" /></td>
                            <td><c:out value="${product.revenue}" /> VND</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</section>
<script defer src="${pageContext.request.contextPath}/assets/js/admin-dashboard.js"></script>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
