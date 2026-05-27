<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Đăng ký" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<section class="auth-wrap">
    <form class="auth-card form-grid" action="${pageContext.request.contextPath}/auth" method="post" data-validate>
        <h1 class="page-title field full">Đăng ký tài khoản</h1>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="register">
        <c:if test="${not empty error}"><p class="badge danger field full"><c:out value="${error}" /></p></c:if>
        <div class="field"><label>Ngày sinh</label><input type="date" name="dateOfBirth" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" required><span class="error"></span></div>
        <div class="field full"><label>Địa chỉ</label><input name="address" required><span class="error"></span></div>
        <div class="field"><label>Mật khẩu</label><input data-password type="password" name="password" minlength="8" autocomplete="new-password" required><span class="error"></span></div>
        <div class="field"><label>Nhập lại mật khẩu</label><input data-password-confirm type="password" name="confirmPassword" minlength="8" autocomplete="new-password" required><span class="error"></span></div>
        <button class="button" type="submit">Đăng ký</button>
        <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a></p>
    </form>
</section>
<jsp:include page="../layouts/footer.jsp" />

