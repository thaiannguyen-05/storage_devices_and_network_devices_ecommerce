<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Xác thực email" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<section class="auth-wrap">
    <form class="auth-card" action="${pageContext.request.contextPath}/auth" method="post" data-validate>
        <h1 class="page-title">Xác thực email</h1>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="verify-email">
        <input type="hidden" name="userId" value="${param.userId}">
        <c:if test="${not empty error}"><p class="badge danger"><c:out value="${error}" /></p></c:if>
        <div class="field"><label>Mã xác thực</label><input name="code" maxlength="36" value="${param.code}" required><span class="error"></span></div>
        <button class="button mt-4" type="submit">Xác thực</button>
    </form>
</section>
<jsp:include page="../layouts/footer.jsp" />

