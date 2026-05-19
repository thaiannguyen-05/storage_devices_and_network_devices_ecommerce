<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Ho so ca nhan" scope="request" />
<c:set var="hideFilters" value="true" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<h1 class="page-title">Ho so ca nhan</h1>
<c:if test="${not empty success}"><p class="badge success mb-4"><c:out value="${success}" /></p></c:if>
<c:if test="${not empty error}"><p class="badge danger mb-4"><c:out value="${error}" /></p></c:if>

<section class="profile-layout">
    <form class="panel form-grid profile-main-form" action="${pageContext.request.contextPath}/profile" method="post" data-validate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="update-profile">
        <h2 class="profile-block-title field full">Thong tin tai khoan</h2>
        <div class="field"><label>Ten tai khoan</label><input name="username" value="${sessionScope.currentUser.name}" readonly></div>
        <div class="field"><label>Ten hien thi</label><input name="displayName" value="${profileUser.name}" required><span class="error"></span></div>
        <div class="field"><label>Email</label><input type="email" name="email" value="${profileUser.email}" required></div>
        <div class="field"><label>Ngay sinh</label><input type="date" name="dateOfBirth" value="${profileUser.dateOfBirth}"><span class="error"></span></div>
        <div class="field full"><label>Vai tro</label><input value="${profileUser.role}" readonly></div>
        <button class="button field full" type="submit">Cap nhat</button>
    </form>

    <aside class="profile-column">
        <form class="panel form-grid profile-password-form" action="${pageContext.request.contextPath}/profile" method="post" data-validate>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="change-password">
            <h2 class="profile-block-title field full">Doi mat khau</h2>
            <div class="field full"><label>Mat khau hien tai</label><input type="password" name="currentPassword" required><span class="error"></span></div>
            <div class="field"><label>Mat khau moi</label><input data-password type="password" name="newPassword" required minlength="8"><span class="error"></span></div>
            <div class="field"><label>Nhap lai</label><input data-password-confirm type="password" name="confirmPassword" required minlength="8"><span class="error"></span></div>
            <button class="button field full" type="submit">Doi mat khau</button>
        </form>

        <div class="panel profile-side">
            <h2 class="profile-block-title profile-session-title">Session</h2>
            <p class="muted">Email: <c:out value="${profileUser.email}" /> - Role: <c:out value="${profileUser.role}" /></p>
        </div>
    </aside>
</section>

<jsp:include page="../layouts/footer.jsp" />
