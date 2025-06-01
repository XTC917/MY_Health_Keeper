package com.healthkeeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "moments")
public class Moment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "moment_images", joinColumns = @JoinColumn(name = "moment_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //@Column(name = "likes_count")
    //private Integer likesCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "moment_likes",
        joinColumns = @JoinColumn(name = "moment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    // 添加媒体文件列表
    @ElementCollection
    @CollectionTable(name = "moment_media", joinColumns = @JoinColumn(name = "moment_id"))
    private List<MediaItem> media = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "moment_courses",
            joinColumns = @JoinColumn(name = "moment_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "moment", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        //likesCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        //likesCount = likes.size();
    }
} 