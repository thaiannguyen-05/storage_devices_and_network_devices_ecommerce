<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Hồ sơ cá nhân" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<h1 class="page-title">Hồ sơ cá nhân</h1>
<c:if test="${not empty success}"><p class="badge success mb-4"><c:out value="${success}" /></p></c:if>
<c:if test="${not empty error}"><p class="badge danger mb-4"><c:out value="${error}" /></p></c:if>

<section class="profile-layout">
    <form class="panel form-grid profile-main-form" action="${pageContext.request.contextPath}/profile" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="update-profile">
        <h2 class="profile-block-title field full">Thông tin tài khoản</h2>
        <div class="field"><label>Tên tài khoản</label><input name="username" value="${sessionScope.currentUser.name}" readonly></div>
        <div class="field"><label>Tên hiển thị</label><input name="displayName" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" value="${sessionScope.currentUser.email}" readonly></div>
        <div class="field"><label>Số điện thoại</label><input name="phone" data-phone="true"><span class="error"></span></div>
        <div class="field full"><label>Địa chỉ</label><input name="address"></div>
        <button class="button field full" type="submit">Cập nhật</button>
    </form>

    <aside class="profile-column">
        <form class="panel form-grid profile-password-form" action="${pageContext.request.contextPath}/profile" method="post" data-validate>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="change-password">
            <h2 class="profile-block-title field full">Đổi mật khẩu</h2>
            <div class="field full"><label>Mật khẩu hiện tại</label><input type="password" name="currentPassword" required><span class="error"></span></div>
            <div class="field"><label>Mật khẩu mới</label><input data-password type="password" name="newPassword" required minlength="8"><span class="error"></span></div>
            <div class="field"><label>Nhập lại</label><input data-password-confirm type="password" name="confirmPassword" required minlength="8"><span class="error"></span></div>
            <button class="button field full" type="submit">Đổi mật khẩu</button>
        </form>

        <div class="panel profile-side">
            <h2 class="profile-block-title profile-session-title">Session</h2>
            <p class="muted">IP: 127.0.0.1 - CreatedAt: 18/05/2026</p>
            <button class="button secondary field full" type="button">Đăng xuất thiết bị khác</button>
        </div>
    </aside>
</section>

<jsp:include page="../layouts/footer.jsp" />
