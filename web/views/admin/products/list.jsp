<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Product Management" scope="request" />
<c:set var="activePage" value="admin-products" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Products</h2>
        <a class="button" href="${pageContext.request.contextPath}/admin/products?action=create">Create product</a>
    </div>
    <div class="table-wrap">
        <table>
            <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Brand</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody><tr><td>p111...</td><td>Samsung 990 PRO NVMe SSD</td><td>STORAGE_DEVICE</td><td>Samsung</td><td><span class="badge success">ACTIVE</span></td><td class="admin-actions"><a class="button secondary" href="${pageContext.request.contextPath}/admin/products?action=edit&id=p111">Edit</a><button class="button danger" type="button">Delete</button></td></tr></tbody>
        </table>
    </div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
