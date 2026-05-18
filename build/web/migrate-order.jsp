<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*, module.core.config.DbConfig"%>
<%
String sql = "ALTER TABLE `Order` ADD COLUMN IF NOT EXISTS customerName VARCHAR(255), " +
    "ADD COLUMN IF NOT EXISTS email VARCHAR(255), " +
    "ADD COLUMN IF NOT EXISTS note TEXT, " +
    "ADD COLUMN IF NOT EXISTS paymentMethod VARCHAR(50), " +
    "ADD COLUMN IF NOT EXISTS voucherId VARCHAR(50), " +
    "ADD COLUMN IF NOT EXISTS totalAmount DECIMAL(15,2)";
try (Connection conn = DbConfig.getConnection();
     Statement stmt = conn.createStatement()) {
    stmt.executeUpdate(sql);
    out.println("SUCCESS: Order table altered successfully");
} catch (SQLException e) {
    // MySQL < 8.0.29 doesn't support IF NOT EXISTS for columns, try without
    String[] cols = {
        "ALTER TABLE `Order` ADD COLUMN customerName VARCHAR(255)",
        "ALTER TABLE `Order` ADD COLUMN email VARCHAR(255)",
        "ALTER TABLE `Order` ADD COLUMN note TEXT",
        "ALTER TABLE `Order` ADD COLUMN paymentMethod VARCHAR(50)",
        "ALTER TABLE `Order` ADD COLUMN voucherId VARCHAR(50)",
        "ALTER TABLE `Order` ADD COLUMN totalAmount DECIMAL(15,2)"
    };
    try (Connection conn = DbConfig.getConnection();
         Statement stmt = conn.createStatement()) {
        for (String colSql : cols) {
            try {
                stmt.executeUpdate(colSql);
                out.println("OK: " + colSql.split("ADD COLUMN ")[1].split(" ")[0] + " added<br>");
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1060) {
                    out.println("EXISTS: " + colSql.split("ADD COLUMN ")[1].split(" ")[0] + " already exists<br>");
                } else {
                    throw ex;
                }
            }
        }
    }
}
%>
