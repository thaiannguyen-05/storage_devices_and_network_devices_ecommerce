<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Giỏ hàng" scope="request" />
<jsp:include page="../../layouts/header.jsp" />
<h1 class="page-title">Giỏ hàng</h1>
<section class="table-wrap" data-cart>
    <table>
        <thead><tr><th>Sản phẩm</th><th>Variant</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th></th></tr></thead>
        <tbody>
            <tr data-cart-row data-price="3490000">
                <td>Samsung 990 PRO NVMe SSD</td>
                <td>SAM-990PRO-1TB</td>
                <td>3.490.000 VND</td>
                <td><input data-cart-quantity type="number" min="1" value="1"></td>
                <td data-line-total></td>
                <td><form action="${pageContext.request.contextPath}/cart" method="post"><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="remove"><button class="button danger" type="submit">Xóa</button></form></td>
            </tr>
            <tr data-cart-row data-price="2890000">
                <td>TP-Link Archer AX73 Wi-Fi 6 Router</td>
                <td>TPL-AX73-AX5400</td>
                <td>2.890.000 VND</td>
                <td><input data-cart-quantity type="number" min="1" value="2"></td>
                <td data-line-total></td>
                <td><button class="button danger" type="button">Xóa</button></td>
            </tr>
        </tbody>
        <tfoot>
            <tr><th colspan="4" class="text-right">Tổng tiền</th><th data-cart-total></th><th></th></tr>
        </tfoot>
    </table>
</section>
<div class="toolbar mt-4">
    <a class="button secondary" href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
    <a class="button" href="${pageContext.request.contextPath}/checkout">Thanh toán</a>
</div>
<jsp:include page="../../layouts/footer.jsp" />

