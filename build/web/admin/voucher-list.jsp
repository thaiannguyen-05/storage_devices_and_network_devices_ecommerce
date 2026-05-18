<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quản lý voucher" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<div class="toolbar"><h1 class="page-title">Voucher</h1><a class="button" href="${pageContext.request.contextPath}/admin/vouchers?action=create">Thêm voucher</a></div>
<section class="table-wrap"><table><thead><tr><th>ID</th><th>Percent</th><th>UserId</th><th>ExpTìme</th><th>Quantity</th><th>CreatedAt</th><th></th></tr></thead><tbody><tr><td data-label="ID">VOU-001</td><td data-label="%">10%</td><td data-label="User">111...</td><td data-label="Hạn">31/12/2026</td><td data-label="SL">20</td><td data-label="Ngày tạo">18/05/2026</td><td data-label="Hành động"><a class="button secondary" href="#">Sửa</a></td></tr></tbody></table></section>
<jsp:include page="../layouts/footer.jsp" />

