package com.healthkeeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Data
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer duration; // 课程时长（分钟）

    @Column(nullable = false, length = 100)
    private String targetAudience; // 目标人群

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "price")
    private Double price;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "total_like_students")
    private Integer totalLikeStudents;

    @Column(name = "status", length = 20)
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "is_published")
    private Boolean isPublished = true;


//    @ManyToMany(mappedBy = "enrolledCourses")
//    @JsonIgnore
//    private Set<User> students = new HashSet<>();


//    @OneToMany(mappedBy = "courseId", cascade = CascadeType.ALL)
//    private List<Long> chapters;
//
//    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
//    private List<CourseProgress> progresses;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isPublished == null) {
            isPublished = true;
        }
        rating = 0.0;
        totalStudents = 0;
        totalLikeStudents = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 