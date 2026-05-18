<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
    </main>
</div>
<footer class="site-footer">
    <div class="footer-grid">
        <section>
            <h3>LinhNamStore</h3>
            <p>Thiết bị lưu trữ, NAS và network cho học tập, làm việc và vận hành cửa hàng nhỏ.</p>
        </section>
        <section>
            <h3>Link nhanh</h3>
            <ul>
                <li><a href="${ctx}/home">Trang chủ</a></li>
                <li><a href="${ctx}/contact">Liên hệ</a></li>
                <li><a href="${ctx}/about">Giới thiệu</a></li>
                <li><a href="${ctx}/home#policy">Chính sách</a></li>
            </ul>
        </section>
        <section>
            <h3>Danh mục</h3>
            <ul>
                <li><a href="${ctx}/home#products">Storage device</a></li>
                <li><a href="${ctx}/home#products">Network device</a></li>
                <li><a href="${ctx}/home#products">Accessory</a></li>
                <li><a href="${ctx}/cart">Giỏ hàng</a></li>
            </ul>
        </section>
        <section>
            <h3>Nhóm thực hiện</h3>
            <ul>
                <li>Thành viên 1 - 01/01/2004</li>
                <li>Thành viên 2 - 02/02/2004</li>
                <li>Thành viên 3 - 03/03/2004</li>
            </ul>
        </section>
    </div>
    <div class="footer-bottom">2026 LinhNamStore. Jakarta EE JSP/Servlet project.</div>
</footer>
</body>
</html>

