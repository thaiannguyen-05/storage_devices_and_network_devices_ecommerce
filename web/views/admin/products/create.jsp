<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Create Product" scope="request" />
<c:set var="activePage" value="admin-products" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <h2>Tạo sản phẩm</h2>
    <form class="admin-form" action="${pageContext.request.contextPath}/admin/products" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="create">
        <label>Tên</label><input name="name" required>
        <label>Thương hiệu</label><select name="brandId"><option>Samsung</option><option>Western Digital</option><option>Synology</option><option>TP-Link</option><option>SanDisk</option></select>
        <label>Danh mục</label><select name="category"><option>HDD</option><option>SSD</option><option>USB</option><option>NAS</option><option>TAPE</option><option>ENCLOSURE</option><option>MEMORY_CARD</option></select>
        <label>Trạng thái</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option></select>
        <label>Mô tả</label><textarea name="description" required></textarea>
        <label>SKU</label><input name="sku">
        <label>Giá</label><input type="number" name="price">
        <label>Số lượng</label><input type="number" name="quantity">
        <label>URL hình ảnh</label><input name="imageUrl">
        <button class="button" type="submit">Lưu</button>
    </form>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
