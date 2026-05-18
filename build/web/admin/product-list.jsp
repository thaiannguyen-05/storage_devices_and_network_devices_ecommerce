<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quản lý sản phẩm" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<div class="toolbar"><h1 class="page-title">Sản phẩm</h1><a class="button" href="${pageContext.request.contextPath}/admin/products?action=create">Thêm sản phẩm</a></div>
<section class="table-wrap">
    <table><thead><tr><th>ID</th><th>Tên</th><th>Category</th><th>Brand</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody><tr><td data-label="ID">p111...</td><td data-label="Tên">Samsung 990 PRO NVMe SSD</td><td data-label="Category">STORAGE_DEVICE</td><td data-label="Brand">Samsung</td><td data-label="Status"><span class="badge success">ACTIVE</span></td><td data-label="Actions" class="table-actions"><a class="button secondary" href="${pageContext.request.contextPath}/admin/products?action=edit&id=p111">Sửa</a><button class="button danger">Xóa</button></td></tr></tbody>
    </table>
</section>
<jsp:include page="../layouts/footer.jsp" />

