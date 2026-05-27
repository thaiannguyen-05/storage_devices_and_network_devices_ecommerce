package module.bussiness.contact;

import entity.UserEntity;
import java.util.UUID;
import module.bussiness.contact.repository.impl.ContactRepository;
import module.core.auth.repository.impl.AuthRepository;
import module.core.common.BaseResponse;
import module.core.mail.EmailService;
import module.core.outbox.OutBoxService;
import module.core.outbox.TypeEvent;

public class ContactService {
    private final ContactRepository contactRepository = new ContactRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final EmailService emailService = new EmailService();
    private final OutBoxService outBoxService = new OutBoxService();

    public BaseResponse submitContact(String name, String email, String content) {
        BaseResponse response = new BaseResponse();
        if (isBlank(name) || isBlank(email) || isBlank(content)) {
            fail(response, "Name, email and message are required");
            return response;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            fail(response, "Email định dạng không đúng");
            return response;
        }
        contactRepository.insert(UUID.randomUUID().toString(), name.trim(), email.trim(), content.trim());
        sendContactNotification(name, email, content);
        response.setSuccess(true);
        response.setSuccessMessage("Đã ghi nhận thông tin liên hệ");
        return response;
    }

    private void sendContactNotification(String name, String email, String content) {
        try {
            UserEntity admin = authRepository.findAdmin();
            if (admin == null) {
                return;
            }
            String subject = "Liên hệ mới từ " + name;
            String body = String.format(
                "Có liên hệ mới từ khách hàng.\n\n" +
                "Tên: %s\n" +
                "Email: %s\n" +
                "Nội dung:\n%s",
                name, email, content
            );
            emailService.send(admin.getEmail(), subject, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
