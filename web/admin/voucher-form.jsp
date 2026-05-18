<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Form voucher" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Thêm / sửa voucher</h1>
<form class="panel form-grid" action="${pageContext.request.contextPath}/admin/vouchers" method="post" data-validate>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <div class="field"><label>Percent</label><input type="number" name="percent" min="1" max="100" required><span class="error"></span></div>
    <div class="field"><label>UserId</label><input name="userId" required><span class="error"></span></div>
    <div class="field"><label>ExpTìme</label><input type="date" name="expTìme" required><span class="error"></span></div>
    <div class="field"><label>Quantity</label><input type="number" name="quantity" min="1" required><span class="error"></span></div>
    <button class="button" type="submit">Lưu</button>
</form>
<jsp:include page="../layouts/footer.jsp" />

