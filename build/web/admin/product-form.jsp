<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Form sản phẩm" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Thêm / sửa sản phẩm</h1>
<form class="panel form-grid" action="${pageContext.request.contextPath}/admin/products" method="post" data-validate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="${empty param.id ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${param.id}">
    <div class="field"><label>Tên sản phẩm</label><input name="name" required><span class="error"></span></div>
    <div class="field"><label>Brand</label><select name="brandId">
        <c:forEach var="brand" items="${brandsResult.brands}"><option value="${brand.id}"><c:out value="${brand.name}" /></option></c:forEach>
    </select></div>
    <div class="field"><label>Category</label><select name="category"><option>STORAGE_DEVICE</option><option>NETWORK_DEVICE</option><option>ACCESSORY</option></select></div>
    <div class="field"><label>Status</label><select name="status"><option>ACTIVE</option><option>DRAFT</option><option>INACTIVE</option><option>ARCHIVED</option></select></div>
    <div class="field full"><label>Description</label><textarea name="description" required></textarea><span class="error"></span></div>
    <h2 class="field full">Variants</h2>
    <div class="field"><label>SKU</label><input name="sku"></div>
    <div class="field"><label>Giá</label><input type="number" name="price"></div>
    <div class="field"><label>Quantity</label><input type="number" name="quantity"></div>
    <div class="field"><label>Image URL</label><input name="imageUrl"></div>
    <button class="button" type="submit">Lưu</button>
</form>
<jsp:include page="../layouts/footer.jsp" />

