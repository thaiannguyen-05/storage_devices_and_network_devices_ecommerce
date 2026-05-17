package module.bussiness.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import module.bussiness.admin.AdminService.AdminMutationResult;
import module.bussiness.admin.dto.AdminOrderRequestDto;
import module.bussiness.admin.dto.AdminProductRequestDto;
import module.bussiness.admin.dto.AdminProductVariantRequestDto;
import module.bussiness.admin.dto.AdminUserRequestDto;
import module.bussiness.admin.dto.AdminVoucherRequestDto;

@WebServlet(name = "admin", urlPatterns = {"/admin"})
public class AdminController extends HttpServlet {
    private static final String UTF_8 = "UTF-8";
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        if (!ensureAdmin(request, response)) {
            return;
        }

        consumeFlash(request);
        String action = value(request.getParameter("action"));
        if (action.isBlank()) {
            action = "dashboard";
        }

        switch (action) {
            case "users":
                showUsers(request, response);
                break;
            case "users-edit":
                showUserForm(request, response, true);
                break;
            case "users-create":
                showUserForm(request, response, false);
                break;
            case "products":
                showProducts(request, response);
                break;
            case "products-edit":
                showProductForm(request, response, true);
                break;
            case "products-create":
                showProductForm(request, response, false);
                break;
            case "orders":
                showOrders(request, response);
                break;
            case "order-detail":
                showOrderDetail(request, response);
                break;
            case "payments":
                showPayments(request, response);
                break;
            case "vouchers":
            case "vouchers-edit":
            case "vouchers-create":
                showVouchers(request, response, action);
                break;
            case "reviews":
                showReviews(request, response);
                break;
            case "dashboard":
            default:
                showDashboard(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        if (!ensureAdmin(request, response)) {
            return;
        }

        String action = value(request.getParameter("action"));
        switch (action) {
            case "users-save":
                handleUserSave(request, response);
                break;
            case "users-delete":
                flashAndRedirect(request, response, adminService.deleteUser(request.getParameter("id")), "users");
                break;
            case "users-activate":
                flashAndRedirect(request, response, adminService.setUserStatus(request.getParameter("id"), "ACTIVE"), "users");
                break;
            case "users-ban":
                flashAndRedirect(request, response, adminService.setUserStatus(request.getParameter("id"), "BANNED"), "users");
                break;
            case "products-save":
                handleProductSave(request, response);
                break;
            case "products-delete":
                flashAndRedirect(request, response, adminService.deleteProduct(request.getParameter("id")), "products");
                break;
            case "products-change-status":
                flashAndRedirect(request, response, adminService.changeProductStatus(request.getParameter("id"), request.getParameter("status")), "products");
                break;
            case "products-variant-save":
                handleVariantSave(request, response);
                break;
            case "products-variant-delete":
                handleVariantDelete(request, response);
                break;
            case "orders-update-status":
                handleOrderStatus(request, response);
                break;
            case "orders-cancel":
                flashAndRedirect(request, response, adminService.cancelOrder(request.getParameter("id")), "order-detail&id=" + url(request.getParameter("id")));
                break;
            case "payments-retry":
                flashAndRedirect(request, response, adminService.retryPayment(request.getParameter("id")), "payments");
                break;
            case "vouchers-save":
                handleVoucherSave(request, response);
                break;
            case "vouchers-delete":
                flashAndRedirect(request, response, adminService.deleteVoucher(request.getParameter("id")), "vouchers");
                break;
            case "reviews-delete":
                flashAndRedirect(request, response, adminService.deleteReview(request.getParameter("id")), "reviews");
                break;
            case "reviews-moderate":
                handleReviewModerate(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin");
                break;
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin dashboard | LinhNamStore");
        request.setAttribute("dashboard", adminService.getDashboardStats());
        forward(request, response, "/views/admin/dashboard.jsp");
    }

    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin users | LinhNamStore");
        request.setAttribute("usersPage", adminService.getUsers(
                request.getParameter("search"),
                request.getParameter("roleFilter"),
                request.getParameter("statusFilter"),
                AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("search", value(request.getParameter("search")));
        request.setAttribute("roleFilter", value(request.getParameter("roleFilter")));
        request.setAttribute("statusFilter", value(request.getParameter("statusFilter")));
        forward(request, response, "/views/admin/users.jsp");
    }

    private void showUserForm(HttpServletRequest request, HttpServletResponse response, boolean edit)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", edit ? "Edit user | LinhNamStore" : "Create user | LinhNamStore");
        request.setAttribute("user", edit ? adminService.getUser(request.getParameter("id")) : null);
        request.setAttribute("editMode", edit);
        forward(request, response, "/views/admin/users-form.jsp");
    }

    private void showProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin products | LinhNamStore");
        request.setAttribute("productsPage", adminService.getProducts(
                request.getParameter("search"),
                request.getParameter("categoryFilter"),
                request.getParameter("statusFilter"),
                request.getParameter("brandFilter"),
                AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("brands", adminService.getBrands());
        request.setAttribute("search", value(request.getParameter("search")));
        request.setAttribute("categoryFilter", value(request.getParameter("categoryFilter")));
        request.setAttribute("statusFilter", value(request.getParameter("statusFilter")));
        request.setAttribute("brandFilter", value(request.getParameter("brandFilter")));
        forward(request, response, "/views/admin/products.jsp");
    }

    private void showProductForm(HttpServletRequest request, HttpServletResponse response, boolean edit)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", edit ? "Edit product | LinhNamStore" : "Create product | LinhNamStore");
        request.setAttribute("product", edit ? adminService.getProduct(request.getParameter("id")) : null);
        request.setAttribute("brands", adminService.getBrands());
        request.setAttribute("editMode", edit);
        forward(request, response, "/views/admin/products-form.jsp");
    }

    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin orders | LinhNamStore");
        request.setAttribute("ordersPage", adminService.getOrders(
                request.getParameter("search"),
                request.getParameter("statusFilter"),
                AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("search", value(request.getParameter("search")));
        request.setAttribute("statusFilter", value(request.getParameter("statusFilter")));
        forward(request, response, "/views/admin/orders.jsp");
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Order detail | LinhNamStore");
        request.setAttribute("order", adminService.getOrder(request.getParameter("id")));
        forward(request, response, "/views/admin/order-detail.jsp");
    }

    private void showPayments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin payments | LinhNamStore");
        request.setAttribute("paymentsPage", adminService.getPayments(
                request.getParameter("search"),
                request.getParameter("statusFilter"),
                AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("search", value(request.getParameter("search")));
        request.setAttribute("statusFilter", value(request.getParameter("statusFilter")));
        forward(request, response, "/views/admin/payments.jsp");
    }

    private void showVouchers(HttpServletRequest request, HttpServletResponse response, String action)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin vouchers | LinhNamStore");
        request.setAttribute("vouchersPage", adminService.getVouchers(AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("users", adminService.getAllUsers());
        if ("vouchers-edit".equals(action)) {
            request.setAttribute("voucher", adminService.getVoucher(request.getParameter("id")));
        }
        request.setAttribute("editMode", "vouchers-edit".equals(action));
        forward(request, response, "/views/admin/vouchers.jsp");
    }

    private void showReviews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Admin reviews | LinhNamStore");
        request.setAttribute("reviewTableAvailable", adminService.isReviewTableAvailable());
        request.setAttribute("reviewsPage", adminService.getReviews(request.getParameter("search"), AdminService.safePage(request.getParameter("page"))));
        request.setAttribute("search", value(request.getParameter("search")));
        forward(request, response, "/views/admin/reviews.jsp");
    }

    private void handleUserSave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdminUserRequestDto dto = new AdminUserRequestDto();
        dto.setId(value(request.getParameter("id")));
        dto.setName(value(request.getParameter("name")));
        dto.setEmail(value(request.getParameter("email")));
        dto.setRole(value(request.getParameter("role")));
        dto.setStatus(value(request.getParameter("status")));
        dto.setDateOfBirth(AdminService.parseDate(request.getParameter("dateOfBirth")));
        flashAndRedirect(request, response, adminService.saveUser(dto), "users");
    }

    private void handleProductSave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdminProductRequestDto dto = new AdminProductRequestDto();
        dto.setId(value(request.getParameter("id")));
        dto.setName(value(request.getParameter("name")));
        dto.setDescription(value(request.getParameter("description")));
        dto.setBrandId(value(request.getParameter("brandId")));
        dto.setStatus(value(request.getParameter("status")));
        dto.setCategory(value(request.getParameter("category")));
        HttpSession session = request.getSession(false);
        Object authUserId = session == null ? null : session.getAttribute("authUserId");
        dto.setUserId(authUserId == null ? "" : value(String.valueOf(authUserId)));
        flashAndRedirect(request, response, adminService.saveProduct(dto), "products");
    }

    private void handleVariantSave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdminProductVariantRequestDto dto = new AdminProductVariantRequestDto();
        dto.setId(value(request.getParameter("variantId")));
        dto.setProductId(value(request.getParameter("productId")));
        dto.setPrice(AdminService.parseBigDecimal(request.getParameter("price")));
        dto.setImageUrl(value(request.getParameter("imageUrl")));
        dto.setSku(value(request.getParameter("sku")));
        dto.setQuantity(AdminService.parseInt(request.getParameter("quantity"), 0));
        dto.setStatus(value(request.getParameter("variantStatus")));
        flashAndRedirect(request, response, adminService.saveVariant(dto), "products-edit&id=" + url(dto.getProductId()));
    }

    private void handleVariantDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = value(request.getParameter("productId"));
        flashAndRedirect(request, response, adminService.deleteVariant(request.getParameter("variantId")), "products-edit&id=" + url(productId));
    }

    private void handleOrderStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdminOrderRequestDto dto = new AdminOrderRequestDto();
        dto.setId(value(request.getParameter("id")));
        dto.setStatus(value(request.getParameter("status")));
        flashAndRedirect(request, response, adminService.updateOrderStatus(dto), "order-detail&id=" + url(dto.getId()));
    }

