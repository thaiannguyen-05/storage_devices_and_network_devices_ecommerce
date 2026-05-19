package module.bussiness.contact.repository.impl;

import module.bussiness.contact.repository.interfaces.IContactRepository;
import module.core.sql.JdbcHelper;

public class ContactRepository implements IContactRepository {
    @Override
    public void insert(String id, String name, String email, String content) {
        JdbcHelper.executeUpdate(
                "INSERT INTO Contact (id, name, email, content) VALUES (?, ?, ?, ?)",
                id, name, email, content);
    }
}
