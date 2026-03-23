package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
    List<Notification> findByReceiverAndReadFalseOrderByCreatedAtDesc(User receiver);
}
