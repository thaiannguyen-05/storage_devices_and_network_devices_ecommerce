<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quên mật khẩu" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<section class="auth-wrap">
    <form class="auth-card form-grid" action="${pageContext.request.contextPath}/auth" method="post" data-validate>
        <h1 class="page-title field full">Quên mật khẩu</h1>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="forgot-password">
        <c:if test="${not empty error}"><p class="badge danger field full"><c:out value="${error}" /></p></c:if>
        <div class="field full"><label>Email</label><input type="email" name="email" required><span class="error"></span></div>
        <button class="button field full" type="submit">Gửi mã xác thực</button>
    </form>
</section>
<jsp:include page="../layouts/footer.jsp" />

