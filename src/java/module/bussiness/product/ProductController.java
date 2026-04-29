/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package module.bussiness.product;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import module.bussiness.cart.dto.CartItemView;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.ProductCardView;
import module.bussiness.product.dto.ProductDetailView;
import entity.ProductReviewEntity;

/**
 *
 * @author An
 */
@WebServlet(name = "product", urlPatterns = {"/product"})
public class ProductController extends HttpServlet {
    // Service layer: controller chỉ điều phối request/response, không viết SQL trực tiếp.
    private final ProductService productService = new ProductService();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProductController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProductController at" + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean isAdmin = "1".equals(request.getParameter("admin"));
        String id = request.getParameter("id");
        if (id != null && !id.trim().isEmpty()) {
            try {
                renderProductDetail(request, response, id.trim(), null);
                return;
            } catch (Exception e) {
                request.setAttribute("error", "Không thể tải chi tiết sản phẩm: " + e.getMessage());
                request.getRequestDispatcher("/views/product/list.jsp").forward(request, response);
                return;
            }
        }

        String category = request.getParameter("category");
        List<ProductCardView> productCards = null;
        List<ProductCardView> allCards = null;
        try {
            allCards = productService.getProductCards();
            productCards = productService.getProductCards(category);
        } catch (Exception e) {
            request.setAttribute("error", "Cannot load products: " + e.getMessage());
            productCards = Collections.emptyList();
            allCards = Collections.emptyList();
        }

        Set<String> categories = new LinkedHashSet<>();
        for (ProductCardView card : allCards) {
            if (card.getCategory() != null && !card.getCategory().trim().isEmpty()) {
                categories.add(card.getCategory().trim().toUpperCase());
            }
        }

        List<ProductCardView> featuredProducts;
        try {
            featuredProducts = productService.getFeaturedProducts(6);
        } catch (Exception e) {
            featuredProducts = productCards == null ? Collections.emptyList() : productCards;
        }

        request.setAttribute("productCards", productCards);
        request.setAttribute("featuredProducts", featuredProducts);
        request.setAttribute("categories", categories);
        request.setAttribute("selectedCategory", category == null ? "" : category.trim().toUpperCase());
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("cartCount", countCartQuantity(request.getSession(false)));
        request.getRequestDispatcher("/views/product/list.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("review".equalsIgnoreCase(action)) {
            String productId = request.getParameter("productId");
            String reviewerName = request.getParameter("reviewerName");
            String comment = request.getParameter("comment");
            int rating = parseIntOrDefault(request.getParameter("rating"), 5);
            rating = Math.max(1, Math.min(rating, 5));
            try {
                if (reviewerName == null || reviewerName.trim().isEmpty()) {
                    renderProductDetail(request, response, productId, "Vui lòng nhập tên người đánh giá.");
                    return;
                }
                productService.createReview(productId, reviewerName.trim(), rating, comment);
                response.sendRedirect(request.getContextPath() + "/product?id=" + productId);
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
           
            response.sendRedirect(request.getContextPath() + "/product?admin=1");
        } catch (Exception e) {
            request.setAttribute("error", "Create product failed: " + e.getMessage());
            doGet(request, response);
        }
    }
    

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

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
}
