<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="User Management" scope="request" />
<c:set var="activePage" value="admin-users" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Quản lý người dùng</h2>
        <a class="button" href="${pageContext.request.contextPath}/admin/users?action=create">Tạo người dùng</a>
    </div>
    <form class="admin-filterbar" action="${pageContext.request.contextPath}/admin/users" method="get">
        <input type="hidden" name="action" value="list">
        <input type="search" name="keyword" value="${keyword}" placeholder="Tìm theo id, tên, email">
        <select name="role">
            <option value="">Tất cả vai trò</option>
            <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
            <option value="USER" ${selectedRole == 'USER' ? 'selected' : ''}>USER</option>
        </select>
        <select name="status">
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
            <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
            <option value="BANNED" ${selectedStatus == 'BANNED' ? 'selected' : ''}>BANNED</option>
            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
        </select>
        <button class="button" type="submit">Lọc</button>
    </form>
    <div class="admin-kpis">
        <c:forEach var="entry" items="${userStats.roles}">
            <span class="admin-pill"><strong>Vai trò ${entry.key}</strong> <c:out value="${entry.value}" /></span>
        </c:forEach>
        <c:forEach var="entry" items="${userStats.statuses}">
            <span class="admin-pill"><strong>Trạng thái ${entry.key}</strong> <c:out value="${entry.value}" /></span>
        </c:forEach>
    </div>
    <div class="table-wrap">
        <table>
            <thead><tr><th>Mã</th><th>Tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
            <tbody>
                <c:forEach var="user" items="${usersResult.users}">
                    <tr>
                        <td><c:out value="${user.id}" /></td>
                        <td><c:out value="${user.name}" /></td>
                        <td><c:out value="${user.email}" /></td>
                        <td><span class="badge info"><c:out value="${user.role}" /></span></td>
                        <td><span class="badge ${user.status == 'ACTIVE' ? 'success' : user.status == 'BANNED' ? 'danger' : 'warning'}"><c:out value="${user.status}" /></span></td>
                        <td><c:out value="${user.createdAt}" /></td>
                        <td class="admin-actions">
                            <a class="button secondary" href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}">Sửa</a>
                            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="action" value="update-status">
                                <input type="hidden" name="id" value="${user.id}">
                                <input type="hidden" name="status" value="${user.status == 'BANNED' ? 'ACTIVE' : 'BANNED'}">
                                <button class="button ${user.status == 'BANNED' ? 'secondary' : 'danger'}" type="submit">${user.status == 'BANNED' ? 'Kích hoạt' : 'Cấm'}</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="admin-pagination">
        <c:forEach var="pageNo" begin="1" end="${totalPages}">
            <a class="${pageNo == currentPage ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users?action=list&page=${pageNo}&role=${selectedRole}&status=${selectedStatus}&keyword=${keyword}"><c:out value="${pageNo}" /></a>
        </c:forEach>
    </div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
