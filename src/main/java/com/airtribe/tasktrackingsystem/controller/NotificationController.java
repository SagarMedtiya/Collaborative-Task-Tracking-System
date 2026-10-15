package com.airtribe.tasktrackingsystem.controller;

import com.airtribe.tasktrackingsystem.dto.response.NotificationResponse;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.service.NotificationService;
import com.airtribe.tasktrackingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotification(
            @RequestParam(defaultValue = "false") boolean unread,
            Pageable pageable,
            Authentication authentication
    ){
        Users user = userService.getEntityByUsername(authentication.getName());

        Page<NotificationResponse> notifications = unread ?
            notificationService.getUnreadNotifications(user, pageable) :
            notificationService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(notifications);
    }
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication){
        Users user = userService.getEntityByUsername(authentication.getName());
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(count);
    }
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication){
        Users user = userService.getEntityByUsername(authentication.getName());
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication){
        Users user = userService.getEntityByUsername(authentication.getName());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication){
        Users user = userService.getEntityByUsername(authentication.getName());
        notificationService.deleteNotification(id, user);
        return ResponseEntity.noContent().build();
    }
}
