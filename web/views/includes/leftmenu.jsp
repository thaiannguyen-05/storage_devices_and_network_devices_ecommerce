<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<aside class="left-menu">
    <% if ("ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("authUserRole")))) { %>
    <div class="left-menu-section">
        <h3 class="left-menu-title">Admin</h3>
        <a href="${pageContext.request.contextPath}/admin" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Dashboard
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=users" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Users
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=products" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Products
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=orders" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Orders
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=payments" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Payments
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=vouchers" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Vouchers
        </a>
        <a href="${pageContext.request.contextPath}/admin?action=reviews" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Reviews
        </a>
    </div>
    <% } %>

    <div class="left-menu-section">
        <h3 class="left-menu-title">Danh mục</h3>
        <%
            Set<String> sidebarCategories = (Set<String>) request.getAttribute("categories");
            String sidebarSelectedCategory = (String) request.getAttribute("selectedCategory");
            String sidebarSelectedSubcategory = (String) request.getAttribute("selectedSubcategory");
            java.util.Map<String, String> categoryLabels = new java.util.LinkedHashMap<>();
            categoryLabels.put("STORAGE_DEVICE", "Thiết bị lưu trữ");
            categoryLabels.put("NETWORK_DEVICE", "Thiết bị mạng");
            categoryLabels.put("ACCESSORY", "Phụ kiện");

            String[] subcategories = new String[]{"HDD", "SSD", "NAS", "USB", "MEMORY_CARD", "TAPE", "ENCLOSURE"};

            if (sidebarCategories != null && !sidebarCategories.isEmpty()) {
                for (String cat : sidebarCategories) {
                    String label = categoryLabels.getOrDefault(cat, cat.replace('_', ' '));
                    boolean isActive = cat.equals(sidebarSelectedCategory) && sidebarSelectedSubcategory == null;
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

        <% for (String sub : subcategories) {
            boolean isSubActive = sub.equals(sidebarSelectedSubcategory);
            String displaySub = sub.replace('_', ' ');
        %>
        <a href="${pageContext.request.contextPath}/product?subcategory=<%= sub %>"
           class="left-menu-item left-menu-subitem<%= isSubActive ? " active" : "" %>">
            <span class="left-menu-dot<%= isSubActive ? " active" : "" %>"></span>
            <%= displaySub %>
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
