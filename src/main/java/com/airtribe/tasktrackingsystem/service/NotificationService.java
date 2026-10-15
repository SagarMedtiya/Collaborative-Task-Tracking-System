package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.config.WebSocketAuthInterceptor;
import com.airtribe.tasktrackingsystem.dto.response.NotificationResponse;
import com.airtribe.tasktrackingsystem.exception.ResourceNotFoundException;
import com.airtribe.tasktrackingsystem.model.Notification;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponse createAndSendNotification(Users recipient, String type, String title,
                                                          String message, String relatedEntityType,
                                                          Long relatedEntityId){
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        NotificationResponse response = NotificationResponse.from(notification);
        sendNotification(recipient.getUsername(), response);
        return response;
    }

    public void sendNotification(String username, NotificationResponse notification){
        messagingTemplate.convertAndSend(WebSocketAuthInterceptor.NOTIFICATION_TOPIC_PREFIX + username, notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Users user, Pageable pageable){
        return toResponsePage(notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Users user, Pageable pageable){
        return toResponsePage(notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user, pageable), pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Users user){
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, Users user){
        Notification notification = findOwnedNotification(notificationId, user);
        notification.setIsRead(true);
    }

    @Transactional
    public void markAllAsRead(Users user){
        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Users user){
        notificationRepository.delete(findOwnedNotification(notificationId, user));
    }

    private Notification findOwnedNotification(Long notificationId, Users user){
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getRecipient().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
    }

    private Page<NotificationResponse> toResponsePage(Page<Notification> notifications, Pageable pageable){
        return new PageImpl<>(
                notifications.getContent().stream()
                        .map(NotificationResponse::from)
                        .collect(Collectors.toList()),
                pageable,
                notifications.getTotalElements()
        );
    }
}
