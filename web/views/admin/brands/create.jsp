<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Create Brand" scope="request" />
<c:set var="activePage" value="admin-brands" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel admin-panel--narrow">
    <h2>Tạo thương hiệu</h2>
    <form class="admin-form" action="${pageContext.request.contextPath}/admin/brands" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <label>Tên</label><input name="name" required>
        <label>Trạng thái</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option></select>
        <label>Mô tả</label><textarea name="description" required></textarea>
        <button class="button" type="submit">Lưu</button>
    </form>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
