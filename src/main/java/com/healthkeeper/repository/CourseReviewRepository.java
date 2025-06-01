package com.healthkeeper.repository;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseReview;
import com.healthkeeper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<CourseReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseIdAndRatingOrderByCreatedAtDesc(Long courseId, Double rating);

    List<CourseReview> findByCourseIdOrderByHelpfulCountDesc(Long courseId);

    long countByCourseId(Long courseId);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.courseId = ?1")
    Double findAverageRatingByCourseId(Long courseId);

    Page<CourseReview> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseReview> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseId(Long courseId);
}
