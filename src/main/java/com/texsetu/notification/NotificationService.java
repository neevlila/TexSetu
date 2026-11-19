package com.texsetu.notification;

import com.texsetu.entity.Notification;
import com.texsetu.entity.User;

import java.util.List;

public interface NotificationService {
    void sendNotification(User recipient, String title, String message, String targetUrl);
    List<Notification> getRecentNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAllAsRead(Long userId);
}
