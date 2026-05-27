<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Liên Hệ LinhNamStore" scope="request" />
<c:set var="activePage" value="contact" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Liên Hệ</h1>
<section class="two-column">
    <form class="panel form-grid" action="${pageContext.request.contextPath}/contact" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <div class="field"><label>Họ tên</label><input name="name" value="${submittedName}" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" value="${submittedEmail}" required><span class="error"></span></div>
        <div class="field full"><label>Nội dung</label><textarea name="message" rows="6" required>${submittedMessage}</textarea><span class="error"></span></div>
        <button class="button" type="submit">Gửi liên hệ</button>
        <c:if test="${not empty success}"><span class="badge success"><c:out value="${success}" /></span></c:if>
        <c:if test="${not empty error}"><span class="badge danger"><c:out value="${error}" /></span></c:if>
    </form>
    <aside class="panel">
        <h2>Thông tin hỗ trợ</h2>
        <p>Email: thaianthedev@gmail.com</p>
        <p>Phone: 0900 000 000</p>
        <p>Địa chỉ: Hà Nội</p>
    </aside>
</section>
<jsp:include page="../layouts/footer.jsp" />
