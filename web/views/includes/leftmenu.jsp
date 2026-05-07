<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<aside class="left-menu">
    <div class="left-menu-section">
        <h3 class="left-menu-title">Danh mục</h3>
        <%
            Set<String> categories = (Set<String>) request.getAttribute("categories");
            String selectedCategory = (String) request.getAttribute("selectedCategory");
            java.util.Map<String, String> categoryLabels = new java.util.LinkedHashMap<>();
            categoryLabels.put("STORAGE_DEVICE", "Thiết bị lưu trữ");
            categoryLabels.put("NETWORK_DEVICE", "Thiết bị mạng");
            categoryLabels.put("ACCESSORY", "Phụ kiện");

            if (categories != null && !categories.isEmpty()) {
                for (String cat : categories) {
                    String label = categoryLabels.getOrDefault(cat, cat.replace('_', ' '));
                    boolean isActive = cat.equals(selectedCategory);
        %>
        <a href="${pageContext.request.contextPath}/product?category=<%= cat %>"
           class="left-menu-item<%= isActive ? " active" : "" %>">
            <span class="left-menu-dot<%= isActive ? " active" : "" %>"></span>
            <%= label %>
        </a>
        <%
                }
            } else {
        %>
        <a href="${pageContext.request.contextPath}/product" class="left-menu-item active">
            <span class="left-menu-dot active"></span>
            Tất cả sản phẩm
        </a>
        <a href="${pageContext.request.contextPath}/product?category=STORAGE_DEVICE" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Thiết bị lưu trữ
        </a>
        <a href="${pageContext.request.contextPath}/product?category=NETWORK_DEVICE" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Thiết bị mạng
        </a>
        <a href="${pageContext.request.contextPath}/product?category=ACCESSORY" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Phụ kiện
        </a>
        <% } %>
    </div>

    <div class="left-menu-section">
        <h3 class="left-menu-title">Bộ lọc</h3>
        <a href="${pageContext.request.contextPath}/product?filter=new" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Hàng mới
        </a>
        <a href="${pageContext.request.contextPath}/product?filter=hot" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Hàng bán chạy
        </a>
        <a href="${pageContext.request.contextPath}/product?filter=sale" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Hàng giảm giá
        </a>
    </div>
</aside>
