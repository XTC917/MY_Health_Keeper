/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.service.impl;

import com.healthkeeper.dto.TrainingScheduleDto;
import com.healthkeeper.dto.TrainingScheduleRequest;
import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.TrainingSchedule;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.TrainingScheduleRepository;
import com.healthkeeper.security.SecurityUtil;
import com.healthkeeper.service.TrainingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingScheduleServiceImpl implements TrainingScheduleService {    private final TrainingScheduleRepository trainingScheduleRepository;
    private final CourseRepository courseRepository;
    private final SecurityUtil securityUtil;
    
    // 使用灵活的日期格式，能同时处理yyyy-M-d和yyyy-MM-dd格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
      // 尝试用多种格式解析日期
    private LocalDate parseDate(String dateString) {
        System.out.println("尝试解析日期: " + dateString);
        
        // 定义多种可能的日期格式
        DateTimeFormatter[] formatters = {
            DATE_FORMATTER,                           // yyyy-MM-dd
            DateTimeFormatter.ofPattern("yyyy-M-d"),  // yyyy-M-d
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),// yyyy/MM/dd
            DateTimeFormatter.ofPattern("yyyy/M/d")   // yyyy/M/d
        };
        
        // 检查日期字符串是否包含分隔符，如果不包含则可能是数字格式
        if (dateString != null && !dateString.contains("-") && !dateString.contains("/")) {
            try {
                // 尝试处理可能的时间戳
                long timestamp = Long.parseLong(dateString);
                return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (NumberFormatException e) {
                System.out.println("日期字符串不是有效的时间戳: " + e.getMessage());
            }
        }
        
        // 尝试所有可能的格式
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(dateString, formatter);
                System.out.println("成功解析日期: " + date);
                return date;
            } catch (Exception e) {
                System.out.println("使用格式 " + formatter + " 解析失败: " + e.getMessage());
            }
        }
        
        // 如果上面的方法都失败了，尝试手动解析 yyyy-M-d 格式
        try {
            String[] parts = dateString.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                
                LocalDate date = LocalDate.of(year, month, day);
                System.out.println("手动解析日期成功: " + date);
                return date;
            }
        } catch (Exception e) {
            System.out.println("手动解析日期失败: " + e.getMessage());
        }
        
        throw new RuntimeException("无法解析日期: " + dateString + "，请使用格式 yyyy-MM-dd");
    }

    @Override
    public Map<String, List<TrainingScheduleDto>> getUserSchedule() {
        User currentUser = securityUtil.getCurrentUser();
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUser(currentUser);
        
        return schedules.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getDate().format(DATE_FORMATTER),
                        Collectors.mapping(this::convertToDto, Collectors.toList())
                ));
    }

    @Override
    public List<TrainingScheduleDto> getDailySchedule(String date) {
        User currentUser = securityUtil.getCurrentUser();
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUserAndDate(currentUser, localDate);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }    @Override
    @Transactional
    public TrainingScheduleDto addScheduleItem(TrainingScheduleRequest request) {
        try {
            // 增加详细日志输出，便于调试
            System.out.println("接收到添加训练计划请求: " + request);
            System.out.println("课程ID: " + request.getCourseId());
            System.out.println("日期: " + request.getDate());
            System.out.println("开始时间: " + request.getStartTime());
            
            // 获取当前用户，添加错误处理
            User currentUser;
            try {
                currentUser = securityUtil.getCurrentUser();
                System.out.println("当前用户ID: " + currentUser.getId());
            } catch (Exception e) {
                System.err.println("获取当前用户失败: " + e.getMessage());
                throw new RuntimeException("用户认证失败或会话已过期，请重新登录", e);
            }
              // 查找课程，添加错误处理
            Course course;
            try {
                Long courseId = request.getCourseId();
                if (courseId == null) {
                    throw new RuntimeException("课程ID不能为空");
                }
                
                System.out.println("尝试查找课程ID: " + courseId + ", 类型: " + courseId.getClass().getName());
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("课程不存在，ID: " + courseId));
                System.out.println("找到课程: " + course.getTitle());
            } catch (NumberFormatException e) {
                System.err.println("课程ID格式不正确: " + e.getMessage());
                throw new RuntimeException("课程ID必须是数字", e);
            } catch (Exception e) {
                System.err.println("查找课程失败: " + e.getMessage());
                throw new RuntimeException("无法找到指定课程: " + e.getMessage(), e);
            }
            
            // 解析日期和时间，添加错误处理
            LocalDate date;
            LocalTime startTime;
            try {
                date = parseDate(request.getDate());
                System.out.println("解析后的日期: " + date);
                
                startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
                System.out.println("解析后的时间: " + startTime);
            } catch (Exception e) {
                System.err.println("解析日期或时间失败: " + e.getMessage());
                throw new RuntimeException("日期或时间格式不正确", e);
            }
              // 额外检查日期是否为空
            if (date == null) {
                throw new RuntimeException("日期解析后为空，无法保存训练计划");
            }
            
            // 确保时间不为空
            if (startTime == null) {
                throw new RuntimeException("时间解析后为空，无法保存训练计划");
            }
            
            // 创建训练计划对象前再次检查字段
            System.out.println("准备构建训练计划对象:");
            System.out.println("- 用户: " + (currentUser != null ? currentUser.getId() : "null"));
            System.out.println("- 课程: " + (course != null ? course.getId() : "null"));
            System.out.println("- 日期: " + date);
            System.out.println("- 时间: " + startTime);
              // 创建并保存训练计划
            LocalDate today = LocalDate.now();
            TrainingSchedule schedule = TrainingSchedule.builder()
                    .user(currentUser)
                    .course(course)
                    .date(date)        // schedule_date字段
                    .dateOriginal(date) // date字段，与schedule_date保持一致
                    .startTime(startTime)
                    .completed(false)
                    .createdAt(today)
                    .updatedAt(today)   // 添加updatedAt字段赋值
                    .build();
            
            // 再次验证对象状态
            if (schedule.getDate() == null) {
                throw new RuntimeException("构建训练计划对象后发现日期为空，字段映射可能有问题");
            }
            
            System.out.println("准备保存训练计划: " + schedule);
            
            // 在SQL执行前确认实体状态
            System.out.println("检查训练计划对象字段值:");
            System.out.println("- schedule.getId(): " + schedule.getId());
            System.out.println("- schedule.getUser().getId(): " + schedule.getUser().getId());
            System.out.println("- schedule.getCourse().getId(): " + schedule.getCourse().getId());            System.out.println("- schedule.getDate(): " + schedule.getDate());
            System.out.println("- schedule.getDate()类型: " + (schedule.getDate() != null ? schedule.getDate().getClass().getName() : "null"));
            System.out.println("- schedule.getDateOriginal(): " + schedule.getDateOriginal());
            System.out.println("- schedule.getDateOriginal()类型: " + (schedule.getDateOriginal() != null ? schedule.getDateOriginal().getClass().getName() : "null"));System.out.println("- schedule.getStartTime(): " + schedule.getStartTime());
            System.out.println("- schedule.isCompleted(): " + schedule.isCompleted());
            System.out.println("- schedule.getCreatedAt(): " + schedule.getCreatedAt());
            System.out.println("- schedule.getUpdatedAt(): " + schedule.getUpdatedAt());
            
            TrainingSchedule savedSchedule = trainingScheduleRepository.save(schedule);
            System.out.println("保存成功，ID: " + savedSchedule.getId());
            
            return convertToDto(savedSchedule);
        } catch (Exception e) {
            System.err.println("添加训练计划失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("添加训练计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public TrainingScheduleDto updateScheduleItem(Long itemId, TrainingScheduleRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
        
        // 确保只能修改自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权修改此训练计划");
        }
        
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            schedule.setCourse(course);
        }
          if (request.getDate() != null) {
            LocalDate parsedDate = LocalDate.parse(request.getDate(), DATE_FORMATTER);
            schedule.setDate(parsedDate);
            schedule.setDateOriginal(parsedDate); // 同时更新两个日期字段
        }
          if (request.getStartTime() != null) {
            schedule.setStartTime(LocalTime.parse(request.getStartTime(), TIME_FORMATTER));
        }
        
        // 更新 updatedAt 字段
        schedule.setUpdatedAt(LocalDate.now());
        
        TrainingSchedule updatedSchedule = trainingScheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    @Override
    @Transactional
    public void deleteScheduleItem(Long itemId) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
        
        // 确保只能删除自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权删除此训练计划");
        }
        
        trainingScheduleRepository.delete(schedule);
    }

    @Override
    public Map<String, List<TrainingScheduleDto>> getScheduleRange(String startDate, String endDate) {
        User currentUser = securityUtil.getCurrentUser();
        
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUserAndDateBetween(currentUser, start, end);
        
        return schedules.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getDate().format(DATE_FORMATTER),
                        Collectors.mapping(this::convertToDto, Collectors.toList())
                ));
    }

    @Override
    @Transactional
    public TrainingScheduleDto updateCompletionStatus(Long itemId, boolean completed) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
          // 确保只能更新自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权更新此训练计划");
        }
        
        schedule.setCompleted(completed);
        // 更新 updatedAt 字段
        schedule.setUpdatedAt(LocalDate.now());
        
        TrainingSchedule updatedSchedule = trainingScheduleRepository.save(schedule);
        
        return convertToDto(updatedSchedule);
    }
    
    private TrainingScheduleDto convertToDto(TrainingSchedule schedule) {
        return TrainingScheduleDto.builder()
                .id(schedule.getId())
                .courseId(schedule.getCourse().getId())
                .courseName(schedule.getCourse().getTitle())
                .date(schedule.getDate().format(DATE_FORMATTER))
                .startTime(schedule.getStartTime().format(TIME_FORMATTER))
                .completed(schedule.isCompleted())
                .build();
    }
}