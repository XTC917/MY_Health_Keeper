package com.healthkeeper.repository;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCreatorId(Long creatorId);

    List<Course> findAllByOrderByCreatedAtDesc();

    List<Course> findByCategory(String category);

    List<Course> findByCategoryContainingIgnoreCase(String category);

    List<Course> findByLevel(String level);

    List<Course> findByIsPublishedTrue();

    List<Course> findByTitleContainingIgnoreCase(String title);
}
