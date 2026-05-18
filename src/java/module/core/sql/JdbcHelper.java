package module.core.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import module.core.config.DbConfig;

public class JdbcHelper {
    private JdbcHelper() {
    }

    public static <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return rows;
    }

    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static int[] executeBatch(String sql, List<Object[]> paramRows) {
        try (Connection conn = DbConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] params : paramRows) {
                setParams(ps, params);
                ps.addBatch();
            }
            return ps.executeBatch();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            if (value instanceof LocalDate) {
                ps.setDate(i + 1, java.sql.Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalDateTime) {
                ps.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) value));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    public static int count(String sql, Object... params) {
        List<Integer> rows = executeQuery(sql, rs -> rs.getInt(1), params);
        return rows.isEmpty() ? 0 : rows.get(0);
    }

    public static int executeRaw(String sql) {
        try (Connection conn = DbConfig.getConnection();
                Statement statement = conn.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
