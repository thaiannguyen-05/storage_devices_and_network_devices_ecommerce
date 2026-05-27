<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Edit Voucher" scope="request" />
<c:set var="activePage" value="admin-vouchers" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel admin-panel--narrow">
    <h2>Sửa mã giảm giá</h2>
    <form class="admin-form" action="${pageContext.request.contextPath}/admin/vouchers" method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <label>Phần trăm</label><input type="number" name="percent" min="1" max="100" required>
        <label>Người dùng</label><input name="userId" required>
        <label>Ngày hết hạn</label><input type="date" name="expTime" required>
        <label>Số lượng</label><input type="number" name="quantity" min="1" required>
        <button class="button" type="submit">Lưu</button>
    </form>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
