<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="entity.ProductReviewEntity"%>
<%@page import="module.bussiness.admin.AdminService"%>
<%@page import="module.bussiness.admin.AdminService.AdminPage"%>
<%@include file="../includes/layout.jsp" %>
<%
    AdminPage<ProductReviewEntity> reviewsPage = (AdminPage<ProductReviewEntity>) request.getAttribute("reviewsPage");
    boolean reviewTableAvailable = Boolean.TRUE.equals(request.getAttribute("reviewTableAvailable"));
    String search = (String) request.getAttribute("search");
    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<section class="admin-shell">
    <div class="admin-page-head">
        <div>
            <p class="admin-eyebrow">Admin</p>
            <h1>Reviews</h1>
        </div>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="admin-alert admin-alert-error"><%= AdminService.escapeHtml(request.getAttribute("error")) %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <div class="admin-alert admin-alert-success"><%= AdminService.escapeHtml(request.getAttribute("success")) %></div>
    <% } %>

    <% if (!reviewTableAvailable) { %>
        <div class="admin-alert admin-alert-warning">Reviews table not yet created.</div>
    <% } %>

    <form class="admin-filter" method="get" action="<%= request.getContextPath() %>/admin">
        <input type="hidden" name="action" value="reviews">
        <input type="search" name="search" placeholder="Search reviewer or productId" value="<%= AdminService.escapeHtml(search) %>">
        <button type="submit">Filter</button>
    </form>

    <section class="admin-panel">
        <table class="admin-table">
            <thead><tr><th>ID</th><th>Product</th><th>Reviewer</th><th>Rating</th><th>Comment</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
            <% for (ProductReviewEntity review : reviewsPage.getItems()) { %>
                <tr>
                    <td><%= AdminService.escapeHtml(review.getId()) %></td>
                    <td><%= AdminService.escapeHtml(review.getProductId()) %></td>
                    <td><%= AdminService.escapeHtml(review.getReviewerName()) %></td>
                    <td><%= review.getRating() %></td>
                    <td><%= AdminService.escapeHtml(review.getComment()) %></td>
                    <td><%= review.getCreatedAt() == null ? "" : dt.format(review.getCreatedAt()) %></td>
                    <td class="admin-actions">
                        <form class="admin-inline-form" method="post" action="<%= request.getContextPath() %>/admin?action=reviews-moderate">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(review.getId()) %>">
                            <input name="rating" value="<%= review.getRating() %>">
                            <input name="comment" value="<%= AdminService.escapeHtml(review.getComment()) %>">
                            <button type="submit">Update</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/admin?action=reviews-delete">
                            <input type="hidden" name="id" value="<%= AdminService.escapeHtml(review.getId()) %>">
                            <button type="submit" class="danger">Delete</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            <% if (reviewsPage.getItems().isEmpty()) { %>
                <tr><td colspan="7" class="admin-empty-cell">No reviews found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <div class="admin-pagination">
        <% if (reviewsPage.hasPrevious()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=reviews&page=<%= reviewsPage.getPage() - 1 %>&search=<%= AdminService.escapeHtml(search) %>">Previous</a>
        <% } %>
        <span>Page <%= reviewsPage.getPage() %> / <%= reviewsPage.getTotalPages() %></span>
        <% if (reviewsPage.hasNext()) { %>
            <a href="<%= request.getContextPath() %>/admin?action=reviews&page=<%= reviewsPage.getPage() + 1 %>&search=<%= AdminService.escapeHtml(search) %>">Next</a>
        <% } %>
    </div>
</section>

<%@include file="../includes/layout-end.jsp" %>
