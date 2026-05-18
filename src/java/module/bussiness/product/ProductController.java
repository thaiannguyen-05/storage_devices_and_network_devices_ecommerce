package module.bussiness.product;

import common.annotation.Public;
import common.controller.BaseController;
import common.guard.AuthGuard;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import module.bussiness.product.dto.CreateProductDto;
import module.bussiness.product.dto.CreateVariantDto;
import module.bussiness.product.dto.UpdateProductDto;
import module.bussiness.product.dto.UpdateVariantDto;
import module.bussiness.product.response_dto.CreateProductResponseDto;

@Public
@WebServlet(name = "Product", urlPatterns = {"/home", "/product", "/products", "/admin/products"})
public class ProductController extends BaseController {
    private final ProductService productService = new ProductService();
    private final VariantService variantService = new VariantService();
    private final AuthGuard authGuard = new AuthGuard();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        checkAndCreateTestProduct();
        String action = action(req, "list");
        if ("/home".equals(req.getServletPath()) && !"autocomplete".equals(action)) {
            action = req.getParameter("keyword") == null || req.getParameter("keyword").trim().isEmpty() ? "home" : "search";
        } else if ("/product".equals(req.getServletPath())) {
            action = "detail";
        }
        if (isAdminPath(req) && !authGuard.checkRole(req, res, "ADMIN")) {
            return;
        }
        if (isAdminPath(req) && ("create".equals(action) || "edit".equals(action))) {
            if ("edit".equals(action)) {
                req.setAttribute("productResult", productService.getProductDetail(req.getParameter("id")));
            }
            req.setAttribute("brandsResult", productService.getAllBrands());
            forwardToJsp(req, res, "/admin/product-form.jsp");
            return;
        }
        switch (action) {
            case "autocomplete":
                String keyword = req.getParameter("keyword");
                java.util.List<ProductCardView> suggestions = productService.autocomplete(keyword);
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < suggestions.size(); i++) {
                    ProductCardView p = suggestions.get(i);
                    sb.append("{");
                    sb.append("\"id\":\"").append(escapeJson(p.getId())).append("\",");
                    sb.append("\"name\":\"").append(escapeJson(p.getName())).append("\",");
                    sb.append("\"imageUrl\":\"").append(escapeJson(p.getImageUrl())).append("\",");
                    sb.append("\"price\":").append(p.getPrice() != null ? p.getPrice().toString() : "0");
                    sb.append("}");
                    if (i < suggestions.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append("]");
                res.getWriter().write(sb.toString());
                break;
            case "detail":
                module.bussiness.product.response_dto.GetProductResponseDto detail = productService.getProductDetail(req.getParameter("id"));
                req.setAttribute("productResult", detail);
                req.setAttribute("product", detail.getProduct());
                req.setAttribute("variants", detail.getVariants());
                forwardToJsp(req, res, "/pages/product-detail.jsp");
                break;
            case "search":
                req.setAttribute("productsResult", productService.searchProducts(req.getParameter("keyword"), parseInt(req.getParameter("page"), 1)));
                forwardToJsp(req, res, "/pages/home.jsp");
                break;
            case "category":
                req.setAttribute("productsResult", productService.findByCategory(req.getParameter("category"), parseInt(req.getParameter("page"), 1)));
                forwardToJsp(req, res, "/pages/home.jsp");
                break;
            case "home":
                java.util.Map<String, Object> homeData = productService.getHomePage();
                req.setAttribute("homeData", homeData);
                req.setAttribute("newProducts", homeData.get("recent"));
                
                String[] categories = req.getParameterValues("category");
                String[] brands = req.getParameterValues("brand");
                String priceRange = req.getParameter("price");
                String filterStatus = req.getParameter("status");
                String sort = req.getParameter("sort");
                
                boolean hasFilter = (categories != null && categories.length > 0)
                        || (brands != null && brands.length > 0)
                        || (priceRange != null && !priceRange.trim().isEmpty())
                        || (filterStatus != null && !filterStatus.trim().isEmpty())
                        || (sort != null && !sort.trim().isEmpty());
                
                if (hasFilter) {
                    req.setAttribute("products", productService.filterProducts(categories, brands, priceRange, filterStatus, sort));
                    req.setAttribute("isFiltered", true);
                } else {
                    req.setAttribute("products", homeData.get("recent"));
                }
                forwardToJsp(req, res, "/pages/home.jsp");
                break;
            default:
                req.setAttribute("productsResult", productService.listProducts(parseInt(req.getParameter("page"), 1)));
                req.setAttribute("brandsResult", productService.getAllBrands());
                forwardToJsp(req, res, isAdminPath(req) ? "/admin/product-list.jsp" : "/pages/home.jsp");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!isAdminPath(req)) {
            redirect(req, res, "/product?id=" + safe(req.getParameter("productId")));
            return;
        }
        if (isAdminPath(req) && !authGuard.checkRole(req, res, "ADMIN")) {
            return;
        }
        String action = action(req, "list");
        if ("create".equals(action)) {
            CreateProductResponseDto product = productService.createProduct(createProductDto(req));
            if (product.isSuccess() && req.getParameter("sku") != null && !req.getParameter("sku").trim().isEmpty()) {
                CreateVariantDto variant = createVariantDto(req);
                variant.setProductId(product.getProductId());
                variantService.createVariant(variant);
            }
        } else if ("edit".equals(action) || "toggle-status".equals(action)) {
            productService.updateProduct(updateProductDto(req));
        } else if ("delete".equals(action)) {
            productService.deleteProduct(req.getParameter("id"));
        } else if ("add-variant".equals(action)) {
            variantService.createVariant(createVariantDto(req));
        } else if ("edit-variant".equals(action)) {
            variantService.updateVariant(updateVariantDto(req));
        } else if ("delete-variant".equals(action)) {
            variantService.deleteVariant(req.getParameter("id"));
        }
        redirect(req, res, "/admin/products?action=list");
    }