    private void handleVoucherSave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AdminVoucherRequestDto dto = new AdminVoucherRequestDto();
        dto.setId(value(request.getParameter("id")));
        dto.setUserId(value(request.getParameter("userId")));
        dto.setPercent(AdminService.parseBigDecimal(request.getParameter("percent")).doubleValue());
        dto.setExpTime(AdminService.parseDate(request.getParameter("expTime")));
        dto.setQuantity(AdminService.parseInt(request.getParameter("quantity"), 0));
        flashAndRedirect(request, response, adminService.saveVoucher(dto), "vouchers");
    }

    private void handleReviewModerate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = value(request.getParameter("id"));
        int rating = AdminService.parseInt(request.getParameter("rating"), 5);
        String comment = value(request.getParameter("comment"));
        flashAndRedirect(request, response, adminService.moderateReview(id, rating, comment), "reviews");
    }

    private boolean ensureAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object sessionRoleValue = session == null ? null : session.getAttribute("authUserRole");
        Object requestRoleValue = request.getAttribute("authUserRole");
        String sessionRole = sessionRoleValue == null ? "" : value(String.valueOf(sessionRoleValue));
        String requestRole = requestRoleValue == null ? "" : value(String.valueOf(requestRoleValue));
        if (AdminService.isAdminRole(sessionRole) || AdminService.isAdminRole(requestRole)) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/auth?action=signin");
        return false;
    }

    private void flashAndRedirect(HttpServletRequest request, HttpServletResponse response, AdminMutationResult result, String action)
            throws IOException {
        HttpSession session = request.getSession(true);
        if (result.isSuccess()) {
            session.setAttribute("flashSuccess", result.getMessage());
        } else {
            session.setAttribute("flashError", result.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin?action=" + action);
    }

    private void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        request.setAttribute("success", session.getAttribute("flashSuccess"));
        request.setAttribute("error", session.getAttribute("flashError"));
        session.removeAttribute("flashSuccess");
        session.removeAttribute("flashError");
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String url(String input) {
        return value(input).replace(" ", "%20");
    }
}
