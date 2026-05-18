<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quản lý brand" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../../../layouts/header.jsp" />
<div class="toolbar"><h1 class="page-title">Thương hiệu</h1><a class="button" href="${pageContext.request.contextPath}/admin/brands?action=create">Thêm brand</a></div>
<section class="table-wrap"><table><thead><tr><th>ID</th><th>Tên</th><th>UserId</th><th>Description</th><th>Status</th><th></th></tr></thead><tbody><tr><td>b111...</td><td>Samsung</td><td>111...</td><td>Consumer SSD solutions</td><td><span class="badge success">ACTIVE</span></td><td><a class="button secondary" href="#">Sửa</a></td></tr></tbody></table></section>
<jsp:include page="../../../layouts/footer.jsp" />

