package module.core.auth;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.core.config.ConfigService;

public class MailService {
    private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());

    public void sendResetPasswordMail(String toEmail, String resetLink) {
        String host = ConfigService.getOrDefault("MAIL_HOST", "smtp.gmail.com");
        int port = ConfigService.getInt("MAIL_PORT", 587);
        String username = ConfigService.getOrDefault("MAIL_USERNAME", "");
        String password = ConfigService.getOrDefault("MAIL_PASSWORD", "");
        String from = ConfigService.getOrDefault("MAIL_FROM", username);

        if (username.isEmpty() || password.isEmpty()) {
            throw new RuntimeException("MAIL_USERNAME hoặc MAIL_PASSWORD chưa được cấu hình");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Reset your StoreIT password");
            message.setText("Click this link to reset your password: " + resetLink
                    + "\n\nIf you did not request this, please ignore this email.");
            Transport.send(message);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "SMTP send failed to " + toEmail + " via " + host + ":" + port + " user=" + username, e);
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }
}
