package com.healthkeeper.controller;

import com.healthkeeper.dto.CommentDTO;
import com.healthkeeper.dto.CommentResponse;
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

        List<Comment> comments = commentRepository.findByMomentOrderByCreatedAtDesc(moment);

        List<CommentResponseDTO> response = comments.stream().map(comment -> {
            CommentResponseDTO dto = new CommentResponseDTO();
            dto.setId(comment.getId());
            dto.setMomentId(comment.getMoment().getId());
            dto.setUserId(comment.getUser().getId());
            dto.setUsername(comment.getUser().getUsername()); // 需要 user 实体中有 username
            dto.setContent(comment.getContent());
            dto.setCreatedAt(comment.getCreatedAt());
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

            Comment savedComment = commentRepository.save(comment);

            CommentResponseDTO responseDTO = CommentResponseDTO.fromEntity(savedComment);

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
