<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Voucher Management" scope="request" />
<c:set var="activePage" value="admin-vouchers" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Vouchers</h2>
        <a class="button" href="${pageContext.request.contextPath}/admin/vouchers?action=create">Create voucher</a>
    </div>
    <div class="table-wrap"><table><thead><tr><th>ID</th><th>Percent</th><th>UserId</th><th>Expire</th><th>Quantity</th><th>Created</th><th></th></tr></thead><tbody><tr><td>VOU-001</td><td>10%</td><td>111...</td><td>31/12/2026</td><td>20</td><td>18/05/2026</td><td><a class="button secondary" href="${pageContext.request.contextPath}/admin/vouchers?action=edit">Edit</a></td></tr></tbody></table></div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
