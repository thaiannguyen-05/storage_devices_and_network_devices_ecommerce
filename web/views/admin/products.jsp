<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="entity.BrandEntity"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@page import="module.bussiness.admin.response_dto.AdminProductResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<AdminProductResponseDto> productsPage = (AdminPage<AdminProductResponseDto>) request.getAttribute("productsPage");
    List<BrandEntity> brands = (List<BrandEntity>) request.getAttribute("brands");
    String search = (String) request.getAttribute("search");
    String categoryFilter = (String) request.getAttribute("categoryFilter");
    String statusFilter = (String) request.getAttribute("statusFilter");
    String brandFilter = (String) request.getAttribute("brandFilter");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Products</h1>
        </div>
        <a class="admin-primary-link" href="<%= request.getContextPath() %>/admin?action=products-create">Create product</a>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <form class="admin-filter" method="get" action="<%= request.getContextPath() %>/admin">
        <input type="hidden" name="action" value="products">
        <input type="search" name="search" placeholder="Search products" value="<%= AdminService.escapeHtml(search) %>">
        <select name="categoryFilter">
            <option value="">All categories</option>
            <% for (String category : AdminService.PRODUCT_CATEGORIES) { %>
                <option value="<%= category %>" <%= category.equalsIgnoreCase(categoryFilter) ? "selected" : "" %>><%= category %></option>
            <% } %>
        </select>
        <select name="statusFilter">
            <option value="">All statuses</option>
            <% for (String status : AdminService.PRODUCT_STATUSES) { %>
                <option value="<%= status %>" <%= status.equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= status %></option>
            <% } %>
        </select>
        <select name="brandFilter">
            <option value="">All brands</option>
            <% if (brands != null) { for (BrandEntity brand : brands) { %>
                <option value="<%= AdminService.escapeHtml(brand.getId()) %>" <%= brand.getId().equals(brandFilter) ? "selected" : "" %>><%= AdminService.escapeHtml(brand.getName()) %></option>
            <% }} %>
        </select>
        <button type="submit">Filter</button>
    </form>

    <section class="admin-panel">
        <table class="admin-table">
            <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Brand</th><th>Status</th><th>Variants</th><th>Stock</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (AdminProductResponseDto product : productsPage.getItems()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(product.getId()) %></td>
                    <td><%= AdminService.escapeHtml(product.getName()) %></td>
                    <td><%= AdminService.escapeHtml(product.getCategory()) %></td>
                    <td><%= AdminService.escapeHtml(product.getBrandName()) %></td>
                    <td><span class="admin-badge"><%= AdminService.escapeHtml(product.getStatus()) %></span></td>
                    <td><%= product.getVariantCount() %></td>
                    <td><%= product.getTotalStock() %></td>
                    <td class="admin-actions">
                        <a href="<%= request.getContextPath() %>/admin?action=products-edit&id=<%= AdminService.escapeHtml(product.getId()) %>">Edit</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=products-change-status">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(product.getId()) %>">
                            <select name="status">
                                <% for (String status : AdminService.PRODUCT_STATUSES) { %>
                                    <option value="<%= status %>" <%= status.equalsIgnoreCase(product.getStatus()) ? "selected" : "" %>><%= status %></option>
                                <% } %>
                            </select>
                            <button type="submit">Set</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=products-delete">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(product.getId()) %>">
                            <button type="submit" class="danger">Delete</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            <% if (productsPage.getItems().isEmpty()) { %>
                <tr><td colspan="8" class="admin-empty-cell">No products found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <div class="admin-pagination">
        <% if (productsPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=products&page=<%= productsPage.getPage() - 1 %>&search=<%= AdminService.escapeHtml(search) %>&categoryFilter=<%= AdminService.escapeHtml(categoryFilter) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>&brandFilter=<%= AdminService.escapeHtml(brandFilter) %>">Previous</a>
        <% } %>
        <span>Page <%= productsPage.getPage() %> / <%= productsPage.getTotalPages() %></span>
        <% if (productsPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=products&page=<%= productsPage.getPage() + 1 %>&search=<%= AdminService.escapeHtml(search) %>&categoryFilter=<%= AdminService.escapeHtml(categoryFilter) %>&statusFilter=<%= AdminService.escapeHtml(statusFilter) %>&brandFilter=<%= AdminService.escapeHtml(brandFilter) %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
