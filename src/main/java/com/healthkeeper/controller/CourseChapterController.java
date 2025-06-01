package com.healthkeeper.controller;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseChapter;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseChapterRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course-chapters")
public class CourseChapterController {
    @Autowired
    CourseChapterRepository courseChapterRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseChapters(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderByOrderAsc(courseId);
        return ResponseEntity.ok(chapters);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createChapter(@RequestBody CourseChapter chapter, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(chapter.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to add chapters to this course");
        }

        // 设置章节顺序
        List<CourseChapter> existingChapters = courseChapterRepository.findByCourseIdOrderByOrderAsc(course.getId());
        if (existingChapters.isEmpty()) {
            chapter.setOrder(1);
        } else {
            chapter.setOrder(existingChapters.get(existingChapters.size() - 1).getOrder() + 1);
        }

        CourseChapter savedChapter = courseChapterRepository.save(chapter);
        return ResponseEntity.ok(savedChapter);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateChapter(@PathVariable Long id, @RequestBody CourseChapter updatedChapter,
                                         Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(chapter.getCourseId());

        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update chapters in this course");
        }

        if (updatedChapter.getTitle() != null) chapter.setTitle(updatedChapter.getTitle());
        if (updatedChapter.getDescription() != null) chapter.setDescription(updatedChapter.getDescription());
        if (updatedChapter.getOrder() != null) chapter.setOrder(updatedChapter.getOrder());
        if (updatedChapter.getEstimatedDuration() != null) chapter.setEstimatedDuration(updatedChapter.getEstimatedDuration());

        CourseChapter savedChapter = courseChapterRepository.save(chapter);
        return ResponseEntity.ok(savedChapter);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteChapter(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(chapter.getCourseId());
        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete chapters from this course");
        }

        courseChapterRepository.delete(chapter);
        return ResponseEntity.ok("Chapter deleted successfully");
    }

    @PutMapping("/{id}/reorder")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reorderChapter(@PathVariable Long id, @RequestBody Integer newOrder,
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(chapter.getCourseId());
        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to reorder this chapter");
        }

        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderByOrderAsc(course.getId());

        // 重新排序
        int oldOrder = chapter.getOrder();
        if (newOrder > oldOrder) {
            // 向后移动
            for (CourseChapter c : chapters) {
                if (c.getOrder() > oldOrder && c.getOrder() <= newOrder) {
                    c.setOrder(c.getOrder() - 1);
                }
            }
        } else if (newOrder < oldOrder) {
            // 向前移动
            for (CourseChapter c : chapters) {
                if (c.getOrder() >= newOrder && c.getOrder() < oldOrder) {
                    c.setOrder(c.getOrder() + 1);
                }
            }
        }

        chapter.setOrder(newOrder);
        courseChapterRepository.saveAll(chapters);
        return ResponseEntity.ok("Chapter reordered successfully");
    }
} 