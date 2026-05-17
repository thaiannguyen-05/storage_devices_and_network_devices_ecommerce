package module.core.sql;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresSchemaSupportTest {
    public static void main(String[] args) throws Exception {
        String schema = Files.readString(Path.of("sto.sql"), StandardCharsets.UTF_8);

        assertNotContains(schema, "SET FOREIGN_KEY_CHECKS", "sto.sql must not use MySQL FOREIGN_KEY_CHECKS");
        assertNotContains(schema, "`", "sto.sql must not use MySQL backtick identifiers");
        assertNotMatches(schema, Pattern.compile("\\bENUM\\s*\\(", Pattern.CASE_INSENSITIVE),
                "sto.sql must not use MySQL ENUM(...) declarations");
        assertNotMatches(schema, Pattern.compile("\\bDATETIME\\b", Pattern.CASE_INSENSITIVE),
                "sto.sql must use PostgreSQL timestamp types, not DATETIME");
        assertNotMatches(schema, Pattern.compile("ON\\s+UPDATE\\s+CURRENT_TIMESTAMP", Pattern.CASE_INSENSITIVE),
                "sto.sql must not use MySQL ON UPDATE CURRENT_TIMESTAMP");
        assertNotMatches(schema, Pattern.compile("\\bENGINE\\s*=", Pattern.CASE_INSENSITIVE),
                "sto.sql must not use MySQL ENGINE clauses");

        assertContains(schema, "CREATE TABLE \"User\"", "sto.sql must create quoted User table");
        assertContains(schema, "CREATE TABLE \"Order\"", "sto.sql must create quoted Order table");
        assertContains(schema, "CREATE TABLE \"ProductReview\"", "sto.sql must include ProductReview used by product/admin code");
        assertContains(schema, "\"variantId\"", "Order schema must support variantId");
        assertContains(schema, "\"quantity\"", "Order schema must support quantity");
        assertContains(schema, "\"phone\"", "Order schema must support phone");
        assertContains(schema, "\"address\"", "Order schema must support address");

        assertNoUnquotedReservedTableQueries();
    }

    private static void assertNoUnquotedReservedTableQueries() throws Exception {
        Pattern pattern = Pattern.compile("\\b(FROM|JOIN|INTO|UPDATE)\\s+(User|Order|Session)\\b|\\bDELETE\\s+FROM\\s+(User|Order|Session)\\b");
        try (Stream<Path> files = Files.walk(Path.of("src/java"))) {
            List<String> violations = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> linesWithViolations(path, pattern).stream())
                    .collect(Collectors.toList());

            if (!violations.isEmpty()) {
                throw new AssertionError("Reserved/mixed-case tables must be quoted in PostgreSQL SQL strings:\n"
                        + String.join("\n", violations));
            }
        }
    }

    private static List<String> linesWithViolations(Path path, Pattern pattern) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            return Stream.iterate(0, i -> i + 1)
                    .limit(lines.size())
                    .filter(i -> pattern.matcher(lines.get(i)).find())
                    .map(i -> path + ":" + (i + 1) + ": " + lines.get(i).trim())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
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

    private static void assertNotMatches(String text, Pattern pattern, String message) {
        if (pattern.matcher(text).find()) {
            throw new AssertionError(message);
        }
    }
}
