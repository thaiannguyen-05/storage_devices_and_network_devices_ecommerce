<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Create User" scope="request" />
<c:set var="activePage" value="admin-users" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel admin-panel--narrow">
    <h2>Tạo người dùng</h2>
    <form class="admin-form" action="${pageContext.request.contextPath}/admin/users" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="create">
        <label for="name">Tên</label>
        <input id="name" name="name" required>
        <label for="email">Email</label>
        <input id="email" type="email" name="email" required>
        <label for="password">Mật khẩu</label>
        <input id="password" type="password" name="password" required>
        <label for="dateOfBirth">Ngày sinh</label>
        <input id="dateOfBirth" type="date" name="dateOfBirth">
        <label for="role">Vai trò</label>
        <select id="role" name="role">
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
        </select>
        <label for="status">Trạng thái</label>
        <select id="status" name="status">
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="BANNED">BANNED</option>
            <option value="PENDING">PENDING</option>
        </select>
        <button class="button" type="submit">Tạo người dùng</button>
    </form>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
