package module.bussiness.contact.repository.interfaces;

public interface IContactRepository {
    void insert(String id, String name, String email, String content);
}
