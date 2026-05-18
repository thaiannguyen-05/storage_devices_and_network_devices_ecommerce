<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Giỏ hàng" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<h1 class="page-title">Giỏ hàng</h1>

<c:choose>
    <c:when test="${not empty cartResult.items}">
        <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="get">
        </form>

        <section class="table-wrap" data-cart>
            <table>
                <thead>
                    <tr>
                        <th style="width: 50px; text-align: center;"><input type="checkbox" data-select-all checked></th>
                        <th>Sản phẩm</th>
                        <th>Variant</th>
                        <th>Đơn giá</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${cartResult.items}">
                        <tr data-cart-row data-price="${item.price != null ? item.price : 0}">
                            <td data-label="Chọn" style="text-align: center;"><input type="checkbox" name="selectedItems" value="${item.id}" form="checkoutForm" data-item-select checked></td>
                            <td data-label="Sản phẩm"><c:out value="${item.productName}" /></td>
                            <td data-label="Variant"><c:out value="${item.sku}" /></td>
                            <td data-label="Đơn giá"><c:out value="${item.price != null ? item.price : 0}" /> VND</td>
                            <td data-label="Số lượng">
                                <form action="${pageContext.request.contextPath}/cart" method="post" style="display:inline">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="itemId" value="${item.id}">
                                    <div class="quantity-control">
                                        <button type="button" class="qty-btn" data-qty-change="-1" aria-label="Giảm số lượng">−</button>
                                        <input data-cart-quantity type="number" name="quantity" min="1" max="${item.stockQuantity}" value="${item.quantity}" readonly>
                                        <button type="button" class="qty-btn" data-qty-change="1" aria-label="Tăng số lượng">+</button>
                                    </div>
                                </form>
                            </td>
                            <td data-label="Thành tiền" data-line-total></td>
                            <td data-label="Xóa">
                                <form action="${pageContext.request.contextPath}/cart" method="post">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="itemId" value="${item.id}">
                                    <button class="button danger" type="submit">Xóa</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
                <tfoot>
                    <tr><th colspan="5" class="text-right">Tổng tiền (trang này)</th><th data-cart-total></th><th></th></tr>
                </tfoot>
            </table>
        </section>

        <c:if test="${cartResult.totalPages > 1}">
            <nav class="cart-pagination" aria-label="Phân trang giỏ hàng">
                <c:if test="${cartResult.page > 1}">
                    <a class="page-btn" href="${pageContext.request.contextPath}/cart?page=${cartResult.page - 1}">‹ Trước</a>
                </c:if>
                <c:forEach begin="1" end="${cartResult.totalPages}" var="p">
                    <a class="page-btn ${p == cartResult.page ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/cart?page=${p}"><c:out value="${p}" /></a>
                </c:forEach>
                <c:if test="${cartResult.page < cartResult.totalPages}">
                    <a class="page-btn" href="${pageContext.request.contextPath}/cart?page=${cartResult.page + 1}">Sau ›</a>
                </c:if>
            </nav>
        </c:if>

        <p class="muted">Tổng ${cartResult.totalItems} sản phẩm · Trang ${cartResult.page}/${cartResult.totalPages}</p>

        <div class="toolbar mt-4">
            <a class="button secondary" href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
            <button type="submit" form="checkoutForm" class="button">Thanh toán</button>
        </div>
    </c:when>
    <c:otherwise>
        <div class="empty-state panel">
            <p class="page-title" style="font-size:1.4rem">Giỏ hàng trống</p>
            <a class="button mt-4" href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="../layouts/footer.jsp" />
