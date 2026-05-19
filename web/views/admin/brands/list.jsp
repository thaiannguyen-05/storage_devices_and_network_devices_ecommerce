<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Brand Management" scope="request" />
<c:set var="activePage" value="admin-brands" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Brands</h2>
        <a class="button" href="${pageContext.request.contextPath}/admin/brands?action=create">Create brand</a>
    </div>
    <div class="table-wrap"><table><thead><tr><th>ID</th><th>Name</th><th>UserId</th><th>Description</th><th>Status</th><th></th></tr></thead><tbody><tr><td>b111...</td><td>Samsung</td><td>111...</td><td>Consumer SSD solutions</td><td><span class="badge success">ACTIVE</span></td><td><a class="button secondary" href="${pageContext.request.contextPath}/admin/brands?action=edit">Edit</a></td></tr></tbody></table></div>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
