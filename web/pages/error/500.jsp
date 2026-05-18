<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="500" scope="request" />
<jsp:include page="../../layouts/header.jsp" />
<section class="panel empty-state">
    <h1>Đã xảy ra lỗi hệ thống</h1>
    <p class="muted">Vui lòng thử lại sau. Trang này không hiển thị stack trace.</p>
    <button class="button" type="button" onclick="window.location.reload()">Thử lại</button>
</section>
<jsp:include page="../../layouts/footer.jsp" />

