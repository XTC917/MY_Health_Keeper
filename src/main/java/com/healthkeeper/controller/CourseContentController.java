package com.healthkeeper.controller;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseChapter;
import com.healthkeeper.entity.CourseContent;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseChapterRepository;
import com.healthkeeper.repository.CourseContentRepository;
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
@RequestMapping("/api/course-contents")
public class CourseContentController {
    @Autowired
    CourseContentRepository courseContentRepository;

    @Autowired
    CourseChapterRepository courseChapterRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<?> getChapterContents(@PathVariable Long chapterId) {
        CourseChapter chapter = courseChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        List<CourseContent> contents = courseContentRepository.findByChapterIdOrderByOrderAsc(chapter.getId());
        return ResponseEntity.ok(contents);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createContent(@RequestBody CourseContent content, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseChapter chapter = courseChapterRepository.findById(content.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(chapter.getCourseId());
        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to create content for this chapter");
        }

        // 设置内容顺序
        List<CourseContent> existingContents = courseContentRepository.findByChapterIdOrderByOrderAsc(chapter.getId());
        if (existingContents.isEmpty()) {
            content.setOrder(1);
        } else {
            content.setOrder(existingContents.get(existingContents.size() - 1).getOrder() + 1);
        }

        CourseContent savedContent = courseContentRepository.save(content);
        return ResponseEntity.ok(savedContent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateContent(@PathVariable Long id, @RequestBody CourseContent updatedContent,
                                         Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseContent content = courseContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        CourseChapter chapter = courseChapterRepository.findById(content.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseService courseService = new CourseService();
        Course course1= courseService.getCourseById(chapter.getCourseId());
        if (!course1.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this content");
        }

        if (updatedContent.getTitle() != null) content.setTitle(updatedContent.getTitle());
        if (updatedContent.getContent() != null) content.setContent(updatedContent.getContent());
        if (updatedContent.getContentType() != null) content.setContentType(updatedContent.getContentType());
        if (updatedContent.getContentUrl() != null) content.setContentUrl(updatedContent.getContentUrl());
        if (updatedContent.getOrder() != null) content.setOrder(updatedContent.getOrder());
        if (updatedContent.getDuration() != null) content.setDuration(updatedContent.getDuration());
        if (updatedContent.getIsFree() != null) content.setIsFree(updatedContent.getIsFree());

        CourseContent savedContent = courseContentRepository.save(content);
        return ResponseEntity.ok(savedContent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteContent(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseContent content = courseContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        CourseChapter chapter = courseChapterRepository.findById(content.getChapterId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        CourseService courseService = new CourseService();
        Course course1= courseService.getCourseById(chapter.getCourseId());

        if (!course1.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this content");
        }

        courseContentRepository.delete(content);
        return ResponseEntity.ok("Content deleted successfully");
    }

    @PutMapping("/{id}/reorder")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reorderContent(@PathVariable Long id, @RequestBody Integer newOrder,
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseContent content = courseContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        CourseChapter chapter = courseChapterRepository.findById(content.getChapterId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        CourseService courseService = new CourseService();
        Course course1= courseService.getCourseById(chapter.getCourseId());

        if (!course1.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to reorder this content");
        }

        List<CourseContent> contents = courseContentRepository.findByChapterIdOrderByOrderAsc(chapter.getId());

        // 重新排序
        int oldOrder = content.getOrder();
        if (newOrder > oldOrder) {
            // 向后移动
            for (CourseContent c : contents) {
                if (c.getOrder() > oldOrder && c.getOrder() <= newOrder) {
                    c.setOrder(c.getOrder() - 1);
                }
            }
        } else if (newOrder < oldOrder) {
            // 向前移动
            for (CourseContent c : contents) {
                if (c.getOrder() >= newOrder && c.getOrder() < oldOrder) {
                    c.setOrder(c.getOrder() + 1);
                }
            }
        }

        content.setOrder(newOrder);
        courseContentRepository.saveAll(contents);
        return ResponseEntity.ok("Content reordered successfully");
    }

    @GetMapping("/chapter/{chapterId}/free")
    public ResponseEntity<?> getFreeContents(@PathVariable Long chapterId) {
        CourseChapter chapter = courseChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        List<CourseContent> freeContents = courseContentRepository.findByChapterIdAndIsFreeTrueOrderByOrderAsc(chapter.getId());
        return ResponseEntity.ok(freeContents);
    }
} 