<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="Giới thiệu LinhNamStore" scope="request" />
<c:set var="activePage" value="about" scope="request" />
<jsp:include page="../layouts/header.jsp" />
<h1 class="page-title">Giới thiệu LinhNamStore</h1>
<section class="two-column">
    <div class="panel">
        <h2>Dự án e-commerce thiết bị lưu trữ và network</h2>
        <p>LinhNamStore là ứng dụng Jakarta EE JSP/Servlet dùng MySQL, tập trung vào catalog sản phẩm, giỏ hàng, thanh toán và quản trị.</p>
        <table>
            <thead><tr><th>Họ tên</th><th>MSSV</th><th>Ngày sinh</th><th>Vai trò</th></tr></thead>
            <tbody>
                <tr><td>Thành viên 1</td><td>SV001</td><td>01/01/2004</td><td>Frontend</td></tr>
                <tr><td>Thành viên 2</td><td>SV002</td><td>02/02/2004</td><td>Backend</td></tr>
                <tr><td>Thành viên 3</td><td>SV003</td><td>03/03/2004</td><td>Database</td></tr>
            </tbody>
        </table>
    </div>
    <div class="product-media"><img src="https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1200&q=80" alt="LinhNamStore mockup"></div>
</section>
<jsp:include page="../layouts/footer.jsp" />

