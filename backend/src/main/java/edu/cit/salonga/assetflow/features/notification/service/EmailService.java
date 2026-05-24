package edu.cit.salonga.assetflow.features.notification.service;

import edu.cit.salonga.assetflow.features.assets.entity.Asset;
import edu.cit.salonga.assetflow.features.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    private NotificationService notificationService;

    @Value("${spring.mail.from:}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String username;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to AssetFlow";
        String body = "Hey " + safeName(user.getName()) + "!\n\n" +
                "Welcome to AssetFlow! Your account has been created successfully.\n\n" +
                "You can now sign in and start browsing assets and borrow what you need.\n\n" +
                "Thanks,\nAssetFlow";
        sendEmail(user.getEmail(), subject, body);
        notificationService.createNotification(user, "welcome", "Welcome to AssetFlow! Your account is ready.");
    }

    public void sendBorrowApprovedEmail(User user, Asset asset, LocalDate dueDate) {
        String subject = "Borrow Request Approved | AssetFlow";
        String body = "Hello, " + safeName(user.getName()) + "!\n\n" +
                "Your borrow request has been approved for the following:\n" +
                "Asset: " + asset.getName() + "\n" +
                "Due date: " + (dueDate != null ? dueDate : "-") + "\n\n" +
                "Please return the asset on or before the due date.\n\n" +
                "Thanks,\nAssetFlow";
        sendEmail(user.getEmail(), subject, body);
        notificationService.createNotification(user, "borrow_approved", "Your borrow request was approved for " + asset.getName() + ".");
    }

    public void sendBorrowRejectedEmail(User user, Asset asset, String note) {
        String subject = "Borrow Request Rejected | AssetFlow";
        String body = "Hello, " + safeName(user.getName()) + "!\n\n" +
                "Your borrow request has been rejected for the following:\n" +
                "Asset: " + asset.getName() + "\n" +
                "Reason: " + (note == null || note.isBlank() ? "No reason provided." : note.trim()) + "\n\n" +
                "If you have questions, please contact the admin.\n\n" +
                "Thanks,\nAssetFlow";
        sendEmail(user.getEmail(), subject, body);
        notificationService.createNotification(user, "borrow_rejected", "Your borrow request was rejected for " + asset.getName() + ".");
    }

    public void sendAssetReturnedEmail(User user, Asset asset, LocalDate returnDate) {
        String subject = "Asset Returned Confirmed | AssetFlow";
        String body = "Hello, " + safeName(user.getName()) + "!\n\n" +
                "Your returned asset has been recorded successfully.\n" +
                "Asset: " + asset.getName() + "\n" +
                "Return date: " + (returnDate != null ? returnDate : "-") + "\n\n" +
                "Thanks for using AssetFlow!\n\n";
        sendEmail(user.getEmail(), subject, body);
        notificationService.createNotification(user, "asset_returned", "Return recorded for " + asset.getName() + ".");
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new RuntimeException("Recipient email is required");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        String resolvedFrom = fromAddress != null && !fromAddress.isBlank() ? fromAddress : username;
        if (resolvedFrom != null && !resolvedFrom.isBlank()) {
            message.setFrom(resolvedFrom);
        }

        mailSender.send(message);
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "there" : value;
    }
}
