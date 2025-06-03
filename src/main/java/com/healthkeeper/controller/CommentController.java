package com.healthkeeper.controller;

import com.healthkeeper.dto.CommentDTO;
import com.healthkeeper.dto.CommentResponseDTO;
import com.healthkeeper.entity.Comment;
import com.healthkeeper.entity.Moment;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CommentRepository;
import com.healthkeeper.repository.MomentRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/moments/comments")
public class CommentController {
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/{momentId}")
    public ResponseEntity<?> getCommentsByMoment(@PathVariable Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        // 只获取顶级评论（没有父评论的评论）
        List<Comment> comments = commentRepository.findByMomentAndParentIsNullOrderByCreatedAtDesc(moment);

        List<CommentResponseDTO> response = comments.stream().map(comment -> {
            CommentResponseDTO dto = CommentResponseDTO.fromEntity(comment);
            // 设置被回复用户的用户名
            if (comment.getReplyToUserId() != null) {
                User replyToUser = userRepository.findById(comment.getReplyToUserId())
                        .orElse(null);
                if (replyToUser != null) {
                    dto.setReplyToUsername(replyToUser.getUsername());
                }
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{momentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createComment(
            @PathVariable Long momentId,
            @RequestBody CommentDTO dto,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Moment moment = momentRepository.findById(momentId)
                    .orElseThrow(() -> new RuntimeException("Moment not found"));

            Comment comment = new Comment();
            comment.setUser(user);
            comment.setMoment(moment);
            comment.setContent(dto.getContent());

            // 处理回复
            if (dto.getParentId() != null) {
                Comment parentComment = commentRepository.findById(dto.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent comment not found"));
                comment.setParent(parentComment);
                
                // 设置被回复的用户ID
                if (dto.getReplyToUserId() != null) {
                    comment.setReplyToUserId(dto.getReplyToUserId());
                } else {
                    // 如果没有指定被回复用户，则默认回复父评论的作者
                    comment.setReplyToUserId(parentComment.getUser().getId());
                }
            }

            Comment savedComment = commentRepository.save(comment);
            CommentResponseDTO responseDTO = CommentResponseDTO.fromEntity(savedComment);

            // 设置被回复用户的用户名
            if (savedComment.getReplyToUserId() != null) {
                User replyToUser = userRepository.findById(savedComment.getReplyToUserId())
                        .orElse(null);
                if (replyToUser != null) {
                    responseDTO.setReplyToUsername(replyToUser.getUsername());
                }
            }

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("评论创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody Comment updatedComment,
                                         Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this comment");
        }

        if (updatedComment.getContent() != null) {
            comment.setContent(updatedComment.getContent());
        }

        Comment savedComment = commentRepository.save(comment);
        return ResponseEntity.ok(savedComment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }
} 
