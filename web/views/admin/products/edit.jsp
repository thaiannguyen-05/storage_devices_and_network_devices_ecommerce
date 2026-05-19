<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Edit Product" scope="request" />
<c:set var="activePage" value="admin-products" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <h2>Edit product</h2>
    <form class="admin-form" action="${pageContext.request.contextPath}/admin/products" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="edit">
        <input type="hidden" name="id" value="${param.id}">
        <label>Name</label><input name="name" required>
        <label>Brand</label><select name="brandId"><option>Samsung</option><option>Western Digital</option><option>Synology</option><option>TP-Link</option><option>SanDisk</option></select>
        <label>Category</label><select name="category"><option>STORAGE_DEVICE</option><option>NETWORK_DEVICE</option><option>ACCESSORY</option></select>
        <label>Status</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option></select>
        <label>Description</label><textarea name="description" required></textarea>
        <button class="button" type="submit">Save</button>
    </form>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