    private CreateProductDto createProductDto(HttpServletRequest req) {
        CreateProductDto dto = new CreateProductDto();
        UserPayload user = getUserFromSession(req);
        dto.setName(req.getParameter("name"));
        dto.setDescription(req.getParameter("description"));
        dto.setBrandId(req.getParameter("brandId"));
        dto.setStatus(req.getParameter("status"));
        dto.setCategory(req.getParameter("category"));
        dto.setUserId(user == null ? req.getParameter("userId") : user.getUserId());
        return dto;
    }

    private UpdateProductDto updateProductDto(HttpServletRequest req) {
        UpdateProductDto dto = new UpdateProductDto();
        dto.setId(req.getParameter("id"));
        dto.setName(req.getParameter("name"));
        dto.setDescription(req.getParameter("description"));
        dto.setBrandId(req.getParameter("brandId"));
        dto.setStatus(req.getParameter("status"));
        dto.setCategory(req.getParameter("category"));
        return dto;
    }

    private CreateVariantDto createVariantDto(HttpServletRequest req) {
        CreateVariantDto dto = new CreateVariantDto();
        dto.setProductId(req.getParameter("productId"));
        dto.setPrice(parseDecimal(req.getParameter("price")));
        dto.setImageUrl(req.getParameter("imageUrl"));
        dto.setStatus(req.getParameter("status"));
        dto.setSku(req.getParameter("sku"));
        dto.setQuantity(parseInt(req.getParameter("quantity"), 0));
        return dto;
    }

    private UpdateVariantDto updateVariantDto(HttpServletRequest req) {
        UpdateVariantDto dto = new UpdateVariantDto();
        dto.setId(req.getParameter("id"));
        dto.setProductId(req.getParameter("productId"));
        dto.setPrice(parseDecimal(req.getParameter("price")));
        dto.setImageUrl(req.getParameter("imageUrl"));
        dto.setStatus(req.getParameter("status"));
        dto.setSku(req.getParameter("sku"));
        dto.setQuantity(parseInt(req.getParameter("quantity"), 0));
        return dto;
    }

    private boolean isAdminPath(HttpServletRequest req) {
        return req.getServletPath().startsWith("/admin");
    }

    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private BigDecimal parseDecimal(String value) {
        try {
            return value == null ? null : new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private void checkAndCreateTestProduct() {
        try {
            int count = module.core.sql.JdbcHelper.count("SELECT COUNT(*) FROM `Product` WHERE id = 'p-test-1k'");
            if (count == 0) {
                String adminId = "11111111-1111-1111-1111-111111111111";
                String brandId = "b1111111-1111-1111-1111-111111111111";
                
                module.core.sql.JdbcHelper.executeUpdate(
                    "INSERT INTO `Product` (id, name, description, brandId, status, userId, category) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    "p-test-1k", "Thiết bị Test Cổng Thanh Toán 2K", "Sản phẩm dùng thử nghiệm thanh toán cổng tự động Sepay.",
                    brandId, "ACTIVE", adminId, "ACCESSORY"
                );
                
                module.core.sql.JdbcHelper.executeUpdate(
                    "INSERT INTO `ProductVariant` (id, productId, price, imageUrl, status, sku, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    "v-test-1k", "p-test-1k", new java.math.BigDecimal("2000.00"),
                    "https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80",
                    "ACTIVE", "TEST-SEPAY-2K", 999
                );
                System.out.println("[ProductController] Created test product variant TEST-SEPAY-2K (2,000 VND) successfully!");
            } else {
                // Ensure name and price are updated to 2K even if the product was already created as 1K
                module.core.sql.JdbcHelper.executeUpdate(
                    "UPDATE `Product` SET name = 'Thiết bị Test Cổng Thanh Toán 2K' WHERE id = 'p-test-1k'"
                );
                module.core.sql.JdbcHelper.executeUpdate(
                    "UPDATE `ProductVariant` SET price = ?, sku = 'TEST-SEPAY-2K' WHERE id = 'v-test-1k'",
                    new java.math.BigDecimal("2000.00")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
