package com.healthkeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingScheduleDto {
    private Long id;
    private Long courseId;
    private String courseName;
    private String date;
    private String startTime;
    private boolean completed;
} 