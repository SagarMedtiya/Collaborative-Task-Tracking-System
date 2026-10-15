package com.airtribe.tasktrackingsystem.repository;

import com.airtribe.tasktrackingsystem.model.Notification;
import com.airtribe.tasktrackingsystem.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientOrderByCreatedAtDesc(Users recipient, Pageable pageable);

    Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(Users recipient, Pageable pageable);

    long countByRecipientAndIsReadFalse(Users recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsRead(@Param("recipient") Users recipient);
}
