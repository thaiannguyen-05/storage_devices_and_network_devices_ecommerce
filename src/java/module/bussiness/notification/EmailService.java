package module.bussiness.notification;

import java.time.LocalDateTime;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.AuthenticationFailedException;
import module.core.config.ConfigService;

public class EmailService {

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String host = getConfig("SMTP_HOST", "");
        int port = parseInt(getConfig("SMTP_PORT", "587"), 587);
        String username = getConfig("SMTP_USER", "");
        String password = getConfig("SMTP_PASS", "");
        String from = getConfig("SMTP_FROM", username);

        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new RuntimeException("SMTP config missing: host/user/pass/from is blank");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            if (port == 465) {
                props.put("mail.smtp.starttls.enable", "false");
                props.put("mail.smtp.ssl.enable", "true");
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Đặt lại mật khẩu");
            message.setText("Bạn đã yêu cầu đặt lại mật khẩu.\n\n"
                    + "Vui lòng truy cập link sau trong vòng 15 phút:\n"
                    + resetLink
                    + "\n\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: kiểm tra SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    public void sendVerificationCodeEmail(String toEmail, String fullName, String code) {
        String host = getConfig("SMTP_HOST", "");
        int port = parseInt(getConfig("SMTP_PORT", "587"), 587);
        String username = getConfig("SMTP_USER", "");
        String password = getConfig("SMTP_PASS", "");
        String from = getConfig("SMTP_FROM", username);

        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new RuntimeException("SMTP config missing: host/user/pass/from is blank");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            if (port == 465) {
                props.put("mail.smtp.starttls.enable", "false");
                props.put("mail.smtp.ssl.enable", "true");
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Mã xác thực tài khoản");
            message.setText("Xin chào " + (fullName == null ? "bạn" : fullName) + ",\n\n"
                    + "Mã xác thực tài khoản của bạn là: " + code + "\n"
                    + "Mã có hiệu lực trong 10 phút.\n\n"
                    + "Trân trọng,\nStoreIT");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: kiểm tra SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    public void sendForgotPasswordCodeEmail(String toEmail, String fullName, String code) {
        String host = getConfig("SMTP_HOST", "");
        int port = parseInt(getConfig("SMTP_PORT", "587"), 587);
        String username = getConfig("SMTP_USER", "");
        String password = getConfig("SMTP_PASS", "");
        String from = getConfig("SMTP_FROM", username);

        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new RuntimeException("SMTP config missing: host/user/pass/from is blank");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            if (port == 465) {
                props.put("mail.smtp.starttls.enable", "false");
                props.put("mail.smtp.ssl.enable", "true");
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Password reset code");
            message.setText("Hello " + (fullName == null ? "there" : fullName) + ",\n\n"
                    + "Your password reset code is: " + code + "\n"
                    + "This code is valid for 10 minutes.\n\n"
                    + "If you did not request this, please ignore this email.\n\n"
                    + "Best regards,\nStoreIT");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: check SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    public void sendLoginAlertEmail(String toEmail, String fullName, String ipAddress) {
        String host = getConfig("SMTP_HOST", "");
        int port = parseInt(getConfig("SMTP_PORT", "587"), 587);
        String username = getConfig("SMTP_USER", "");
        String password = getConfig("SMTP_PASS", "");
        String from = getConfig("SMTP_FROM", username);

        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new RuntimeException("SMTP config missing: host/user/pass/from is blank");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            if (port == 465) {
                props.put("mail.smtp.starttls.enable", "false");
                props.put("mail.smtp.ssl.enable", "true");
            }

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Cảnh báo đăng nhập từ IP mới");
            message.setText("Xin chào " + (fullName == null ? "bạn" : fullName) + ",\n\n"
                    + "Hệ thống vừa ghi nhận đăng nhập từ địa chỉ IP mới: " + (ipAddress == null ? "unknown" : ipAddress) + "\n"
                    + "Thời gian: " + LocalDateTime.now() + "\n\n"
                    + "Nếu không phải bạn, vui lòng đổi mật khẩu ngay.\n\n"
                    + "Trân trọng,\nStoreIT");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: kiểm tra SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    private String getConfig(String key, String defaultValue) {
        String fromJvm = System.getProperty(key);
        if (fromJvm != null && !fromJvm.isBlank()) {
            return fromJvm;
        }
        String fromDotenv = ConfigService.getOrDefault(key, "");
        if (fromDotenv != null && !fromDotenv.isBlank()) {
            return fromDotenv;
        }
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return defaultValue;
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }
}
