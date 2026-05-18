<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Đặt lại mật khẩu" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<section class="auth-wrap">
    <form class="auth-card form-grid" action="${pageContext.request.contextPath}/auth" method="post" data-validate>
        <h1 class="page-title field full">Đặt lại mật khẩu</h1>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="reset-password">
        <c:if test="${param.sent == '1'}"><p class="badge success field full">Mã xác thực đã được gửi về email.</p></c:if>
        <c:if test="${not empty error}"><p class="badge danger field full"><c:out value="${error}" /></p></c:if>
        <div class="field full">
            <label>Email</label>
            <input type="email" name="email" value="${email}" required>
            <span class="error"></span>
        </div>
        <div class="field full">
            <label>Mã xác thực</label>
            <input name="code" maxlength="8" required>
            <span class="error"></span>
        </div>
        <div class="field">
            <label>Mật khẩu mới</label>
            <input data-password type="password" name="newPassword" minlength="8" required>
            <span class="error"></span>
        </div>
        <div class="field">
            <label>Nhập lại mật khẩu mới</label>
            <input data-password-confirm type="password" name="confirmPassword" minlength="8" required>
            <span class="error"></span>
        </div>
        <button class="button field full" type="submit">Đổi mật khẩu</button>
    </form>
</section>
<jsp:include page="../layouts/footer.jsp" />

