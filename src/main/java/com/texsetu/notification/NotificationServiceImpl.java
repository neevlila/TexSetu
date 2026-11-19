package com.texsetu.notification;

import com.texsetu.entity.Notification;
import com.texsetu.entity.User;
import com.texsetu.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Async
    @Transactional
    public void sendNotification(User recipient, String title, String message, String targetUrl) {
        try {
            Notification notification = new Notification(recipient, title, message, targetUrl);
            notificationRepository.save(notification);
            log.info("[ASYNC-NOTIFY] Sent notification to user {}: '{}' - URL: {}", recipient.getEmail(), title, targetUrl);
        } catch (Exception e) {
            log.error("[ASYNC-NOTIFY-ERROR] Failed to send notification to user {}: {}", recipient.getEmail(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
