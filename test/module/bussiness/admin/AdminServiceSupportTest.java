package module.bussiness.admin;

public class AdminServiceSupportTest {
    public static void main(String[] args) {
        detectsAdminRoleFromSessionOrRequest();
        clampsPaginationInput();
        escapesHtmlForAdminDisplay();
        validatesAllowedStatusValues();
    }

    private static void detectsAdminRoleFromSessionOrRequest() {
        assertTrue(AdminService.isAdminRole("ADMIN"));
        assertTrue(AdminService.isAdminRole(" admin "));
        assertFalse(AdminService.isAdminRole("USER"));
        assertFalse(AdminService.isAdminRole(null));
    }

    private static void clampsPaginationInput() {
        assertEquals(1, AdminService.safePage("0"));
        assertEquals(1, AdminService.safePage("abc"));
        assertEquals(3, AdminService.safePage("3"));
        assertEquals(20, AdminService.safePageSize(0));
        assertEquals(20, AdminService.safePageSize(-5));
        assertEquals(50, AdminService.safePageSize(50));
    }

    private static void escapesHtmlForAdminDisplay() {
        String escaped = AdminService.escapeHtml("<script>alert(\"x\")</script>&'");
        assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;&amp;&#39;", escaped);
    }

    private static void validatesAllowedStatusValues() {
        assertEquals("PENDING", AdminService.allowedStatus("bad", AdminService.ORDER_STATUSES, "PENDING"));
        assertEquals("COMPLETED", AdminService.allowedStatus("completed", AdminService.ORDER_STATUSES, "PENDING"));
        assertEquals("ACTIVE", AdminService.allowedStatus("ACTIVE", AdminService.USER_STATUSES, "PENDING"));
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
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
}
