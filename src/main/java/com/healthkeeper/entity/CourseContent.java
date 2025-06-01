package com.healthkeeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "course_contents")
public class CourseContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_type")
    private String contentType; // 内容类型：视频、文档、测验等

    @Column(name = "content_url")
    private String contentUrl; // 内容URL（如视频链接）

    @Column(name = "content_order")
    private Integer order;

    @Column(name = "duration")
    private Integer duration; // 内容时长（分钟）

    @Column(name = "is_free")
    private Boolean isFree; // 是否免费
} 