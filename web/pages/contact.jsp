<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Liên hệ LinhNamStore" scope="request" />
<c:set var="activePage" value="contact" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Liên hệ</h1>
<section class="two-column">
    <form class="panel form-grid" action="${pageContext.request.contextPath}/contact" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <div class="field"><label>Họ tên</label><input name="name" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" required><span class="error"></span></div>
        <div class="field full"><label>Nội dung</label><textarea name="message" rows="6" required></textarea><span class="error"></span></div>
        <button class="button" type="submit">Gửi liên hệ</button>
        <c:if test="${not empty success}"><span class="badge success"><c:out value="${success}" /></span></c:if>
    </form>
    <aside class="panel">
        <h2>Thông tin hỗ trợ</h2>
        <p>Email: support@linhnamstore.local</p>
        <p>Phone: 0900 000 000</p>
        <p>Địa chỉ: Quận 1, TP. Hồ Chí Minh</p>
    </aside>
</section>
<jsp:include page="../layouts/footer.jsp" />

