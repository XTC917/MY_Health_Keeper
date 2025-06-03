package com.healthkeeper.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private String content;
    private Long parentId;  // 父评论ID
    private Long replyToUserId;  // 被回复的用户ID
}
