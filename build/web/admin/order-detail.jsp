<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Chi tiết đơn hàng" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Chi tiết đơn hàng</h1>
<section class="two-column">
    <div class="panel"><h2>Thông tin đơn</h2><p>Mã đơn: <c:out value="${param.id}" /></p><p>Status: PENDING</p><p>Payment: PENDING</p></div>
    <form class="panel" action="${pageContext.request.contextPath}/admin/orders" method="post"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="update-status"><input type="hidden" name="id" value="${param.id}"><label>Cập nhật status</label><select name="status"><option>PENDING</option><option>CONFIRMED</option><option>SHIPPING</option><option>COMPLETED</option><option>CANCELLED</option></select><button class="button mt-4">Lưu</button></form>
</section>
<jsp:include page="../layouts/footer.jsp" />

