package com.healthkeeper.dto;

import com.healthkeeper.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponseDTO {
    private Long id;
    private Long momentId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    private List<CommentResponseDTO> replies; // 如果你有嵌套回复功能

    public static CommentResponseDTO fromEntity(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setMomentId(comment.getMoment().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        // 若有子评论，递归转换（假设你有 getReplies() 方法）
        /*if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(CommentResponseDTO::fromEntity)
                    .collect(Collectors.toList()));
        }*/

        return dto;
    }
}
