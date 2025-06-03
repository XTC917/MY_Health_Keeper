package com.healthkeeper.dto;

import com.healthkeeper.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommentResponseDTO {
    private Long id;
    private Long momentId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUsername;
    private List<CommentResponseDTO> replies;

    public static CommentResponseDTO fromEntity(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setMomentId(comment.getMoment().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        
        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }
        
        if (comment.getReplyToUserId() != null) {
            dto.setReplyToUserId(comment.getReplyToUserId());
            // 这里需要设置被回复用户的用户名，可以在service层处理
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(CommentResponseDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
