package com.healthkeeper.service;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseComment;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseCommentRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseCommentService {

    @Autowired
    private CourseCommentRepository courseCommentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取指定课程的评论，按创建时间降序排序
     */
    public List<CourseComment> getCommentsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("课程不存在");
        }
        return courseCommentRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    /**
     * 添加新的课程评论
     */
    public CourseComment createComment(Long courseId, Long userId, String content) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        return courseCommentRepository.save(comment);
    }


    /**
     * 删除评论（仅允许本人删除）
     */
    public void deleteComment(Long commentId, Long userId) {
        CourseComment comment = courseCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("你没有权限删除此评论");
        }

        courseCommentRepository.delete(comment);
    }
}
