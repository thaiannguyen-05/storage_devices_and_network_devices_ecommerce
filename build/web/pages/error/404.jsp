<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="404" scope="request" />
<jsp:include page="../../layouts/header.jsp" />
<section class="panel empty-state">
    <h1>Trang không tìm thấy</h1>
    <p class="muted">URL không tồn tại hoặc đã bị thay đổi.</p>
    <a class="button" href="${pageContext.request.contextPath}/home">Quay lại trang chủ</a>
</section>
<jsp:include page="../../layouts/footer.jsp" />

