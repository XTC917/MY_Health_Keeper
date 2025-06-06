// src/main/java/com/healthkeeper/repository/FriendRequestRepository.java
package com.healthkeeper.repository;

import com.healthkeeper.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, String status);
    List<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}