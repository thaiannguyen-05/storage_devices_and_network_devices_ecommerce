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
    <article class="admin-stat-card"><span>Total Users</span><strong id="statTotalUsers"><c:out value="${dashboardStats.totalUsers}" /></strong></article>
    <article class="admin-stat-card"><span>Total Products</span><strong id="statTotalProducts"><c:out value="${dashboardStats.totalProducts}" /></strong></article>
    <article class="admin-stat-card"><span>Total Orders</span><strong id="statTotalOrders"><c:out value="${dashboardStats.totalOrders}" /></strong></article>
    <article class="admin-stat-card"><span>Total Revenue</span><strong id="statTotalRevenue"><c:out value="${dashboardStats.totalRevenue}" /></strong></article>
    <article class="admin-stat-card"><span>Active Orders</span><strong id="statActiveOrders"><c:out value="${dashboardStats.activeOrders}" /></strong></article>
</section>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Revenue Trend</h2>
        <span>Auto refresh every 30s</span>
    </div>
    <div class="admin-chart" id="adminRevenueChart"></div>
</section>
<section class="admin-grid-2">
    <section class="admin-panel">
        <div class="admin-panel-head">
            <h2>Recent Orders</h2>
            <a class="button secondary" href="${pageContext.request.contextPath}/admin/orders?action=list">View all</a>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>ID</th><th>Customer</th><th>Total</th><th>Status</th><th>Created</th></tr></thead>
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
            <h2>Top Products</h2>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Product</th><th>Sold</th><th>Revenue</th></tr></thead>
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
