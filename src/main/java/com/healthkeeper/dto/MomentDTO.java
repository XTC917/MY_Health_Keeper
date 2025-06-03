package com.healthkeeper.dto;

import com.healthkeeper.entity.MediaItem;
import com.healthkeeper.entity.Moment;
import com.healthkeeper.entity.User;
import com.healthkeeper.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MomentDTO {
    private Long id;
    private String content;
    private String username;
    private Long userId;
    private List<String> images;
    private List<MediaItem> media;
    private LocalDateTime createdAt;
    private Integer likesCount;
    private List<UserSummary> likes;
    private Integer commentCount;
    private List<CommentDTO> comments;

    // 构造函数从实体转为DTO
    public MomentDTO(Moment moment) {
        this.id = moment.getId();
        this.content = moment.getContent();
        this.username = moment.getUser().getUsername(); // 简化 user
        this.userId = moment.getUser().getId(); // 提取 user.id
        this.images = moment.getImages();
        this.media = moment.getMedia();
        this.createdAt = moment.getCreatedAt();
        this.likesCount = moment.getLikes().size();
        this.likes = moment.getLikes().stream()
                .map(UserSummary::new)
                .collect(Collectors.toList());
        this.commentCount = moment.getComments().size();
        this.comments = moment.getComments().stream()
                .map(comment -> new CommentDTO(comment))
                .collect(Collectors.toList());
    }

    @Data
    public static class UserSummary {
        private Long id;
        private String username;

        public UserSummary(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
        }
    }

    @Data
    public static class CommentDTO {
        private Long id;
        private String content;
        private String username;
        private LocalDateTime createdAt;

        public CommentDTO(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.username = comment.getUser().getUsername();
            this.createdAt = comment.getCreatedAt();
        }
    }
}

