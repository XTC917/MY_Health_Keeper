// src/main/java/com/healthkeeper/entity/FriendRequest.java
package com.healthkeeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({
            "friends", "sentRequests", "receivedRequests",
            "enrolledCourses", "courseProgresses", "roles"
    })
    private User sender;

    @ManyToOne
    @JsonIgnoreProperties({
            "friends", "sentRequests", "receivedRequests",
            "enrolledCourses", "courseProgresses", "roles"
    })
    private User receiver;

    private LocalDateTime createdAt;
    private String status; // PENDING, ACCEPTED, REJECTED

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}