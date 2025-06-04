/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "training_schedules")
public class TrainingSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    private boolean completed;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;
} 