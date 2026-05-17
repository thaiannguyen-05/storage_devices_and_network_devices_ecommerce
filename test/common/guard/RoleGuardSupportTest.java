package common.guard;

import common.annotation.Public;
import common.annotation.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import module.bussiness.admin.AdminController;
import module.bussiness.cart.CartController;
import module.bussiness.order.OrderController;
import module.bussiness.payment.PaymentController;
import module.bussiness.product.ProductController;
import module.core.user.UserController;

public class RoleGuardSupportTest {
    public static void main(String[] args) throws Exception {
        assertTrue(RoleGuard.roleMatches("ADMIN", new String[]{"ADMIN"}));
        assertTrue(RoleGuard.roleMatches(" admin ", new String[]{"ADMIN"}));
        assertTrue(RoleGuard.roleMatches("USER", new String[]{"ADMIN", "USER"}));
        assertFalse(RoleGuard.roleMatches("USER", new String[]{"ADMIN"}));
        assertFalse(RoleGuard.roleMatches("", new String[]{"USER"}));

        Claims claims = Jwts.claims()
                .subject("user-1")
                .add("email", "user@example.com")
                .add("role", "ADMIN")
                .add("sessionId", "session-1")
                .build();
        assertEquals("ADMIN", RoleGuard.roleFromClaims(claims));

        Role classRole = RoleGuard.resolveRoleAnnotation(AdminOnlyServlet.class, "GET");
        assertRoles(classRole, "ADMIN");

        Role methodRole = RoleGuard.resolveRoleAnnotation(MixedServlet.class, "POST");
        assertRoles(methodRole, "USER", "ADMIN");

        Role reviewRole = RoleGuard.resolveRoleAnnotation(MixedActionServlet.class, "POST", "/mixed", Map.of("action", "review"));
        assertRoles(reviewRole, "USER", "ADMIN");

        Role defaultAdminRole = RoleGuard.resolveRoleAnnotation(MixedActionServlet.class, "POST", "/mixed", Map.of("action", "create"));
        assertRoles(defaultAdminRole, "ADMIN");

        Role webhookRole = RoleGuard.resolveRoleAnnotation(PaymentController.class, "POST", "/payment/webhook", Map.of());
        assertNull(webhookRole);

        assertTrue(RoleGuard.isPublicEndpoint(MixedServlet.class, "GET"));
        assertFalse(RoleGuard.isPublicEndpoint(MixedServlet.class, "POST"));

        assertRoles(RoleGuard.resolveRoleAnnotation(AdminController.class, "GET"), "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(AdminController.class, "POST"), "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(UserController.class, "GET"), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(OrderController.class, "GET"), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(CartController.class, "POST"), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(CartController.class, "GET"), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(PaymentController.class, "GET", "/payment", Map.of()), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(PaymentController.class, "POST", "/payment", Map.of("actionType", "placeOrder")), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(PaymentController.class, "POST", "/payment", Map.of("actionType", "listPayments")), "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(ProductController.class, "POST", "/product", Map.of("action", "review")), "USER", "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(ProductController.class, "POST", "/product", Map.of("admin", "1")), "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(ProductController.class, "PUT"), "ADMIN");
        assertRoles(RoleGuard.resolveRoleAnnotation(ProductController.class, "DELETE"), "ADMIN");
    }

    @Role("ADMIN")
    private static class AdminOnlyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        }
    }

    @Role("ADMIN")
    private static class MixedServlet extends HttpServlet {
        @Public
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        }

        @Role({"USER", "ADMIN"})
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        }
    }

    private static class MixedActionServlet extends HttpServlet {
        @Role(value = {"USER", "ADMIN"}, actions = "review")
        @Role("ADMIN")
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        }
    }

    private static void assertRoles(Role role, String... expected) {
        if (role == null) {
            throw new AssertionError("Expected role annotation");
        }
        if (!Arrays.equals(expected, role.value())) {
            throw new AssertionError("Expected roles " + Arrays.toString(expected) + " but got " + Arrays.toString(role.value()));
        }
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected true");
        }
    }

    private static void assertFalse(boolean value) {
        if (value) {
            throw new AssertionError("Expected false");
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertNull(Object value) {
        if (value != null) {
            throw new AssertionError("Expected null but got <" + value + ">");
        }
    }
}
