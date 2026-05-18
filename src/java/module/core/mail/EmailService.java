package module.core.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import module.core.config.ConfigService;

public class EmailService {
    private final ConfigService config = ConfigService.getInstance();

    public void send(String to, String subject, String body) {
        if (isBlank(to)) {
            throw new IllegalArgumentException("Email recipient is required");
        }

        String username = config.require("SMTP_USER");
        String password = config.require("SMTP_PASS");
        String from = config.get("SMTP_FROM", username);

        try {
            Message message = new MimeMessage(createSession(username, password));
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Unable to send email to " + to, ex);
        }
    }

    private Session createSession(String username, String password) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", config.get("SMTP_HOST", "smtp.gmail.com"));
        properties.put("mail.smtp.port", config.get("SMTP_PORT", "587"));
        properties.put("mail.smtp.auth", config.get("SMTP_AUTH", "true"));
        properties.put("mail.smtp.starttls.enable", config.get("SMTP_STARTTLS", "true"));
        properties.put("mail.smtp.ssl.trust", config.get("SMTP_SSL_TRUST", config.get("SMTP_HOST", "smtp.gmail.com")));
        properties.put("mail.smtp.connectiontimeout", config.get("SMTP_CONNECTION_TIMEOUT_MS", "10000"));
        properties.put("mail.smtp.timeout", config.get("SMTP_TIMEOUT_MS", "10000"));
        properties.put("mail.smtp.writetimeout", config.get("SMTP_WRITE_TIMEOUT_MS", "10000"));

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug("true".equalsIgnoreCase(config.get("SMTP_DEBUG", "false")));
        return session;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
