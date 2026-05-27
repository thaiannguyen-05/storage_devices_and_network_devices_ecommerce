<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Voucher Management" scope="request" />
<c:set var="activePage" value="admin-vouchers" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Mã giảm giá</h2>
        <a class="button" href="${pageContext.request.contextPath}/admin/vouchers?action=create">Tạo mã giảm giá</a>
    </div>
    <div class="table-wrap"><table><thead><tr><th>Mã</th><th>Giảm giá</th><th>Người dùng</th><th>Hết hạn</th><th>Số lượng</th><th>Ngày tạo</th><th></th></tr></thead><tbody><tr><td>VOU-001</td><td>10%</td><td>111...</td><td>31/12/2026</td><td>20</td><td>18/05/2026</td><td><a class="button secondary" href="${pageContext.request.contextPath}/admin/vouchers?action=edit">Sửa</a></td></tr></tbody></table></div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
