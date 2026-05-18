<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Form user" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Thêm / sửa user</h1>
<form class="panel form-grid" action="${pageContext.request.contextPath}/admin/users" method="post" data-validate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="${empty param.id ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${param.id}">
    <div class="field"><label>Name</label><input name="name" required><span class="error"></span></div>
    <div class="field"><label>Email</label><input type="email" name="email" required><span class="error"></span></div>
    <div class="field"><label>Password</label><input type="password" name="password" ${empty param.id ? 'required' : ''}><span class="error"></span></div>
    <div class="field"><label>Date of birth</label><input type="date" name="dateOfBirth"></div>
    <div class="field"><label>Role</label><select name="role"><option>USER</option><option>ADMIN</option></select></div>
    <div class="field"><label>Status</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option><option>BANNED</option><option>PENDING</option></select></div>
    <button class="button" type="submit">Lưu</button>
</form>
<jsp:include page="../layouts/footer.jsp" />

