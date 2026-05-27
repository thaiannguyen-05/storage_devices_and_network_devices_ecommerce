package module.core.outbox.repository.impl;

import entity.OutBoxEntity;
import java.util.List;
import module.core.outbox.repository.interfaces.IOutBoxRepository;
import module.core.sql.JdbcHelper;

public class OutBoxRepository implements IOutBoxRepository {
    private static volatile boolean schemaChecked;

    @Override
    public void insert(OutBoxEntity entity) {
        ensureSchema();
        JdbcHelper.executeUpdate("INSERT INTO OutBox (id, code, status, type, userId, expiresAt) VALUES (?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 24 HOUR))",
                entity.getId(), entity.getCode(), entity.getStatus(), entity.getType(), entity.getUserId());
    }

    @Override
    public List<OutBoxEntity> findPending(int limit) {
        return JdbcHelper.executeQuery("SELECT * FROM OutBox WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT ?",
                rs -> mapOutBox(rs),
                limit);
    }

    @Override
    public OutBoxEntity findValidCode(String userId, String type, String code) {
        ensureSchema();
        List<OutBoxEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM OutBox WHERE userId = ? AND type = ? AND code = ? AND status IN ('PENDING', 'PROCESSED') AND usedAt IS NULL AND (expiresAt IS NULL OR expiresAt > NOW()) ORDER BY createdAt DESC LIMIT 1",
                rs -> mapOutBox(rs),
                userId, type, code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private OutBoxEntity mapOutBox(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
        java.sql.Timestamp updatedAt = rs.getTimestamp("updatedAt");
        return new OutBoxEntity(rs.getString("id"), rs.getString("code"), rs.getString("status"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime(),
                rs.getString("type"), rs.getString("userId"));
    }

    @Override
    public void markProcessed(String id) {
        JdbcHelper.executeUpdate("UPDATE OutBox SET status = 'PROCESSED' WHERE id = ?", id);
    }

    @Override
    public void markFailed(String id) {
        JdbcHelper.executeUpdate("UPDATE OutBox SET status = 'FAILED' WHERE id = ?", id);
    }

    @Override
    public void markUsed(String id) {
        ensureSchema();
        JdbcHelper.executeUpdate("UPDATE OutBox SET usedAt = NOW() WHERE id = ?", id);
    }

    private void ensureSchema() {
        if (schemaChecked) {
            return;
        }
        synchronized (OutBoxRepository.class) {
            if (schemaChecked) {
                return;
            }
            addColumnIfMissing("expiresAt", "ALTER TABLE OutBox ADD COLUMN expiresAt DATETIME NULL");
            addColumnIfMissing("usedAt", "ALTER TABLE OutBox ADD COLUMN usedAt DATETIME NULL");
            schemaChecked = true;
        }
    }

    private void addColumnIfMissing(String columnName, String alterSql) {
        int count = JdbcHelper.count(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'OutBox' AND COLUMN_NAME = ?",
                columnName);
        if (count == 0) {
            JdbcHelper.executeUpdate(alterSql);
        }
    }
}
