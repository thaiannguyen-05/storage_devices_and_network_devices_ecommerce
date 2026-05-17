package module.bussiness.admin;

import entity.BrandEntity;
import entity.ProductEntity;
import entity.ProductReviewEntity;
import entity.ProductVariantEntity;
import entity.UserEntity;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import module.bussiness.admin.dto.AdminDashboardStatsDto;
import module.bussiness.admin.dto.AdminOrderRequestDto;
import module.bussiness.admin.dto.AdminProductRequestDto;
import module.bussiness.admin.dto.AdminProductVariantRequestDto;
import module.bussiness.admin.dto.AdminUserRequestDto;
import module.bussiness.admin.dto.AdminVoucherRequestDto;
import module.bussiness.admin.repository.impl.AdminStatsRepository;
import module.bussiness.admin.repository.impl.ProductReviewRepository;
import module.bussiness.admin.response_dto.AdminDashboardResponseDto;
import module.bussiness.admin.response_dto.AdminOrderResponseDto;
import module.bussiness.admin.response_dto.AdminPaymentResponseDto;
import module.bussiness.admin.response_dto.AdminProductResponseDto;
import module.bussiness.admin.response_dto.AdminUserResponseDto;
import module.bussiness.admin.response_dto.AdminVoucherResponseDto;
import module.bussiness.order.repository.impl.OrderRepository;
import module.bussiness.payment.repository.impl.PaymentRepository;
import module.bussiness.product.repository.impl.BrandRepository;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductVariantRepository;
import module.core.sql.ConnecDb;
import module.core.user.dto.UpdateUserDto;
import module.core.user.repository.impl.UserRepository;

public class AdminService {
    private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int PASSWORD_ITERATIONS = 120000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BYTES = 32;
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static final String[] USER_ROLES = {"ADMIN", "USER"};
    public static final String[] USER_STATUSES = {"PENDING", "ACTIVE", "INACTIVE", "BANNED"};
    public static final String[] PRODUCT_STATUSES = {"DRAFT", "ACTIVE", "INACTIVE", "ARCHIVED"};
    public static final String[] PRODUCT_CATEGORIES = {"STORAGE_DEVICE", "NETWORK_DEVICE", "ACCESSORY"};
    public static final String[] VARIANT_STATUSES = {"ACTIVE", "INACTIVE", "OUT_OF_STOCK"};
    public static final String[] ORDER_STATUSES = {"PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED"};
    public static final String[] PAYMENT_STATUSES = {"PENDING", "SUCCESS", "FAILED", "CANCELLED"};

    private final AdminStatsRepository adminStatsRepository = new AdminStatsRepository();
    private final ProductReviewRepository productReviewRepository = new ProductReviewRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final ProductVariantRepository productVariantRepository = new ProductVariantRepository();
    private final BrandRepository brandRepository = new BrandRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminDashboardResponseDto getDashboardStats() {
        AdminDashboardResponseDto response = new AdminDashboardResponseDto();
        AdminDashboardStatsDto stats = adminStatsRepository.getDashboardStats();
        response.setSuccess(true);
        response.setStats(stats);
        return response;
    }

