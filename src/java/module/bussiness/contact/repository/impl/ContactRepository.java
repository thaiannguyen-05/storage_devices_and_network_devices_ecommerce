package module.bussiness.contact.repository.impl;

import entity.ContactEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.core.sql.ConnecDb;

public class ContactRepository {
    private static final Logger LOGGER = Logger.getLogger(ContactRepository.class.getName());

    public boolean save(ContactEntity contact) {
        String sql = "INSERT INTO \"Contact\" (\"id\", \"fullName\", \"email\", \"subject\", \"message\", \"status\", \"createdAt\") "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = contact.getId() == null || contact.getId().isBlank()
                    ? UUID.randomUUID().toString()
                    : contact.getId().trim();
            ps.setString(1, id);
            ps.setString(2, contact.getFullName());
            ps.setString(3, contact.getEmail());
            ps.setString(4, contact.getSubject());
            ps.setString(5, contact.getMessage());
            ps.setString(6, contact.getStatus() == null || contact.getStatus().isBlank() ? "NEW" : contact.getStatus().trim());
            boolean saved = ps.executeUpdate() > 0;
            if (saved) {
                contact.setId(id);
            }
            return saved;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save contact submission for email=" + contact.getEmail(), e);
            return false;
        }
    }
}
