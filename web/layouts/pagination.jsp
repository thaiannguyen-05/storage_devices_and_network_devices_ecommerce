<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${pg_totalPages > 1}">
<div class="pg-wrapper">
    <span class="pg-info">Trang ${pg_currentPage} / ${pg_totalPages}</span>
    <div class="pg-nav">
        <%-- Previous --%>
        <c:choose>
            <c:when test="${pg_currentPage <= 1}">
                <span class="pg disabled">&lt;</span>
            </c:when>
            <c:otherwise>
                <a class="pg" href="?page=${pg_currentPage - 1}#products">&lt;</a>
            </c:otherwise>
        </c:choose>

        <%-- Page numbers --%>
        <c:forEach begin="${pg_currentPage - 2 < 1 ? 1 : pg_currentPage - 2}" end="${pg_currentPage + 2 > pg_totalPages ? pg_totalPages : pg_currentPage + 2}" var="p">
            <c:choose>
                <c:when test="${p == pg_currentPage}">
                    <span class="pg active">${p}</span>
                </c:when>
                <c:otherwise>
                    <a class="pg" href="?page=${p}#products">${p}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <%-- Ellipsis + Last --%>
        <c:if test="${pg_currentPage < pg_totalPages - 2}">
            <c:if test="${pg_currentPage < pg_totalPages - 3}">
                <span class="pg dots">...</span>
            </c:if>
            <a class="pg" href="?page=${pg_totalPages}#products">${pg_totalPages}</a>
        </c:if>

        <%-- Next --%>
        <c:choose>
            <c:when test="${pg_currentPage >= pg_totalPages}">
                <span class="pg disabled">&gt;</span>
            </c:when>
            <c:otherwise>
                <a class="pg" href="?page=${pg_currentPage + 1}#products">&gt;</a>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</c:if>
