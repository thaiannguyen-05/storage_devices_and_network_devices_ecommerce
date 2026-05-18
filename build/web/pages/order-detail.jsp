<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Chi tiết đơn hàng" scope="request" />
<c:set var="activePage" value="orders" scope="request" />
<jsp:include page="../layouts/header.jsp" />

<div class="toolbar">
    <h1 class="page-title">Chi tiết đơn hàng</h1>
    <a href="${pageContext.request.contextPath}/orders?action=history" class="button secondary">
        &larr; Lịch sử đơn hàng
    </a>
</div>

<c:choose>
    <c:when test="${orderResult.success && not empty orderResult.order}">
        <c:set var="order" value="${orderResult.order}" />
        <div class="two-column">
            <div class="panel">
                <h2 class="profile-block-title">Thông tin giao nhận</h2>
                <div class="summary-list" style="margin-bottom: 24px;">
                    <li style="padding: 10px 0; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between;">
                        <strong>Họ và tên:</strong> <span><c:out value="${order.customerName}" /></span>
                    </li>
                    <li style="padding: 10px 0; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between;">
                        <strong>Số điện thoại:</strong> <span><c:out value="${order.phone}" /></span>
                    </li>
                    <li style="padding: 10px 0; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between;">
                        <strong>Email:</strong> <span><c:out value="${order.email}" /></span>
                    </li>
                    <li style="padding: 10px 0; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between;">
                        <strong>Địa chỉ nhận hàng:</strong> <span><c:out value="${order.address}" /></span>
                    </li>
                    <c:if test="${not empty order.note}">
                        <li style="padding: 10px 0; display: flex; justify-content: space-between;">
                            <strong>Ghi chú:</strong> <span><c:out value="${order.note}" /></span>
                        </li>
                    </c:if>
                </div>

                <h2 class="profile-block-title" style="margin-top: 32px;">Sản phẩm đặt mua</h2>
                <table style="width: 100%; border-collapse: collapse; margin-top: 14px;">
                    <thead>
                        <tr>
                            <th style="padding: 10px; background: var(--color-gray-100);">Tên sản phẩm</th>
                            <th style="padding: 10px; background: var(--color-gray-100); text-align: center;">Số lượng</th>
                            <th style="padding: 10px; background: var(--color-gray-100); text-align: right;">Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding: 12px 10px; border-bottom: 1px solid var(--color-border);">
                                <c:out value="${order.productName}" />
                            </td>
                            <td style="padding: 12px 10px; border-bottom: 1px solid var(--color-border); text-align: center;">
                                x${order.quantity}
                            </td>
                            <td style="padding: 12px 10px; border-bottom: 1px solid var(--color-border); text-align: right; font-weight: 600;">
                                <c:out value="${order.totalAmount}" /> VND
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="panel">
                <h2 class="profile-block-title">Trạng thái đơn hàng</h2>
                <div class="summary-list" style="margin-bottom: 24px;">
                    <li style="padding: 8px 0; display: flex; justify-content: space-between;">
                        <strong>Mã đơn:</strong> <span style="font-family: monospace; font-weight: 600;"><c:out value="${order.id}" /></span>
                    </li>
                    <li style="padding: 8px 0; display: flex; justify-content: space-between;">
                        <strong>Ngày tạo:</strong> <span><c:out value="${order.createdAt}" /></span>
                    </li>
                    <li style="padding: 8px 0; display: flex; justify-content: space-between; align-items: center;">
                        <strong>Trạng thái:</strong>
                        <c:choose>
                            <c:when test="${order.status == 'PENDING'}">
                                <span class="badge warning">PENDING</span>
                            </c:when>
                            <c:when test="${order.status == 'PAID'}">
                                <span class="badge success" style="background: #10b981; color: #fff;">PAID</span>
                            </c:when>
                            <c:when test="${order.status == 'COMPLETED'}">
                                <span class="badge success">COMPLETED</span>
                            </c:when>
                            <c:when test="${order.status == 'CANCELLED'}">
                                <span class="badge danger">CANCELLED</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge secondary">${order.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <li style="padding: 8px 0; display: flex; justify-content: space-between;">
                        <strong>Thanh toán:</strong> <span class="badge secondary" style="text-transform: uppercase;"><c:out value="${order.paymentMethod}" /></span>
                    </li>
                    <c:if test="${not empty order.voucherId}">
                        <li style="padding: 8px 0; display: flex; justify-content: space-between;">
                            <strong>Mã Voucher:</strong> <span class="badge warning"><c:out value="${order.voucherId}" /></span>
                        </li>
                    </c:if>
                    <li style="padding: 16px 0 0 0; border-top: 1px dashed var(--color-gray-300); display: flex; justify-content: space-between; align-items: baseline;">
                        <strong style="font-size: 1.15rem;">Tổng cộng:</strong>
                        <strong style="font-size: 1.4rem; color: var(--color-primary);"><c:out value="${order.totalAmount}" /> VND</strong>
                    </li>
                </div>

                <div style="display: grid; gap: 10px; margin-top: 24px;">
                    <c:if test="${order.status == 'PENDING' && order.paymentMethod == 'SEPAY'}">
                        <a href="${pageContext.request.contextPath}/checkout?action=pay&orderIds=${order.id}" class="button" style="text-align: center; justify-content: center;">
                            Thanh toán ngay qua cổng Sepay
                        </a>
                    </c:if>
                    <c:if test="${order.status == 'PENDING'}">
                        <form action="${pageContext.request.contextPath}/orders" method="post" style="margin: 0; width: 100%;">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="id" value="${order.id}">
                            <button type="submit" class="button danger" style="width: 100%; justify-content: center;" onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?');">
                                Hủy đơn hàng
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="panel empty-state">
            <p class="muted">Không tìm thấy thông tin đơn hàng này hoặc bạn không có quyền xem.</p>
            <a href="${pageContext.request.contextPath}/orders?action=history" class="button">Quay lại lịch sử</a>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="../layouts/footer.jsp" />
