package com.healthkeeper.controller;

import com.healthkeeper.dto.CourseCommentDTO;
import com.healthkeeper.entity.CourseComment;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.CourseCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course-comments")
public class CourseCommentController {

    @Autowired
    private CourseCommentService courseCommentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取某门课程的所有评论（按创建时间倒序）
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCommentsByCourse(@PathVariable Long courseId) {
        List<CourseComment> comments = courseCommentService.getCommentsByCourseId(courseId);
        List<CourseCommentDTO> commentDTOs = comments.stream().map(comment -> {
            CourseCommentDTO dto = new CourseCommentDTO();
            dto.setId(comment.getId());
            dto.setCourseId(comment.getCourseId());
            dto.setUserId(comment.getUserId());
            dto.setContent(comment.getContent());
            dto.setCreatedAt(comment.getCreatedAt());

            // 查询用户信息
            Optional<User> userOpt = userRepository.findById(comment.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar()); // 假设有 avatar 字段
            } else {
                dto.setUsername("未知用户");
                dto.setUserAvatar(null);
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(commentDTOs);
    }

    /**
     * 新增课程评论（登录用户）
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createCourseComment(@RequestBody CourseComment comment, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        CourseComment savedComment = courseCommentService.createComment(
                comment.getCourseId(), userId, comment.getContent()
        );

        return ResponseEntity.ok(savedComment);
    }

    /**
     * 删除评论（只能本人）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCourseComment(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        try {
            courseCommentService.deleteComment(id, userId);
            return ResponseEntity.ok("Course comment deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
