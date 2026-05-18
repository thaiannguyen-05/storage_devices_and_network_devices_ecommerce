<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Form brand" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Thêm / sửa brand</h1>
<form class="panel form-grid" action="${pageContext.request.contextPath}/admin/brands" method="post" data-validate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <div class="field"><label>Tên</label><input name="name" required><span class="error"></span></div>
    <div class="field"><label>Status</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option></select></div>
    <div class="field full"><label>Description</label><textarea name="description" required></textarea><span class="error"></span></div>
    <button class="button" type="submit">Lưu</button>
</form>
<jsp:include page="../layouts/footer.jsp" />

