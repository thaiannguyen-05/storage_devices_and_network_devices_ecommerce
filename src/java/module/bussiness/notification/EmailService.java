package module.bussiness.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;
import module.core.config.ConfigService;

public class EmailService {

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        Session session = createMailSession();
        String from = getFromAddress();
        int port = getPort();

        try {
            configureSslIfNeeded(session, port);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Password reset link");
            message.setText("Hello,\n\n"
                    + "Use the link below to reset your StoreIT account password:\n"
                    + resetLink + "\n\n"
                    + "This link is valid for 15 minutes.\n"
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

    public void sendVerificationCodeEmail(String toEmail, String fullName, String code) {
        Session session = createMailSession();
        String from = getFromAddress();
        int port = getPort();

        try {
            configureSslIfNeeded(session, port);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Account verification code");
            message.setText("Hello " + (fullName == null ? "there" : fullName) + ",\n\n"
                    + "Use the following code to verify your StoreIT account:\n"
                    + code + "\n\n"
                    + "This code is valid for 10 minutes.\n"
                    + "If you did not create this account, please ignore this email.\n\n"
                    + "Best regards,\nStoreIT");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: check SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    public void sendForgotPasswordCodeEmail(String toEmail, String fullName, String code) {
        Session session = createMailSession();
        String from = getFromAddress();
        int port = getPort();

        try {
            configureSslIfNeeded(session, port);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] Password reset verification code");
            message.setText("Hello " + (fullName == null ? "there" : fullName) + ",\n\n"
                    + "Use the following code to reset your StoreIT account password:\n"
                    + code + "\n\n"
                    + "This code is valid for 10 minutes.\n"
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
        Session session = createMailSession();
        String from = getFromAddress();
        int port = getPort();

        try {
            configureSslIfNeeded(session, port);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[StoreIT] New sign-in detected");
            message.setText("Hello " + (fullName == null ? "there" : fullName) + ",\n\n"
                    + "A new sign-in to your StoreIT account was detected from IP address: " + (ipAddress == null ? "unknown" : ipAddress) + "\n"
                    + "Time: " + LocalDateTime.now() + "\n\n"
                    + "If this was not you, please change your password immediately.\n\n"
                    + "Best regards,\nStoreIT");
            Transport.send(message);
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException("SMTP auth failed: check SMTP_USER/SMTP_PASS (App Password)", e);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new RuntimeException("SMTP send failed: " + reason, e);
        }
    }

    private Session createMailSession() {
        String host = getConfig("SMTP_HOST", "");
        int port = getPort();
        String username = getConfig("SMTP_USER", "");
        String password = getConfig("SMTP_PASS", "");
        String from = getFromAddress();

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

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private void configureSslIfNeeded(Session session, int port) {
        if (port == 465) {
            session.getProperties().put("mail.smtp.starttls.enable", "false");
            session.getProperties().put("mail.smtp.ssl.enable", "true");
        }
    }

    private String getFromAddress() {
        return getConfig("SMTP_FROM", getConfig("SMTP_USER", ""));
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

    private int getPort() {
        return parseInt(getConfig("SMTP_PORT", "587"), 587);
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }
}
