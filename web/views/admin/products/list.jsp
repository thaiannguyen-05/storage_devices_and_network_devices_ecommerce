<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Product Management" scope="request" />
<c:set var="activePage" value="admin-products" scope="request" />
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="start" />
</jsp:include>
<section class="admin-panel">
    <div class="admin-panel-head">
        <h2>Products</h2>
        <div style="display:flex;gap:8px;align-items:center;">
            <c:if test="${not empty productsResult}">
                <span class="muted"><c:out value="${productsResult.total}" /> san pham</span>
            </c:if>
            <a class="button" href="${pageContext.request.contextPath}/admin/products?action=create">Create product</a>
        </div>
    </div>
    <div class="table-wrap">
        <c:choose>
            <c:when test="${not empty productsResult && not empty productsResult.products}">
                <table>
                    <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Brand</th><th>Status</th><th>Actions</th></tr></thead>
                    <tbody>
                        <c:forEach var="p" items="${productsResult.products}">
                            <tr>
                                <td><c:out value="${p.id.substring(0, 8)}" />...</td>
                                <td><c:out value="${p.name}" /></td>
                                <td><c:out value="${p.category}" /></td>
                                <td>${p.brandId != null ? p.brandId.substring(0, 8) : ''}...</td>
                                <td><span class="badge ${p.status == 'ACTIVE' ? 'success' : ''}"><c:out value="${p.status}" /></span></td>
                                <td class="admin-actions">
                                    <a class="button secondary" href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.id}">Edit</a>
                                    <form action="${pageContext.request.contextPath}/admin/products" method="post" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <button class="button danger" type="submit" onclick="return confirm('Delete this product?')">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p class="muted">No products found.</p>
            </c:otherwise>
        </c:choose>
    </div>
    <c:if test="${not empty productsResult}">
        <c:set var="pg_totalPages" value="${(productsResult.total + 9) / 10}" scope="request" />
        <c:set var="pg_currentPage" value="${param.page != null ? param.page : 1}" scope="request" />
        <jsp:include page="/layouts/pagination.jsp" />
    </c:if>
</section>
<jsp:include page="/layouts/admin-layout.jsp">
    <jsp:param name="part" value="end" />
</jsp:include>
