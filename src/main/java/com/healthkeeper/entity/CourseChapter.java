package com.healthkeeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "course_chapters")
public class CourseChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "chapter_order")
    private Integer order;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 预计学习时长（分钟）

//    @ElementCollection
//    @CollectionTable(name = "chapter_contents", joinColumns = @JoinColumn(name = "chapter_id"))
//    @Column(name = "content_id")
//    private List<Long> contentsId;
} 