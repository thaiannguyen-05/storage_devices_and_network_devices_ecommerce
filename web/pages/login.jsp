<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Đăng nhập" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<section class="auth-wrap">
    <form class="auth-card" action="${pageContext.request.contextPath}/auth" method="post" data-validate>
        <h1 class="page-title">Đăng nhập</h1>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="login">
        <c:if test="${not empty param.redirect}"><input type="hidden" name="redirect" value="${param.redirect}"></c:if>
        <c:if test="${param.reset == '1'}"><p class="badge success">Đổi mật khẩu thành công. Vui lòng đăng nhập lại.</p></c:if>
        <c:if test="${not empty error}"><p class="badge danger"><c:out value="${error}" /></p></c:if>
        <div class="field"><label>Email</label><input type="email" name="email" autocomplete="email" required><span class="error"></span></div>
        <div class="field mt-4"><label>Mật khẩu</label><input type="password" name="password" autocomplete="current-password" required><span class="error"></span></div>
        <p><label><input type="checkbox" name="remember"> Ghi nhớ đăng nhập</label></p>
        <button class="button" type="submit">Đăng nhập</button>
        <p><a href="${pageContext.request.contextPath}/auth?action=forgot-password">Quên mật khẩu?</a> - <a href="${pageContext.request.contextPath}/auth?action=register">Đăng ký</a></p>
    </form>
</section>
<jsp:include page="../layouts/footer.jsp" />