    public AdminPage<AdminUserResponseDto> getUsers(String search, String roleFilter, String statusFilter, int page) {
        String where = " WHERE 1=1 ";
        List<Object> params = new ArrayList<>();
        if (!isBlank(search)) {
            where += " AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?) ";
            String pattern = like(search);
            params.add(pattern);
            params.add(pattern);
        }
        if (contains(USER_ROLES, roleFilter)) {
            where += " AND role = ? ";
            params.add(roleFilter.trim().toUpperCase());
        }
        if (contains(USER_STATUSES, statusFilter)) {
            where += " AND status = ? ";
            params.add(statusFilter.trim().toUpperCase());
        }

        int safePage = Math.max(page, 1);
        int total = count("SELECT COUNT(*) FROM \"User\"" + where, params);
        List<AdminUserResponseDto> users = new ArrayList<>();
        String sql = "SELECT id, name, \"dateOfBirth\", status, role, email, \"createdAt\", \"updatedAt\" FROM \"User\""
                + where + " ORDER BY \"createdAt\" DESC LIMIT ? OFFSET ?";
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(DEFAULT_PAGE_SIZE);
        queryParams.add((safePage - 1) * DEFAULT_PAGE_SIZE);
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, queryParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin users", e);
        }
        return new AdminPage<>(users, safePage, DEFAULT_PAGE_SIZE, total);
    }

    public AdminUserResponseDto getUser(String id) {
        if (isBlank(id)) {
            return null;
        }
        UserEntity user = userRepository.findById(id.trim());
        return user == null ? null : mapUser(user);
    }

    public AdminMutationResult saveUser(AdminUserRequestDto dto) {
        if (dto == null || isBlank(dto.getName()) || isBlank(dto.getEmail())) {
            return AdminMutationResult.fail("Name and email are required.");
        }
        if (isBlank(dto.getId())) {
            return createUser(dto);
        }

        UpdateUserDto update = new UpdateUserDto();
        update.setName(dto.getName().trim());
        update.setEmail(dto.getEmail().trim().toLowerCase());
        update.setDateOfBirth(dto.getDateOfBirth() == null ? LocalDate.now() : dto.getDateOfBirth());
        update.setRole(allowedStatus(dto.getRole(), USER_ROLES, "USER"));
        update.setStatus(allowedStatus(dto.getStatus(), USER_STATUSES, "PENDING"));
        boolean success = userRepository.update(dto.getId().trim(), update);
        return success ? AdminMutationResult.ok("User saved.") : AdminMutationResult.fail("User was not updated.");
    }

    public AdminMutationResult setUserStatus(String id, String status) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("User id is required.");
        }
        String allowed = allowedStatus(status, USER_STATUSES, "ACTIVE");
        String sql = "UPDATE \"User\" SET status = ?, \"updatedAt\" = NOW() WHERE id = ?";
        return executeUpdate(sql, List.of(allowed, id.trim()), "User status updated.", "User status was not updated.");
    }

    public AdminMutationResult deleteUser(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("User id is required.");
        }
        return userRepository.delete(id.trim())
                ? AdminMutationResult.ok("User deleted.")
                : AdminMutationResult.fail("User was not deleted.");
    }

    public AdminPage<AdminProductResponseDto> getProducts(String search, String categoryFilter, String statusFilter, String brandFilter, int page) {
        String where = " WHERE 1=1 ";
        List<Object> params = new ArrayList<>();
        if (!isBlank(search)) {
            where += " AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ";
            String pattern = like(search);
            params.add(pattern);
            params.add(pattern);
        }
        if (contains(PRODUCT_CATEGORIES, categoryFilter)) {
            where += " AND p.category = ? ";
            params.add(categoryFilter.trim().toUpperCase());
        }
        if (contains(PRODUCT_STATUSES, statusFilter)) {
            where += " AND p.status = ? ";
            params.add(statusFilter.trim().toUpperCase());
        }
        if (!isBlank(brandFilter)) {
            where += " AND p.\"brandId\" = ? ";
            params.add(brandFilter.trim());
        }

        int safePage = Math.max(page, 1);
        int total = count("SELECT COUNT(*) FROM \"Product\" p" + where, params);
        List<AdminProductResponseDto> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.\"brandId\", b.name AS brandName, p.status, p.category, "
                + "COUNT(pv.id) AS variantCount, COALESCE(SUM(pv.quantity), 0) AS totalStock "
                + "FROM \"Product\" p LEFT JOIN \"Brand\" b ON b.id = p.\"brandId\" "
                + "LEFT JOIN \"ProductVariant\" pv ON pv.\"productId\" = p.id "
                + where
                + " GROUP BY p.id, p.name, p.description, p.\"brandId\", b.name, p.status, p.category "
                + "ORDER BY p.name ASC LIMIT ? OFFSET ?";
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(DEFAULT_PAGE_SIZE);
        queryParams.add((safePage - 1) * DEFAULT_PAGE_SIZE);
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, queryParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin products", e);
        }
        return new AdminPage<>(products, safePage, DEFAULT_PAGE_SIZE, total);
    }

    public AdminProductResponseDto getProduct(String id) {
        if (isBlank(id)) {
            return null;
        }
        ProductEntity product = productRepository.findById(id.trim());
        if (product == null) {
            return null;
        }
        AdminProductResponseDto response = mapProduct(product);
        response.getVariants().addAll(productVariantRepository.findByProductId(product.getId()));
        response.setVariantCount(response.getVariants().size());
        int totalStock = 0;
        for (ProductVariantEntity variant : response.getVariants()) {
            totalStock += variant.getQuantity() == null ? 0 : variant.getQuantity();
        }
        response.setTotalStock(totalStock);
        return response;
    }

    public AdminMutationResult saveProduct(AdminProductRequestDto dto) {
        if (dto == null || isBlank(dto.getName()) || isBlank(dto.getBrandId())) {
            return AdminMutationResult.fail("Product name and brand are required.");
        }
        if (isBlank(dto.getId())) {
            String sql = "INSERT INTO \"Product\" (id, name, description, \"brandId\", status, \"userId\", \"createdAt\", \"updatedAt\", category) "
                    + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)";
            List<Object> params = List.of(
                    UUID.randomUUID().toString(),
                    dto.getName().trim(),
                    value(dto.getDescription()),
                    dto.getBrandId().trim(),
                    allowedStatus(dto.getStatus(), PRODUCT_STATUSES, "DRAFT"),
                    isBlank(dto.getUserId()) ? nullToAdminUserId() : dto.getUserId().trim(),
                    allowedStatus(dto.getCategory(), PRODUCT_CATEGORIES, "STORAGE_DEVICE")
            );
            return executeUpdate(sql, params, "Product created.", "Product was not created.");
        }

        String sql = "UPDATE \"Product\" SET name = ?, description = ?, \"brandId\" = ?, status = ?, category = ?, \"updatedAt\" = NOW() WHERE id = ?";
        List<Object> params = List.of(
                dto.getName().trim(),
                value(dto.getDescription()),
                dto.getBrandId().trim(),
                allowedStatus(dto.getStatus(), PRODUCT_STATUSES, "DRAFT"),
                allowedStatus(dto.getCategory(), PRODUCT_CATEGORIES, "STORAGE_DEVICE"),
                dto.getId().trim()
        );
        return executeUpdate(sql, params, "Product saved.", "Product was not updated.");
    }

    public AdminMutationResult deleteProduct(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Product id is required.");
        }
        return productRepository.delete(id.trim())
                ? AdminMutationResult.ok("Product deleted.")
                : AdminMutationResult.fail("Product was not deleted.");
    }

    public AdminMutationResult changeProductStatus(String id, String status) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Product id is required.");
        }
        String sql = "UPDATE \"Product\" SET status = ?, \"updatedAt\" = NOW() WHERE id = ?";
        return executeUpdate(sql, List.of(allowedStatus(status, PRODUCT_STATUSES, "DRAFT"), id.trim()),
                "Product status updated.", "Product status was not updated.");
    }

    public AdminMutationResult saveVariant(AdminProductVariantRequestDto dto) {
        if (dto == null || isBlank(dto.getProductId()) || isBlank(dto.getSku())) {
            return AdminMutationResult.fail("Variant product and SKU are required.");
        }
        BigDecimal price = dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice();
        int quantity = dto.getQuantity() == null ? 0 : Math.max(dto.getQuantity(), 0);
        String status = allowedStatus(dto.getStatus(), VARIANT_STATUSES, "ACTIVE");
        if (isBlank(dto.getId())) {
            String sql = "INSERT INTO \"ProductVariant\" (id, \"productId\", price, \"imageUrl\", status, \"createdAt\", \"updatedAt\", sku, quantity) "
                    + "VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";
            return executeUpdate(sql, List.of(UUID.randomUUID().toString(), dto.getProductId().trim(), price,
                    value(dto.getImageUrl()), status, dto.getSku().trim(), quantity),
                    "Variant created.", "Variant was not created.");
        }
        String sql = "UPDATE \"ProductVariant\" SET price = ?, \"imageUrl\" = ?, status = ?, sku = ?, quantity = ?, \"updatedAt\" = NOW() WHERE id = ?";
        return executeUpdate(sql, List.of(price, value(dto.getImageUrl()), status, dto.getSku().trim(), quantity, dto.getId().trim()),
                "Variant saved.", "Variant was not updated.");
    }

    public AdminMutationResult deleteVariant(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Variant id is required.");
        }
        return executeUpdate("DELETE FROM \"ProductVariant\" WHERE id = ?", List.of(id.trim()),
                "Variant deleted.", "Variant was not deleted.");
    }

    public AdminPage<AdminOrderResponseDto> getOrders(String search, String statusFilter, int page) {
        String where = orderWhere(search, statusFilter, false);
        List<Object> params = orderParams(search, statusFilter);
        int safePage = Math.max(page, 1);
        int total = count("SELECT COUNT(*) FROM \"Order\" o LEFT JOIN \"User\" u ON u.id = o.\"userId\" "
                + "LEFT JOIN \"Product\" p ON p.id = o.\"productId\" " + where, params);
        List<AdminOrderResponseDto> orders = loadOrders(where, params, safePage, DEFAULT_PAGE_SIZE, null);
        return new AdminPage<>(orders, safePage, DEFAULT_PAGE_SIZE, total);
    }

    public AdminOrderResponseDto getOrder(String id) {
        if (isBlank(id)) {
            return null;
        }
        List<AdminOrderResponseDto> orders = loadOrders(" WHERE o.id = ? ", List.of(id.trim()), 1, 1, id.trim());
        if (orders.isEmpty()) {
            return null;
        }
        AdminOrderResponseDto order = orders.get(0);
        order.getPayments().addAll(getPaymentsByOrderId(order.getId()));
        return order;
    }

    public AdminMutationResult updateOrderStatus(AdminOrderRequestDto dto) {
        if (dto == null || isBlank(dto.getId())) {
            return AdminMutationResult.fail("Order id is required.");
        }
        String status = allowedStatus(dto.getStatus(), ORDER_STATUSES, "PENDING");
        try {
            orderRepository.updateStatusByOrderId(dto.getId().trim(), status);
            return AdminMutationResult.ok("Order status updated.");
        } catch (RuntimeException e) {
            return AdminMutationResult.fail(e.getMessage());
        }
    }

    public AdminMutationResult cancelOrder(String id) {
        AdminOrderRequestDto dto = new AdminOrderRequestDto();
        dto.setId(id);
        dto.setStatus("CANCELLED");
        return updateOrderStatus(dto);
    }

    public AdminPage<AdminPaymentResponseDto> getPayments(String search, String statusFilter, int page) {
        String where = " WHERE 1=1 ";
        List<Object> params = new ArrayList<>();
        if (!isBlank(search)) {
            where += " AND (LOWER(pay.id) LIKE ? OR LOWER(pay.\"orderId\") LIKE ? OR LOWER(pay.\"userId\") LIKE ? OR LOWER(u.name) LIKE ?) ";
            String pattern = like(search);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (contains(PAYMENT_STATUSES, statusFilter)) {
            where += " AND pay.status = ? ";
            params.add(statusFilter.trim().toUpperCase());
        }
        int safePage = Math.max(page, 1);
        int total = count("SELECT COUNT(*) FROM \"Payment\" pay LEFT JOIN \"User\" u ON u.id = pay.\"userId\" " + where, params);
        List<AdminPaymentResponseDto> payments = new ArrayList<>();
        String sql = "SELECT pay.*, u.name AS userName FROM \"Payment\" pay LEFT JOIN \"User\" u ON u.id = pay.\"userId\" "
                + where + " ORDER BY pay.\"createdAt\" DESC LIMIT ? OFFSET ?";
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(DEFAULT_PAGE_SIZE);
        queryParams.add((safePage - 1) * DEFAULT_PAGE_SIZE);
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, queryParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin payments", e);
        }
        return new AdminPage<>(payments, safePage, DEFAULT_PAGE_SIZE, total);
    }

    public AdminMutationResult retryPayment(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Payment id is required.");
        }
        String sql = "UPDATE \"Payment\" SET status = 'PENDING', \"updatedAt\" = NOW() WHERE id = ? AND status = 'FAILED'";
        return executeUpdate(sql, List.of(id.trim()), "Payment marked pending for retry.", "Only failed payments can be retried.");
    }

    public AdminPage<AdminVoucherResponseDto> getVouchers(int page) {
        int safePage = Math.max(page, 1);
        int total = count("SELECT COUNT(*) FROM \"Voucher\"", List.of());
        List<AdminVoucherResponseDto> vouchers = new ArrayList<>();
        String sql = "SELECT v.*, u.name AS userName, u.email AS userEmail FROM \"Voucher\" v "
                + "LEFT JOIN \"User\" u ON u.id = v.\"userId\" ORDER BY v.\"createdAt\" DESC LIMIT ? OFFSET ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, DEFAULT_PAGE_SIZE);
            ps.setInt(2, (safePage - 1) * DEFAULT_PAGE_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vouchers.add(mapVoucher(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin vouchers", e);
        }
        return new AdminPage<>(vouchers, safePage, DEFAULT_PAGE_SIZE, total);
    }

    public AdminVoucherResponseDto getVoucher(String id) {
        if (isBlank(id)) {
            return null;
        }
        String sql = "SELECT v.*, u.name AS userName, u.email AS userEmail FROM \"Voucher\" v "
                + "LEFT JOIN \"User\" u ON u.id = v.\"userId\" WHERE v.id = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVoucher(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin voucher", e);
        }
        return null;
    }

    public AdminMutationResult saveVoucher(AdminVoucherRequestDto dto) {
        if (dto == null || isBlank(dto.getUserId()) || dto.getPercent() == null || dto.getExpTime() == null) {
            return AdminMutationResult.fail("Voucher user, percent, and expiry are required.");
        }
        int quantity = dto.getQuantity() == null ? 0 : Math.max(dto.getQuantity(), 0);
        if (isBlank(dto.getId())) {
            String sql = "INSERT INTO \"Voucher\" (id, percent, \"userId\", \"expTime\", \"createdAt\", quantity) VALUES (?, ?, ?, ?, NOW(), ?)";
            return executeUpdate(sql, List.of(UUID.randomUUID().toString(), dto.getPercent(), dto.getUserId().trim(),
                    Date.valueOf(dto.getExpTime()), quantity), "Voucher created.", "Voucher was not created.");
        }
        String sql = "UPDATE \"Voucher\" SET percent = ?, \"userId\" = ?, \"expTime\" = ?, quantity = ? WHERE id = ?";
        return executeUpdate(sql, List.of(dto.getPercent(), dto.getUserId().trim(), Date.valueOf(dto.getExpTime()), quantity, dto.getId().trim()),
                "Voucher saved.", "Voucher was not updated.");
    }

    public AdminMutationResult deleteVoucher(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Voucher id is required.");
        }
        return executeUpdate("DELETE FROM \"Voucher\" WHERE id = ?", List.of(id.trim()),
                "Voucher deleted.", "Voucher was not deleted.");
    }

    public AdminPage<ProductReviewEntity> getReviews(String search, int page) {
        int safePage = Math.max(page, 1);
        int total = productReviewRepository.count(search);
        return new AdminPage<>(productReviewRepository.findAll(search, safePage, DEFAULT_PAGE_SIZE), safePage, DEFAULT_PAGE_SIZE, total);
    }

    public boolean isReviewTableAvailable() {
        return productReviewRepository.isAvailable();
    }

    public AdminMutationResult deleteReview(String id) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Review id is required.");
        }
        return productReviewRepository.delete(id.trim())
                ? AdminMutationResult.ok("Review deleted.")
                : AdminMutationResult.fail("Review was not deleted.");
    }

    public AdminMutationResult moderateReview(String id, int rating, String comment) {
        if (isBlank(id)) {
            return AdminMutationResult.fail("Review id is required.");
        }
        return productReviewRepository.update(id.trim(), rating, comment)
                ? AdminMutationResult.ok("Review updated.")
                : AdminMutationResult.fail("Review was not updated.");
    }

    public List<BrandEntity> getBrands() {
        return brandRepository.findAll();
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public static boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(value(role));
    }

    public static int safePage(String rawPage) {
        try {
            return Math.max(1, Integer.parseInt(value(rawPage)));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static int safePageSize(int requestedPageSize) {
        return requestedPageSize <= 0 ? DEFAULT_PAGE_SIZE : requestedPageSize;
    }

    public static String allowedStatus(String status, String[] allowed, String fallback) {
        String normalized = value(status).toUpperCase();
        for (String item : allowed) {
            if (item.equalsIgnoreCase(normalized)) {
                return item;
            }
        }
        return fallback;
    }

    public static String escapeHtml(Object input) {
        String value = input == null ? "" : String.valueOf(input);
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String value(String input) {
        return input == null ? "" : input.trim();
    }

    public static LocalDate parseDate(String raw) {
        try {
            return isBlank(raw) ? null : LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal parseBigDecimal(String raw) {
        try {
            return isBlank(raw) ? BigDecimal.ZERO : new BigDecimal(raw.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public static int parseInt(String raw, int fallback) {
        try {
            return isBlank(raw) ? fallback : Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private AdminMutationResult createUser(AdminUserRequestDto dto) {
        String sql = "INSERT INTO \"User\" (id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        List<Object> params = List.of(
                UUID.randomUUID().toString(),
                dto.getName().trim(),
                Date.valueOf(dto.getDateOfBirth() == null ? LocalDate.now() : dto.getDateOfBirth()),
                temporaryPasswordHash(),
                allowedStatus(dto.getStatus(), USER_STATUSES, "PENDING"),
                allowedStatus(dto.getRole(), USER_ROLES, "USER"),
                dto.getEmail().trim().toLowerCase()
        );
        return executeUpdate(sql, params, "User created. Ask the user to reset password before sign-in.", "User was not created.");
    }

    private List<AdminOrderResponseDto> loadOrders(String where, List<Object> params, int page, int pageSize, String targetId) {
        try {
            return loadRichOrders(where, params, page, pageSize);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Admin rich order query failed; using schema-limited fallback.", e);
            return loadBasicOrders(where, params, page, pageSize, targetId);
        }
    }

    private List<AdminOrderResponseDto> loadRichOrders(String where, List<Object> params, int page, int pageSize) {
        String sql = "SELECT o.id, o.\"userId\", u.name AS userName, u.email AS userEmail, o.\"productId\", p.name AS productName, "
                + "o.\"variantId\", pv.sku, COALESCE(o.quantity, 1) AS quantity, COALESCE(pv.price * o.quantity, 0) AS total, "
                + "o.status, o.phone, o.address, o.\"createdAt\", o.\"updatedAt\" "
                + "FROM \"Order\" o LEFT JOIN \"User\" u ON u.id = o.\"userId\" "
                + "LEFT JOIN \"Product\" p ON p.id = o.\"productId\" "
                + "LEFT JOIN \"ProductVariant\" pv ON pv.id = o.\"variantId\" "
                + where + " ORDER BY o.\"createdAt\" DESC LIMIT ? OFFSET ?";
        return queryOrders(sql, params, page, pageSize, false);
    }

    private List<AdminOrderResponseDto> loadBasicOrders(String where, List<Object> params, int page, int pageSize, String targetId) {
        String effectiveWhere = where;
        List<Object> effectiveParams = params;
        if (!isBlank(targetId)) {
            effectiveWhere = " WHERE o.id = ? ";
            effectiveParams = List.of(targetId);
        }
        String sql = "SELECT o.id, o.\"userId\", u.name AS userName, u.email AS userEmail, o.\"productId\", p.name AS productName, "
                + "NULL AS \"variantId\", NULL AS sku, 1 AS quantity, 0 AS total, o.status, NULL AS phone, NULL AS address, "
                + "o.\"createdAt\", NULL AS \"updatedAt\" "
                + "FROM \"Order\" o LEFT JOIN \"User\" u ON u.id = o.\"userId\" "
                + "LEFT JOIN \"Product\" p ON p.id = o.\"productId\" "
                + effectiveWhere + " ORDER BY o.\"createdAt\" DESC LIMIT ? OFFSET ?";
        return queryOrders(sql, effectiveParams, page, pageSize, true);
    }

    private List<AdminOrderResponseDto> queryOrders(String sql, List<Object> params, int page, int pageSize, boolean schemaLimited) {
        List<AdminOrderResponseDto> orders = new ArrayList<>();
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageSize);
        queryParams.add((Math.max(page, 1) - 1) * pageSize);
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, queryParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminOrderResponseDto order = mapOrder(rs);
                    order.setSchemaLimited(schemaLimited);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders", e);
        }
        return orders;
    }

    private String orderWhere(String search, String statusFilter, boolean rich) {
        String where = " WHERE 1=1 ";
        if (!isBlank(search)) {
            where += " AND (LOWER(o.id) LIKE ? OR LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(p.name) LIKE ?) ";
        }
        if (contains(ORDER_STATUSES, statusFilter)) {
            where += " AND o.status = ? ";
        }
        return where;
    }

    private List<Object> orderParams(String search, String statusFilter) {
        List<Object> params = new ArrayList<>();
        if (!isBlank(search)) {
            String pattern = like(search);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (contains(ORDER_STATUSES, statusFilter)) {
            params.add(statusFilter.trim().toUpperCase());
        }
        return params;
    }

    private List<AdminPaymentResponseDto> getPaymentsByOrderId(String orderId) {
        List<AdminPaymentResponseDto> payments = new ArrayList<>();
        String sql = "SELECT pay.*, u.name AS userName FROM \"Payment\" pay LEFT JOIN \"User\" u ON u.id = pay.\"userId\" WHERE pay.\"orderId\" = ? ORDER BY pay.\"createdAt\" DESC";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load order payments for admin.", e);
        }
        return payments;
    }

    private AdminUserResponseDto mapUser(ResultSet rs) throws SQLException {
        AdminUserResponseDto user = new AdminUserResponseDto();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        Date dob = rs.getDate("dateOfBirth");
        user.setDateOfBirth(dob == null ? null : dob.toLocalDate());
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        user.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return user;
    }

    private AdminUserResponseDto mapUser(UserEntity entity) {
        AdminUserResponseDto user = new AdminUserResponseDto();
        user.setId(entity.getId());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setRole(entity.getRole());
        user.setStatus(entity.getStatus());
        user.setDateOfBirth(entity.getDateOfBirth());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        return user;
    }

    private AdminProductResponseDto mapProduct(ResultSet rs, boolean withVariants) throws SQLException {
        AdminProductResponseDto product = new AdminProductResponseDto();
        product.setId(rs.getString("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setBrandId(rs.getString("brandId"));
        product.setBrandName(rs.getString("brandName"));
        product.setStatus(rs.getString("status"));
        product.setCategory(rs.getString("category"));
        product.setVariantCount(rs.getInt("variantCount"));
        product.setTotalStock(rs.getInt("totalStock"));
        if (withVariants) {
            product.getVariants().addAll(productVariantRepository.findByProductId(product.getId()));
        }
        return product;
    }

    private AdminProductResponseDto mapProduct(ProductEntity entity) {
        AdminProductResponseDto product = new AdminProductResponseDto();
        product.setId(entity.getId());
        product.setName(entity.getName());
        product.setDescription(entity.getDescription());
        product.setBrandId(entity.getBrandId());
        product.setStatus(entity.getStatus());
        product.setCategory(entity.getCategory());
        BrandEntity brand = isBlank(entity.getBrandId()) ? null : brandRepository.findById(entity.getBrandId());
        product.setBrandName(brand == null ? entity.getBrandId() : brand.getName());
        return product;
    }

    private AdminOrderResponseDto mapOrder(ResultSet rs) throws SQLException {
        AdminOrderResponseDto order = new AdminOrderResponseDto();
        order.setId(rs.getString("id"));
        order.setUserId(rs.getString("userId"));
        order.setUserName(rs.getString("userName"));
        order.setUserEmail(rs.getString("userEmail"));
        order.setProductId(rs.getString("productId"));
        order.setProductName(rs.getString("productName"));
        order.setVariantId(rs.getString("variantId"));
        order.setSku(rs.getString("sku"));
        order.setQuantity(rs.getInt("quantity"));
        order.setTotal(rs.getBigDecimal("total"));
        order.setStatus(rs.getString("status"));
        order.setPhone(rs.getString("phone"));
        order.setAddress(rs.getString("address"));
        order.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        order.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return order;
    }

    private AdminPaymentResponseDto mapPayment(ResultSet rs) throws SQLException {
        AdminPaymentResponseDto payment = new AdminPaymentResponseDto();
        payment.setId(rs.getString("id"));
        payment.setOrderId(rs.getString("orderId"));
        payment.setUserId(rs.getString("userId"));
        payment.setUserName(rs.getString("userName"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setMethod(rs.getString("partnerCode"));
        payment.setStatus(rs.getString("status"));
        payment.setRedirectUrl(rs.getString("redirectUrl"));
        payment.setSignature(rs.getString("signature"));
        payment.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        payment.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
        return payment;
    }

    private AdminVoucherResponseDto mapVoucher(ResultSet rs) throws SQLException {
        AdminVoucherResponseDto voucher = new AdminVoucherResponseDto();
        voucher.setId(rs.getString("id"));
        voucher.setPercent(rs.getDouble("percent"));
        voucher.setUserId(rs.getString("userId"));
        voucher.setUserName(rs.getString("userName"));
        voucher.setUserEmail(rs.getString("userEmail"));
        Date exp = rs.getDate("expTime");
        voucher.setExpTime(exp == null ? null : exp.toLocalDate());
        voucher.setCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
        voucher.setQuantity(rs.getInt("quantity"));
        return voucher;
    }

    private AdminMutationResult executeUpdate(String sql, List<Object> params, String successMessage, String emptyMessage) {
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate() > 0 ? AdminMutationResult.ok(successMessage) : AdminMutationResult.fail(emptyMessage);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, emptyMessage, e);
            return AdminMutationResult.fail(e.getMessage());
        }
    }

    private int count(String sql, List<Object> params) {
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Admin count query failed.", e);
        }
        return 0;
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else if (value instanceof Double) {
                ps.setDouble(i + 1, (Double) value);
            } else if (value instanceof Date) {
                ps.setDate(i + 1, (Date) value);
            } else {
                ps.setString(i + 1, value == null ? "" : String.valueOf(value));
            }
        }
    }

    private String nullToAdminUserId() {
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            if (isAdminRole(user.getRole())) {
                return user.getId();
            }
        }
        return users.isEmpty() ? "" : users.get(0).getId();
    }

    private String temporaryPasswordHash() {
        char[] password = UUID.randomUUID().toString().toCharArray();
        try {
            byte[] salt = new byte[PASSWORD_SALT_BYTES];
            secureRandom.nextBytes(salt);
            byte[] derived = derivePassword(password, salt, PASSWORD_ITERATIONS, PASSWORD_HASH_BYTES);
            return "pbkdf2:" + PASSWORD_ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(derived);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private byte[] derivePassword(char[] password, byte[] salt, int iterations, int keyLengthBytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBytes * 8);
            try {
                return SecretKeyFactory.getInstance(PASSWORD_ALGORITHM).generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to derive password hash.", e);
        }
    }

    private static boolean contains(String[] allowed, String value) {
        if (isBlank(value)) {
            return false;
        }
        for (String item : allowed) {
            if (item.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    private static String like(String input) {
        return "%" + value(input).toLowerCase() + "%";
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public static class AdminMutationResult {
        private final boolean success;
        private final String message;

        private AdminMutationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static AdminMutationResult ok(String message) {
            return new AdminMutationResult(true, message);
        }

        public static AdminMutationResult fail(String message) {
            return new AdminMutationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class AdminPage<T> {
        private final List<T> items;
        private final int page;
        private final int pageSize;
        private final int totalItems;
        private final int totalPages;

        public AdminPage(List<T> items, int page, int pageSize, int totalItems) {
            this.items = items == null ? new ArrayList<>() : items;
            this.page = Math.max(page, 1);
            this.pageSize = Math.max(pageSize, 1);
            this.totalItems = Math.max(totalItems, 0);
            this.totalPages = Math.max(1, (int) Math.ceil((double) this.totalItems / this.pageSize));
        }

        public List<T> getItems() {
            return items;
        }

        public int getPage() {
            return page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean hasPrevious() {
            return page > 1;
        }

        public boolean hasNext() {
            return page < totalPages;
        }
    }
}
