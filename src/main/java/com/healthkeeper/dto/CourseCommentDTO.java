package com.healthkeeper.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CourseCommentDTO {
    private Long id;
    private Long courseId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private String username;
    private String userAvatar;
}
