<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Quản lý đơn hàng" scope="request" />
<c:set var="activePage" value="admin" scope="request" />
<jsp:include page="../../../layouts/header.jsp" />
<div class="toolbar"><h1 class="page-title">Đơn hàng</h1><select><option>Tất cả</option><option>PENDING</option><option>CONFIRMED</option><option>SHIPPING</option><option>COMPLETED</option><option>CANCELLED</option></select></div>
<section class="table-wrap"><table><thead><tr><th>ID</th><th>UserId</th><th>ProductId</th><th>Status</th><th>CreatedAt</th><th></th></tr></thead><tbody><tr><td>ORD-0001</td><td>111...</td><td>p111...</td><td><span class="badge warning">PENDING</span></td><td>18/05/2026</td><td><a class="button secondary" href="${pageContext.request.contextPath}/admin/orders?action=detail&id=ORD-0001">Chi tiết</a></td></tr></tbody></table></section>
<jsp:include page="../../../layouts/footer.jsp" />

