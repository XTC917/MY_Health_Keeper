package com.healthkeeper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "course_progress")
public class CourseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "status")
    private String status;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_time_spent")
    private Integer totalTimeSpent; // 总学习时长（分钟）

    @Column(name = "last_position")
    private Integer lastPosition; // 视频最后观看位置（秒）

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
        progress = 0;
        status = "NOT_STARTED";
        totalTimeSpent = 0;
        lastPosition = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
    }
} 