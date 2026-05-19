package module.bussiness.ai;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import module.core.sql.JdbcHelper;

public class AiShopContextService {
    private static final int MAX_PRODUCTS = 5;
    private static final int MAX_ORDERS = 5;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String buildContext(String prompt, String userId) {
        if (isBlank(prompt)) {
            return "";
        }

        List<String> sections = new ArrayList<String>();
        if (isOrderQuery(prompt)) {
            String orderContext = buildOrderContext(userId);
            if (!isBlank(orderContext)) {
                sections.add(orderContext);
            }
        }
        if (isProductQuery(prompt)) {
            String productContext = buildProductContext(prompt);
            if (!isBlank(productContext)) {
                sections.add(productContext);
            }
        }

        if (sections.isEmpty()) {
            return "";
        }
        return "Du lieu noi bo cua shop de tham khao. Chi su dung neu phu hop voi cau hoi.\n"
                + String.join("\n\n", sections);
    }

    private String buildProductContext(String prompt) {
        try {
            List<String> tokens = extractSearchTokens(prompt);
            StringBuilder sql = new StringBuilder(
                    "SELECT p.id, p.name, p.category, b.name AS brandName, MIN(v.price) AS minPrice, "
                    + "SUM(CASE WHEN v.quantity IS NULL THEN 0 ELSE v.quantity END) AS totalStock "
                    + "FROM Product p "
                    + "LEFT JOIN Brand b ON b.id = p.brandId "
                    + "LEFT JOIN ProductVariant v ON v.productId = p.id "
                    + "WHERE p.status = 'ACTIVE' ");
            List<Object> params = new ArrayList<Object>();

            if (!tokens.isEmpty()) {
                sql.append("AND (");
                for (int i = 0; i < tokens.size(); i++) {
                    if (i > 0) {
                        sql.append(" OR ");
                    }
                    sql.append("(LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(p.category) LIKE ? OR LOWER(b.name) LIKE ?)");
                    String like = "%" + tokens.get(i) + "%";
                    params.add(like);
                    params.add(like);
                    params.add(like);
                    params.add(like);
                }
                sql.append(") ");
            }

            sql.append("GROUP BY p.id, p.name, p.category, b.name ORDER BY p.createdAt DESC LIMIT ?");
            params.add(MAX_PRODUCTS);

            List<String> rows = JdbcHelper.executeQuery(sql.toString(), rs -> {
                String brand = rs.getString("brandName");
                BigDecimal minPrice = rs.getBigDecimal("minPrice");
                int totalStock = rs.getInt("totalStock");
                return "- " + rs.getString("name")
                        + " | thuong hieu: " + defaultText(brand, "Khong ro")
                        + " | danh muc: " + defaultText(rs.getString("category"), "Khong ro")
                        + " | gia tu: " + formatMoney(minPrice)
                        + " | ton kho: " + totalStock;
            }, params.toArray());

            if (rows.isEmpty()) {
                return "Khong tim thay san pham phu hop trong du lieu shop.";
            }
            return "San pham lien quan trong shop:\n" + String.join("\n", rows);
        } catch (RuntimeException ex) {
            return "Khong lay duoc du lieu san pham tu database.";
        }
    }

    private String buildOrderContext(String userId) {
        if (isBlank(userId)) {
            return "Nguoi dung chua dang nhap, khong the truy van don hang ca nhan.";
        }
        try {
            List<String> rows = JdbcHelper.executeQuery(
                    "SELECT o.id, o.status, o.quantity, o.totalAmount, o.createdAt, p.name AS productName "
                    + "FROM `Order` o "
                    + "LEFT JOIN Product p ON p.id = o.productId "
                    + "WHERE o.userId = ? "
                    + "ORDER BY o.createdAt DESC LIMIT ?",
                    rs -> "- Don " + rs.getString("id")
                    + " | san pham: " + defaultText(rs.getString("productName"), "Khong ro")
                    + " | so luong: " + rs.getInt("quantity")
                    + " | tong tien: " + formatMoney(rs.getBigDecimal("totalAmount"))
                    + " | trang thai: " + defaultText(rs.getString("status"), "Khong ro")
                    + " | tao luc: " + rs.getTimestamp("createdAt").toLocalDateTime().format(DATE_TIME_FORMAT),
                    userId, MAX_ORDERS);
            if (rows.isEmpty()) {
                return "Khong tim thay don hang nao cua nguoi dung hien tai.";
            }
            return "Don hang gan day cua nguoi dung:\n" + String.join("\n", rows);
        } catch (RuntimeException ex) {
            return "Khong lay duoc du lieu don hang tu database.";
        }
    }

    private List<String> extractSearchTokens(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^\\p{L}\\p{Nd}\\s]", " ");
        String[] rawTokens = normalized.split("\\s+");
        Set<String> tokens = new LinkedHashSet<String>();
        for (String token : rawTokens) {
            if (token.length() < 3 || isStopWord(token)) {
                continue;
            }
            tokens.add(token);
            if (tokens.size() >= 4) {
                break;
            }
        }
        return new ArrayList<String>(tokens);
    }

    private boolean isProductQuery(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ENGLISH);
        return containsAny(normalized, "san pham", "product", "ssd", "hdd", "nas", "router",
                "o cung", "ổ cứng", "phu kien", "phụ kiện", "gia", "giá", "ton kho", "tồn kho",
                "thuong hieu", "thương hiệu", "so sanh", "so sánh", "mua");
    }

    private boolean isOrderQuery(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ENGLISH);
        return containsAny(normalized, "don hang", "đơn hàng", "order", "lich su mua", "lịch sử mua",
                "trang thai don", "trạng thái đơn", "ma don", "mã đơn", "van chuyen", "vận chuyển");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStopWord(String token) {
        return "cho".equals(token)
                || "cua".equals(token)
                || "voi".equals(token)
                || "hay".equals(token)
                || "can".equals(token)
                || "nhung".equals(token)
                || "toi".equals(token)
                || "ban".equals(token)
                || "the".equals(token)
                || "la".equals(token)
                || "mot".equals(token)
                || "nhu".equals(token)
                || "nao".equals(token)
                || "giu".equals(token)
                || "mua".equals(token)
                || "gia".equals(token);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "Khong ro";
        }
        return value.stripTrailingZeros().toPlainString() + " VND";
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
