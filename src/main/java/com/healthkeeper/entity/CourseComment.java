package com.healthkeeper.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "course_comments")
public class CourseComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId; // 关联课程ID

    @Column(name = "user_id", nullable = false)
    private Long userId; // 评论用户ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 评论内容

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 评论时间
}
