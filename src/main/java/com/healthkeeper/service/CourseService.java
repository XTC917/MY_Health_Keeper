package com.healthkeeper.service;

import com.healthkeeper.dto.CourseRequest;
import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Course createCourse(CourseRequest courseRequest, String username) {
        System.out.println("Creating new course for user: " + username);
        User creator = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("未找到用户: " + username));

        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setDuration(courseRequest.getDuration());
        course.setVideoUrl(courseRequest.getVideoUrl());
        course.setTargetAudience(courseRequest.getTargetAudience());
        course.setDescription(courseRequest.getDescription());
        course.setCreatorId(creator.getId());
        course.setStatus("PUBLISHED");
        course.setIsPublished(true);
        course.setCategory(courseRequest.getCategory());
        course.setLevel(courseRequest.getLevel());

        System.out.println("Saving course with title: " + course.getTitle());
        Course savedCourse = courseRepository.save(course);
        System.out.println("Course saved with ID: " + savedCourse.getId());
        return savedCourse;
    }

    public List<Course> getUserCourses(String username) {
        User creator = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("未找到用户: " + username));
        return courseRepository.findByCreatorId(creator.getId());
    }

    public List<Course> getAllCourses() {
        System.out.println("Service: Getting all courses...");
        List<Course> courses = courseRepository.findAll();
        System.out.println("Service: Found " + courses.size() + " courses");
        return courses;
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("未找到课程: " + id));
    }
} 