<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%>
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
            if (sidebarSelectedCategory == null) {
                sidebarSelectedCategory = "";
            }
            if (sidebarSelectedSubcategory == null) {
                sidebarSelectedSubcategory = "";
            }
            Map<String, Integer> categoryCounts = (Map<String, Integer>) request.getAttribute("categoryCounts");
            int storageDeviceCount = categoryCounts == null || categoryCounts.get("STORAGE_DEVICE") == null ? 0 : categoryCounts.get("STORAGE_DEVICE");
            int networkDeviceCount = categoryCounts == null || categoryCounts.get("NETWORK_DEVICE") == null ? 0 : categoryCounts.get("NETWORK_DEVICE");
            int accessoryCount = categoryCounts == null || categoryCounts.get("ACCESSORY") == null ? 0 : categoryCounts.get("ACCESSORY");
            java.util.Map<String, String> categoryLabels = new java.util.LinkedHashMap<>();
            categoryLabels.put("STORAGE_DEVICE", "Thiết bị lưu trữ");
            categoryLabels.put("NETWORK_DEVICE", "Thiết bị mạng");
            categoryLabels.put("ACCESSORY", "Phụ kiện");
            java.util.Map<String, Integer> topLevelCounts = new java.util.LinkedHashMap<>();
            topLevelCounts.put("STORAGE_DEVICE", storageDeviceCount);
            topLevelCounts.put("NETWORK_DEVICE", networkDeviceCount);
            topLevelCounts.put("ACCESSORY", accessoryCount);

            String[][] subcategories = new String[][]{
                {"HDD", "STORAGE_DEVICE"},
                {"SSD", "STORAGE_DEVICE"},
                {"NAS", "NETWORK_DEVICE"},
                {"ROUTER", "NETWORK_DEVICE"},
                {"SWITCH", "NETWORK_DEVICE"},
                {"CABLE", "ACCESSORY"},
                {"FLASH_DRIVE", "ACCESSORY"},
                {"MEMORY_CARD", "ACCESSORY"}
            };

            if (sidebarCategories != null && !sidebarCategories.isEmpty()) {
                for (String cat : sidebarCategories) {
                    String label = categoryLabels.getOrDefault(cat, cat.replace('_', ' '));
                    int categoryCount = topLevelCounts.getOrDefault(cat, 0);
                    boolean isActive = cat.equals(sidebarSelectedCategory) && sidebarSelectedSubcategory.isBlank();
        %>
        <a href="${pageContext.request.contextPath}/product?category=<%= cat %>"
           class="left-menu-item<%= isActive ? " active" : "" %>">
            <span class="left-menu-dot<%= isActive ? " active" : "" %>"></span>
            <%= label %> (<%= categoryCount %>)
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
            Thiết bị lưu trữ (<%= storageDeviceCount %>)
        </a>
        <a href="${pageContext.request.contextPath}/product?category=NETWORK_DEVICE" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Thiết bị mạng (<%= networkDeviceCount %>)
        </a>
        <a href="${pageContext.request.contextPath}/product?category=ACCESSORY" class="left-menu-item">
            <span class="left-menu-dot"></span>
            Phụ kiện (<%= accessoryCount %>)
        </a>
        <% } %>

        <% for (String[] subcategoryItem : subcategories) {
            String sub = subcategoryItem[0];
            String subCategory = subcategoryItem[1];
            boolean isSubActive = sub.equals(sidebarSelectedSubcategory);
            String displaySub = sub.replace('_', ' ');
        %>
        <a href="${pageContext.request.contextPath}/product?category=<%= subCategory %>&subcategory=<%= sub %>"
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
