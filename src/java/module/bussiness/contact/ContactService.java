package module.bussiness.contact;

import java.util.UUID;
import module.bussiness.contact.repository.impl.ContactRepository;
import module.core.common.BaseResponse;

public class ContactService {
    private final ContactRepository contactRepository = new ContactRepository();

    public BaseResponse submitContact(String name, String email, String content) {
        BaseResponse response = new BaseResponse();
        if (isBlank(name) || isBlank(email) || isBlank(content)) {
            fail(response, "Name, email and message are required");
            return response;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            fail(response, "Email format is invalid");
            return response;
        }
        contactRepository.insert(UUID.randomUUID().toString(), name.trim(), email.trim(), content.trim());
        response.setSuccess(true);
        response.setSuccessMessage("Da ghi nhan thong tin");
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
