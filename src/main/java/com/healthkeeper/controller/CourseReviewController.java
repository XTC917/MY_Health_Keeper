package com.healthkeeper.controller;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseProgress;
import com.healthkeeper.entity.CourseReview;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseProgressRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.CourseReviewRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course-reviews")
public class CourseReviewController {
    @Autowired
    CourseReviewRepository courseReviewRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseProgressRepository courseProgressRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CourseReview> reviews = courseReviewRepository.findByCourseId(course.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReview(@RequestBody CourseReview review, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(review.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 检查是否已经评价过
        if (courseReviewRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return ResponseEntity.badRequest().body("You have already reviewed this course");
        }

        review.setUserId(user.getId());
        review.setCourseId(course.getId());
        review.setCreatedAt(LocalDateTime.now());
        CourseReview savedReview = courseReviewRepository.save(review);

        // 更新课程评分
        updateCourseRating(course);

        return ResponseEntity.ok(savedReview);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody CourseReview updatedReview,
                                        Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this review");
        }

        if (updatedReview.getRating() != null) review.setRating(updatedReview.getRating());
        if (updatedReview.getContent() != null) review.setContent(updatedReview.getContent());
        review.setUpdatedAt(LocalDateTime.now());

        CourseReview savedReview = courseReviewRepository.save(review);

        // 更新课程评分
        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(review.getCourseId());
        updateCourseRating(course);

        return ResponseEntity.ok(savedReview);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this review");
        }

        CourseService courseService = new CourseService();
        Course course= courseService.getCourseById(review.getCourseId());
        courseReviewRepository.delete(review);

        // 更新课程评分
        updateCourseRating(course);

        return ResponseEntity.ok("Review deleted successfully");
    }

    @PostMapping("/{id}/helpful")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markHelpful(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        CourseReview savedReview = courseReviewRepository.save(review);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/course/{courseId}/statistics")
    public ResponseEntity<?> getCourseReviewStatistics(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseReview> reviews = courseReviewRepository.findByCourseId(course.getId());

        Map<String, Object> statistics = new HashMap<>();
        
        // 计算平均评分
        double averageRating = reviews.stream()
                .mapToDouble(CourseReview::getRating)
                .average()
                .orElse(0.0);
        statistics.put("averageRating", averageRating);

        // 计算评分分布
        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        CourseReview::getRating,
                        Collectors.counting()
                ));
        statistics.put("ratingDistribution", ratingDistribution);

        // 计算总评价数
        statistics.put("totalReviews", reviews.size());

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CourseReview> reviews = courseReviewRepository.findByUserId(user.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }

    private void updateCourseRating(Course course) {
        List<CourseReview> reviews = courseReviewRepository.findByCourseId(course.getId());
        double averageRating = reviews.stream()
                .mapToDouble(CourseReview::getRating)
                .average()
                .orElse(0.0);
        course.setRating(averageRating);
        courseRepository.save(course);
    }
} 