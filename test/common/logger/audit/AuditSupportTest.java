package common.logger.audit;

import java.util.Map;

public class AuditSupportTest {
    public static void main(String[] args) {
        masksSensitiveJsonFields();
        masksSensitiveFormFields();
        truncatesLongBodiesWithSize();
        identifiesSkippedAuditPaths();
    }

    private static void masksSensitiveJsonFields() {
        String body = "{\"email\":\"user@test.com\",\"password\":\"secret\",\"accessToken\":\"abc123\",\"nested\":{\"apiKey\":\"key-value\"}}";

        String masked = BodyExtractor.maskSensitiveContent(body);

        assertContains(masked, "\"password\":\"***\"");
        assertContains(masked, "\"accessToken\":\"***\"");
        assertContains(masked, "\"apiKey\":\"***\"");
        assertNotContains(masked, "secret");
        assertNotContains(masked, "abc123");
        assertNotContains(masked, "key-value");
    }

    private static void masksSensitiveFormFields() {
        Map<String, String[]> params = BodyExtractor.maskParameters(Map.of(
                "email", new String[]{"user@test.com"},
                "password", new String[]{"secret"},
                "remember", new String[]{"true"}
        ));

        assertEquals("user@test.com", params.get("email")[0]);
        assertEquals("***", params.get("password")[0]);
        assertEquals("true", params.get("remember")[0]);
    }

    private static void truncatesLongBodiesWithSize() {
        String truncated = BodyExtractor.truncate("1234567890", 4);

        assertEquals("1234 [truncated - size: 10 bytes]", truncated);
    }

    private static void identifiesSkippedAuditPaths() {
        assertTrue(RequestAuditFilter.shouldSkipAudit("/assets/site.css"));
        assertTrue(RequestAuditFilter.shouldSkipAudit("/views/product/list.jsp"));
        assertTrue(RequestAuditFilter.shouldSkipAudit("/images/logo.png"));
        assertFalse(RequestAuditFilter.shouldSkipAudit("/auth?action=login"));
        assertFalse(RequestAuditFilter.shouldSkipAudit("/product"));
    }

    private static void assertContains(String actual, String expected) {
        if (!actual.contains(expected)) {
            throw new AssertionError("Expected <" + actual + "> to contain <" + expected + ">");
        }
    }

    private static void assertNotContains(String actual, String expected) {
        if (actual.contains(expected)) {
            throw new AssertionError("Expected <" + actual + "> not to contain <" + expected + ">");
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
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
