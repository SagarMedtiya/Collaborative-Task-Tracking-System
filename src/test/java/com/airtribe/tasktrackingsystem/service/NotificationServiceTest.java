package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.response.NotificationResponse;
import com.airtribe.tasktrackingsystem.exception.ResourceNotFoundException;
import com.airtribe.tasktrackingsystem.model.Notification;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createAndSendNotificationPersistsAndPushesToRecipientUsername() {
        Users recipient = Users.builder().id(1L).username("alice").build();
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        NotificationResponse response = notificationService.createAndSendNotification(
                recipient, "TASK_ASSIGNED", "Task Assigned: X", "msg", "Task", 5L);

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertEquals("Task Assigned: X", saved.getValue().getTitle());
        assertFalse(saved.getValue().getIsRead());
        assertEquals(10L, response.getId());
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/alice"), eq(response));
    }

    @Test
    void markAsReadRejectsNotificationOwnedByAnotherUser() {
        Users owner = Users.builder().id(1L).username("alice").build();
        Users other = Users.builder().id(2L).username("bob").build();
        Notification notification = Notification.builder().id(10L).recipient(owner).isRead(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(10L, other));
        assertFalse(notification.getIsRead());
    }

    @Test
    void deleteNotificationRejectsNotificationOwnedByAnotherUser() {
        Users owner = Users.builder().id(1L).username("alice").build();
        Users other = Users.builder().id(2L).username("bob").build();
        Notification notification = Notification.builder().id(10L).recipient(owner).isRead(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(ResourceNotFoundException.class, () -> notificationService.deleteNotification(10L, other));
        verify(notificationRepository, org.mockito.Mockito.never()).delete(any(Notification.class));
    }
}
