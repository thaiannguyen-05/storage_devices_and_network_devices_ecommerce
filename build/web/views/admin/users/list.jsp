<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quản lý user" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../../../layouts/header.jsp" />
<div class="toolbar"><h1 class="page-title">User</h1><a class="button" href="${pageContext.request.contextPath}/admin/users?action=create">Thêm user</a></div>
<section class="table-wrap"><table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>CreatedAt</th><th></th></tr></thead><tbody><tr><td>111...</td><td>LinhNamStore Admin</td><td>admin@linhnamstore.local</td><td>ADMIN</td><td><span class="badge success">ACTIVE</span></td><td>18/05/2026</td><td><a class="button secondary" href="#">Sửa</a></td></tr></tbody></table></section>
<jsp:include page="../../../layouts/footer.jsp" />

