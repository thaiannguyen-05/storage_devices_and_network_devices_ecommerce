package module.bussiness.product;

import common.annotation.Role;
import entity.ProductReviewEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import module.bussiness.cart.dto.CartItemView;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.ProductCardView;
import module.bussiness.product.dto.ProductDetailView;
import module.bussiness.product.dto.UpdateProduct;

@WebServlet(name = "product", urlPatterns = {"/product"})
public class ProductController extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean isAdmin = "1".equals(request.getParameter("admin"));
        String id = request.getParameter("id");

        if (id != null && !id.trim().isEmpty()) {
            try {
                renderProductDetail(request, response, id.trim(), null);
                return;
            } catch (Throwable e) {
                request.setAttribute("error", "Không thể tải chi tiết sản phẩm: " + e.getMessage());
                request.setAttribute("productCards", Collections.emptyList());
                request.setAttribute("featuredProducts", Collections.emptyList());
                request.setAttribute("categories", new LinkedHashSet<>());
                request.setAttribute("selectedCategory", "");
                request.setAttribute("isAdmin", isAdmin);
                request.setAttribute("cartCount", countCartQuantity(request.getSession(false)));
                request.getRequestDispatcher("/views/product/list.jsp").forward(request, response);
                return;
            }
        }

        String category = request.getParameter("category");
        String subcategory = request.getParameter("subcategory");
        List<ProductCardView> productCards;
        List<ProductCardView> allCards;
        try {
            allCards = productService.getProductCards();
            productCards = filterByCategory(allCards, category, subcategory);
        } catch (Throwable e) {
            request.setAttribute("error", "Cannot load products: " + e.getMessage());
            productCards = Collections.emptyList();
            allCards = Collections.emptyList();
        }

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("HDD", 0);
        categoryCounts.put("SSD", 0);
        categoryCounts.put("NAS", 0);
        categoryCounts.put("USB", 0);
        categoryCounts.put("MEMORY_CARD", 0);
        categoryCounts.put("TAPE", 0);
        categoryCounts.put("ENCLOSURE", 0);

        for (ProductCardView card : allCards) {
            if (card.getCategory() == null || card.getCategory().trim().isEmpty()) {
                continue;
            }
            String key = card.getCategory().trim().toUpperCase();
            if (!categoryCounts.containsKey(key)) {
                categoryCounts.put(key, 0);
            }
            categoryCounts.put(key, categoryCounts.get(key) + 1);
        }

        Set<String> categories = new LinkedHashSet<>(categoryCounts.keySet());

        List<ProductCardView> featuredProducts;
        try {
            featuredProducts = productService.getFeaturedProducts(allCards, Integer.MAX_VALUE);
        } catch (Exception e) {
            featuredProducts = productCards;
        }

        request.setAttribute("productCards", productCards);
        request.setAttribute("featuredProducts", featuredProducts);
        request.setAttribute("categories", categories);
        request.setAttribute("categoryCounts", categoryCounts);
        request.setAttribute("selectedCategory", category == null ? "" : category.trim().toUpperCase());
        request.setAttribute("selectedSubcategory", subcategory == null ? "" : subcategory.trim().toUpperCase());
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("cartCount", countCartQuantity(request.getSession(false)));
        request.getRequestDispatcher("/views/product/list.jsp").forward(request, response);
    }

    @Override
    @Role(value = {"USER", "ADMIN"}, actions = "review")
    @Role("ADMIN")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        if ("review".equalsIgnoreCase(action)) {
            String productId = request.getParameter("productId");
            String reviewerName = request.getParameter("reviewerName");
            String comment = request.getParameter("comment");
            int rating = parseIntOrDefault(request.getParameter("rating"), 5);
            rating = Math.max(1, Math.min(rating, 5));
            try {
                Object authUserName = request.getSession(true).getAttribute("authUserName");
                String finalReviewerName = authUserName == null ? reviewerName : String.valueOf(authUserName);
                if (finalReviewerName == null || finalReviewerName.trim().isEmpty()) {
                    renderProductDetail(request, response, productId, "Vui lòng đăng nhập hoặc nhập tên người đánh giá.");
                    return;
                }
                productService.createReview(productId, finalReviewerName.trim(), rating, comment);
                response.sendRedirect("/product?id=" + productId);
            } catch (Exception e) {
                try {
                    renderProductDetail(request, response, productId, "Gửi đánh giá thất bại: " + e.getMessage());
                } catch (Exception inner) {
                    throw new ServletException(inner);
                }
            }
            return;
        }

        boolean isAdmin = "1".equals(request.getParameter("admin"));
        if (!isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("error", "Only admin can create products.");
            doGet(request, response);
            return;
        }

        CreateProduct dto = new CreateProduct();
        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));
        dto.setBrandId(request.getParameter("brandId"));
        dto.setStatus(request.getParameter("status"));
        dto.setUserId(request.getParameter("userId"));
        dto.setCategory(request.getParameter("category"));

        try {
            productService.createProduct(dto);
            response.sendRedirect("/product?admin=1");
        } catch (Exception e) {
            request.setAttribute("error", "Create product failed: " + e.getMessage());
            doGet(request, response);
        }
    }

    @Override
    @Role("ADMIN")
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String id = request.getParameter("id");

        UpdateProduct dto = new UpdateProduct();
        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));
        dto.setBrandId(request.getParameter("brandId"));
        dto.setStatus(request.getParameter("status"));
        dto.setCategory(request.getParameter("category"));

        try (PrintWriter out = response.getWriter()) {
            boolean ok = productService.updateProduct(id, dto);
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out .println("Update product failed");
                return;
            }
            out.println("Update product success");
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Throwable cause = e.getCause();
            String message = cause != null ? cause.getMessage() : e.getMessage();
            response.getWriter().println("Database error: " + message);
        }
    }

    @Override
    @Role("ADMIN")
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String id = request.getParameter("id");

        try (PrintWriter out = response.getWriter()) {
            boolean ok = productService.deleteProduct(id);
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Delete product failed");
                return;
            }
            out.println("Delete product success");
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Throwable cause = e.getCause();
            String message = cause != null ? cause.getMessage() : e.getMessage();
            response.getWriter().println("Database error: " + message);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    @SuppressWarnings("unchecked")
    private int countCartQuantity(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Object raw = session.getAttribute("cartItems");
        if (!(raw instanceof Map)) {
            return 0;
        }
        int count = 0;
        Map<String, CartItemView> items = (Map<String, CartItemView>) raw;
        for (CartItemView item : items.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    private void renderProductDetail(HttpServletRequest request, HttpServletResponse response, String productId, String reviewError)
            throws Exception {
        ProductDetailView detail = productService.getProductDetail(productId);
        if (detail == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("error", "Không tìm thấy sản phẩm.");
            request.getRequestDispatcher("/views/product/list.jsp").forward(request, response);
            return;
        }

        List<String> galleryImages = productService.getGalleryImages(productId);
        List<ProductCardView> relatedProducts = productService.getRelatedProducts(productId, detail.getCategory(), 10);
        List<ProductReviewEntity> reviews = productService.getProductReviews(productId);

        request.setAttribute("productDetail", detail);
        request.setAttribute("productVariants", productService.getProductVariants(productId));
        request.setAttribute("galleryImages", galleryImages);
        request.setAttribute("relatedProducts", relatedProducts);
        request.setAttribute("reviews", reviews);
        request.setAttribute("reviewError", reviewError);
        request.setAttribute("cartCount", countCartQuantity(request.getSession(false)));
        request.getRequestDispatcher("/views/product/detail.jsp").forward(request, response);
    }

    private int parseIntOrDefault(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<ProductCardView> filterByCategory(List<ProductCardView> source, String category, String subcategory) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedCat = category == null ? "" : category.trim().toUpperCase();
        String normalizedSub = subcategory == null ? "" : subcategory.trim().toUpperCase();
        if (normalizedCat.isEmpty() && normalizedSub.isEmpty()) {
            return source;
        }

        java.util.ArrayList<ProductCardView> filtered = new java.util.ArrayList<>();
        for (ProductCardView card : source) {
            String c = card.getCategory() == null ? "" : card.getCategory().trim().toUpperCase();
            boolean matchCat = normalizedCat.isEmpty() || normalizedCat.equals(c);
            boolean matchSub = normalizedSub.isEmpty() || c.contains(normalizedSub);
            if (matchCat && matchSub) {
                filtered.add(card);
            }
        }
        return filtered;
    }
}
