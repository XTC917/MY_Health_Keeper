package com.healthkeeper.dto;

import lombok.Data;

@Data
public class CourseEnrollmentResponse {
    private boolean success;
    private boolean enrolled;
    private int totalStudents;
    private Long courseId;
    private String courseTitle;
    private String message;

    public static CourseEnrollmentResponse success(boolean enrolled, int totalStudents, Long courseId, String courseTitle) {
        CourseEnrollmentResponse response = new CourseEnrollmentResponse();
        response.setSuccess(true);
        response.setEnrolled(enrolled);
        response.setTotalStudents(totalStudents);
        response.setCourseId(courseId);
        response.setCourseTitle(courseTitle);
        return response;
    }

    public static CourseEnrollmentResponse error(String message) {
        CourseEnrollmentResponse response = new CourseEnrollmentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
} 