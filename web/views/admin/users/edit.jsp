<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Edit User" scope="request" />
<c:set var="activePage" value="admin-users" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-grid-2">
    <section class="admin-panel">
        <h2>Thông tin người dùng</h2>
        <form class="admin-form" action="${pageContext.request.contextPath}/admin/users" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="id" value="${userResult.user.id}">
            <label for="name">Tên</label>
            <input id="name" name="name" value="${userResult.user.name}" required>
            <label for="email">Email</label>
            <input id="email" type="email" name="email" value="${userResult.user.email}" required>
            <label for="dateOfBirth">Ngày sinh</label>
            <input id="dateOfBirth" type="date" name="dateOfBirth" value="${userResult.user.dateOfBirth}">
            <label for="role">Vai trò</label>
            <select id="role" name="role">
                <option value="USER" ${userResult.user.role == 'USER' ? 'selected' : ''}>USER</option>
                <option value="ADMIN" ${userResult.user.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
            </select>
            <label for="status">Trạng thái</label>
            <select id="status" name="status">
                <option value="ACTIVE" ${userResult.user.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                <option value="INACTIVE" ${userResult.user.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                <option value="BANNED" ${userResult.user.status == 'BANNED' ? 'selected' : ''}>BANNED</option>
                <option value="PENDING" ${userResult.user.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
            </select>
            <button class="button" type="submit">Lưu</button>
        </form>
    </section>
    <section class="admin-panel">
        <h2>Đơn hàng gần đây</h2>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Mã</th><th>Sản phẩm</th><th>Tổng</th><th>Trạng thái</th></tr></thead>
                <tbody>
                    <c:forEach var="order" items="${userOrders}">
                        <tr>
                            <td><c:out value="${order.id}" /></td>
                            <td><c:out value="${order.productName}" /></td>
                            <td><c:out value="${order.totalAmount}" /> VND</td>
                            <td><c:out value="${order.status}" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="admin-actions mt-4">
            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${userResult.user.id}">
                <button class="button danger" type="submit">Xóa người dùng</button>
            </form>
        </div>
    </section>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
