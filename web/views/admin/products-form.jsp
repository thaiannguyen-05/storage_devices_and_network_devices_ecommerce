<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="entity.BrandEntity"%>
<%@page import="entity.ProductVariantEntity"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.response_dto.AdminProductResponseDto"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminProductResponseDto product = (AdminProductResponseDto) request.getAttribute("product");
    List<BrandEntity> brands = (List<BrandEntity>) request.getAttribute("brands");
    boolean editMode = Boolean.TRUE.equals(request.getAttribute("editMode"));
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Products</p>
            <h1><%= editMode ? "Edit product" : "Create product" %></h1>
        </div>
        <a class="admin-secondary-link" href="<%= request.getContextPath() %>/admin?action=products">Back</a>
    </div>

    <div class="admin-grid-two">
        <form class="admin-form-panel" method="post" action="<%= request.getContextPath() %>/admin?action=products-save">
            <input type="hidden" name="id" value="<%= product == null ? "" : AdminService.escapeHtml(product.getId()) %>">
            <label>Name
                <input type="text" name="name" required value="<%= product == null ? "" : AdminService.escapeHtml(product.getName()) %>">
            </label>
            <label>Description
                <textarea name="description" rows="5"><%= product == null ? "" : AdminService.escapeHtml(product.getDescription()) %></textarea>
            </label>
            <label>Brand
                <select name="brandId" required>
                    <% if (brands != null) { for (BrandEntity brand : brands) { %>
                        <option value="<%= AdminService.escapeHtml(brand.getId()) %>" <%= product != null && brand.getId().equals(product.getBrandId()) ? "selected" : "" %>><%= AdminService.escapeHtml(brand.getName()) %></option>
                    <% }} %>
                </select>
            </label>
            <label>Category
                <select name="category">
                    <% for (String category : AdminService.PRODUCT_CATEGORIES) { %>
                        <option value="<%= category %>" <%= product != null && category.equalsIgnoreCase(product.getCategory()) ? "selected" : "" %>><%= category %></option>
                    <% } %>
                </select>
            </label>
            <label>Status
                <select name="status">
                    <% for (String status : AdminService.PRODUCT_STATUSES) { %>
                        <option value="<%= status %>" <%= product != null && status.equalsIgnoreCase(product.getStatus()) ? "selected" : "" %>><%= status %></option>
                    <% } %>
                </select>
            </label>
            <div class="admin-form-actions">
                <button type="submit">Save product</button>
                <a href="<%= request.getContextPath() %>/admin?action=products">Cancel</a>
            </div>
        </form>

        <section class="admin-panel">
            <div class="admin-panel-head"><h2>Variants</h2></div>
            <% if (!editMode || product == null) { %>
                <p class="admin-empty">Save the product before adding variants.</p>
            <% } else { %>
                <table class="admin-table compact">
                    <thead><tr><th>SKU</th><th>Price</th><th>Qty</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% for (ProductVariantEntity variant : product.getVariants()) { %>
                        <tr>
                            <td><%= AdminService.escapeHtml(variant.getSku()) %></td>
                            <td><%= variant.getPrice() %></td>
                            <td><%= variant.getQuantity() %></td>
                            <td><%= AdminService.escapeHtml(variant.getStatus()) %></td>
                            <td>
                                <form method="post" action="<%= request.getContextPath() %>/admin?action=products-variant-delete">
                                    <input type="hidden" name="productId" value="<%= AdminService.escapeHtml(product.getId()) %>">
                                    <input type="hidden" name="variantId" value="<%= AdminService.escapeHtml(variant.getId()) %>">
                                    <button type="submit" class="danger">Delete</button>
                                </form>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="5">
                                <form class="admin-inline-form" method="post" action="<%= request.getContextPath() %>/admin?action=products-variant-save">
                                    <input type="hidden" name="productId" value="<%= AdminService.escapeHtml(product.getId()) %>">
                                    <input type="hidden" name="variantId" value="<%= AdminService.escapeHtml(variant.getId()) %>">
                                    <input name="sku" value="<%= AdminService.escapeHtml(variant.getSku()) %>" required>
                                    <input name="price" value="<%= variant.getPrice() %>" required>
                                    <input name="quantity" value="<%= variant.getQuantity() %>" required>
                                    <input name="imageUrl" value="<%= AdminService.escapeHtml(variant.getImageUrl()) %>">
                                    <select name="variantStatus">
                                        <% for (String status : AdminService.VARIANT_STATUSES) { %>
                                            <option value="<%= status %>" <%= status.equalsIgnoreCase(variant.getStatus()) ? "selected" : "" %>><%= status %></option>
                                        <% } %>
                                    </select>
                                    <button type="submit">Update</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                    <% if (product.getVariants().isEmpty()) { %>
                        <tr><td colspan="5" class="admin-empty-cell">No variants yet.</td></tr>
                    <% } %>
                    </tbody>
                </table>
                <form class="admin-form-panel admin-variant-create" method="post" action="<%= request.getContextPath() %>/admin?action=products-variant-save">
                    <input type="hidden" name="productId" value="<%= AdminService.escapeHtml(product.getId()) %>">
                    <label>SKU <input name="sku" required></label>
                    <label>Price <input name="price" required></label>
                    <label>Quantity <input name="quantity" required></label>
                    <label>Image URL <input name="imageUrl"></label>
                    <label>Status
                        <select name="variantStatus">
                            <% for (String status : AdminService.VARIANT_STATUSES) { %>
                                <option value="<%= status %>"><%= status %></option>
                            <% } %>
                        </select>
                    </label>
                    <button type="submit">Add variant</button>
                </form>
            <% } %>
        </section>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
