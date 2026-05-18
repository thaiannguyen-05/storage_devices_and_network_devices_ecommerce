package module.bussiness.bugfix;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorefrontBugfixRegressionTest {
    public static void main(String[] args) throws Exception {
        forgotPasswordSuccessShowsResetForm();
        cartAddUsesAtomicUpsert();
        cartBuyNowRedirectsToCheckout();
        productListShowsErrorToasts();
        reviewCreationHandlesInvalidInput();
        orderDeliveryInfoLogsMissingRows();
        contactSubmissionsArePersisted();
        footerDoesNotShowLoginLink();
        leftMenuSubcategoryLinksIncludeCategory();
        leftMenuShowsTopLevelCategoryCounts();
    }

    private static void forgotPasswordSuccessShowsResetForm() throws Exception {
        String auth = read("src/java/module/core/auth/AuthController.java");
        String method = between(auth, "private void handleForgotPassword", "private void handleResetPassword");
        assertContains(method, "request.setAttribute(\"success\", result.getSuccessMessage());",
                "Forgot password success must set success message");
        assertContains(method, "request.getRequestDispatcher(\"/views/auth/reset-password.jsp\").forward(request, response);",
                "Forgot password success must forward to reset-password.jsp");
    }

    private static void cartAddUsesAtomicUpsert() throws Exception {
        String iface = read("src/java/module/bussiness/cart/repository/interfaces/IItemCartRepository.java");
        String repo = read("src/java/module/bussiness/cart/repository/impl/ItemCartRepository.java");
        String controller = read("src/java/module/bussiness/cart/CartController.java");
        String addItem = between(controller, "private void addItem", "private void updateItem");

        assertContains(iface, "upsert(String cartId, String productId, String variantId, int quantity)",
                "Item cart repository interface must expose atomic upsert");
        assertContains(repo, "ON CONFLICT", "Item cart repository upsert must use PostgreSQL ON CONFLICT");
        assertContains(addItem, "itemCartRepository.upsert(", "Cart add must call atomic upsert");
        assertNotContains(addItem, "findByCartIdAndProductAndVariant",
                "Cart add must not use check-then-create for persisted cart rows");
    }

    private static void cartBuyNowRedirectsToCheckout() throws Exception {
        String controller = read("src/java/module/bussiness/cart/CartController.java");
        String list = read("web/views/product/list.jsp");

        assertContains(controller, "case \"buyNow\"", "Cart controller must handle buyNow action");
        assertContains(controller, "/payment?source=buyNow", "buyNow must target payment checkout");
        assertContains(controller, "buyNowRedirectUrl", "buyNow redirect must preserve selected item parameters");
        assertContains(controller, "redirectUrl", "AJAX buyNow response must include redirectUrl");
        assertContains(list, "data.redirectUrl", "Product list JavaScript must follow buyNow redirectUrl");
    }

    private static void productListShowsErrorToasts() throws Exception {
        String list = read("web/views/product/list.jsp");

        assertContains(list, "showCartErrorToast", "Product list must define an error toast");
        assertContains(list, "data.message", "Product list must read JSON error messages");
        assertContains(list, "catch (err)", "Product list must handle cart fetch errors");
    }

    private static void reviewCreationHandlesInvalidInput() throws Exception {
        String service = read("src/java/module/bussiness/product/ProductService.java");
        String method = between(service, "public boolean createReview", "private Map<String, ReviewStats> getReviewStatsMap");

        assertContains(method, "productId == null", "Review creation must validate productId");
        assertContains(method, "comment == null", "Review creation must default null comments");
        assertContains(method, "catch (SQLException", "Review creation must catch SQLException specifically");
        assertContains(method, "return false", "Review creation must return false on invalid input or SQL failure");
    }

    private static void orderDeliveryInfoLogsMissingRows() throws Exception {
        String repo = read("src/java/module/bussiness/order/repository/impl/OrderRepository.java");
        String method = between(repo, "public void updateDeliveryInfo", "public void updateCartQuantity");

        assertContains(repo, "Logger.getLogger(OrderRepository.class.getName())",
                "Order repository must define a logger");
        assertContains(method, "int updated = ps.executeUpdate();",
                "Delivery info update must inspect affected row count");
        assertContains(method, "updated == 0", "Delivery info update must handle missing rows");
        assertContains(method, "catch (SQLException", "Delivery info update must catch SQLException specifically");
    }

    private static void contactSubmissionsArePersisted() throws Exception {
        String schema = read("sto.sql");
        String controller = read("src/java/module/bussiness/contact/ContactController.java");
        String form = read("web/views/contact/index.jsp");

        assertContains(schema, "CREATE TABLE \"Contact\"", "Schema must create Contact table");
        assertContains(schema, "\"status\" VARCHAR(20) NOT NULL DEFAULT 'NEW'",
                "Contact table must track status");
        assertFileExists("src/java/entity/ContactEntity.java", "Contact entity must exist");
        assertFileExists("src/java/module/bussiness/contact/repository/impl/ContactRepository.java",
                "Contact repository must exist");
        assertContains(controller, "contactRepository.save", "Contact controller must save submissions");
        assertContains(form, "method=\"post\"", "Contact form must submit with POST");
    }

    private static void footerDoesNotShowLoginLink() throws Exception {
        String footer = read("web/views/includes/footer.jsp");
        assertNotContains(footer, "\u0110\u0103ng nh\u1eadp", "Footer must not show login link");
    }

    private static void leftMenuSubcategoryLinksIncludeCategory() throws Exception {
        String leftMenu = read("web/views/includes/leftmenu.jsp");

        assertContains(leftMenu, "category=<%= subCategory %>&subcategory=<%= sub %>",
                "Subcategory links must include category and subcategory params");
        assertContains(leftMenu, "{\"HDD\", \"STORAGE_DEVICE\"}", "HDD link must map to storage category");
        assertContains(leftMenu, "{\"SSD\", \"STORAGE_DEVICE\"}", "SSD link must map to storage category");
        assertContains(leftMenu, "{\"NAS\", \"NETWORK_DEVICE\"}", "NAS link must map to network category");
        assertContains(leftMenu, "{\"ROUTER\", \"NETWORK_DEVICE\"}", "Router link must map to network category");
        assertContains(leftMenu, "{\"SWITCH\", \"NETWORK_DEVICE\"}", "Switch link must map to network category");
        assertContains(leftMenu, "{\"CABLE\", \"ACCESSORY\"}", "Cable link must map to accessory category");
        assertContains(leftMenu, "{\"FLASH_DRIVE\", \"ACCESSORY\"}", "Flash drive link must map to accessory category");
        assertContains(leftMenu, "{\"MEMORY_CARD\", \"ACCESSORY\"}", "Memory card link must map to accessory category");
    }

    private static void leftMenuShowsTopLevelCategoryCounts() throws Exception {
        String controller = read("src/java/module/bussiness/product/ProductController.java");
        String leftMenu = read("web/views/includes/leftmenu.jsp");

        assertContains(controller, "categoryCounts.put(\"STORAGE_DEVICE\"", "Controller must count storage devices");
        assertContains(controller, "categoryCounts.put(\"NETWORK_DEVICE\"", "Controller must count network devices");
        assertContains(controller, "categoryCounts.put(\"ACCESSORY\"", "Controller must count accessories");
        assertContains(leftMenu, "categoryCounts", "Left menu must read categoryCounts");
        assertContains(leftMenu, "storageDeviceCount", "Left menu must display storage device count");
        assertContains(leftMenu, "networkDeviceCount", "Left menu must display network device count");
        assertContains(leftMenu, "accessoryCount", "Left menu must display accessory count");
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    private static void assertFileExists(String path, String message) {
        if (!Files.exists(Path.of(path))) {
            throw new AssertionError(message);
        }
    }

    private static String between(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start < 0) {
            throw new AssertionError("Missing start marker: " + startMarker);
        }
        int end = text.indexOf(endMarker, start + startMarker.length());
        if (end < 0) {
            throw new AssertionError("Missing end marker: " + endMarker);
        }
        return text.substring(start, end);
    }

    private static void assertContains(String text, String needle, String message) {
        if (!text.contains(needle)) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotContains(String text, String needle, String message) {
        if (text.contains(needle)) {
            throw new AssertionError(message);
        }
    }
}
