package edu.cit.salonga.assetflow.features.notification.service;

import edu.cit.salonga.assetflow.features.auth.entity.User;
import edu.cit.salonga.assetflow.features.notification.entity.Notification;
import edu.cit.salonga.assetflow.features.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User user, String type, String message) {
        if (user == null) {
            throw new RuntimeException("User is required for notification");
        }
        if (message == null || message.isBlank()) {
            throw new RuntimeException("Notification message is required");
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type == null || type.isBlank() ? "system" : type.trim());
        notification.setMessage(message.trim());

        notificationRepository.save(notification);
    }
}
