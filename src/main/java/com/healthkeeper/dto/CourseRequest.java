package com.healthkeeper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "课程标题不能为空")
    private String title;

    @NotNull(message = "课程时长不能为空")
    @Positive(message = "课程时长必须大于0")
    private Integer duration;

    @NotBlank(message = "课程视频不能为空")
    private String videoUrl;

    @NotBlank(message = "适宜人群不能为空")
    private String targetAudience;

    @NotBlank(message = "课程简介不能为空")
    private String description;
} 