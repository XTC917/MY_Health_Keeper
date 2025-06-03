package com.healthkeeper.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthkeeper.dto.CommentRequest;
import com.healthkeeper.dto.MomentDTO;
import com.healthkeeper.entity.*;
import com.healthkeeper.repository.CommentRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.MomentRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.SupabaseStorageService;
import com.healthkeeper.service.MomentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/moments")
public class MomentController {
    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    // 添加缺失的依赖注入
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private MomentService momentService;

    /*@GetMapping("/")
    public ResponseEntity<?> getAllMoments() {
        List<Moment> moments = momentRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(moments);
    }*///准备废掉这个

    @GetMapping("/friends")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getFriendMoments(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Long> friendIds = currentUser.getFriends().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        friendIds.add(currentUser.getId());

        List<Moment> moments = momentRepository.findByUserIdInOrderByCreatedAtDesc(friendIds);

        // 转换为 DTO 列表
        List<MomentDTO> momentDTOs = moments.stream()
                .map(MomentDTO::new)
                .toList();

        return ResponseEntity.ok(momentDTOs);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getMomentById(@PathVariable Long id) {
        Moment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        return ResponseEntity.ok(new MomentDTO(moment));
    }

    /*@PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createMoment(@RequestBody Moment moment, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        moment.setUser(user);
        Moment savedMoment = momentRepository.save(moment);
        return ResponseEntity.ok(savedMoment);
    }*/

    @PostMapping(value = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createMoment(
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "courseIds", required = false) String courseIdsJson,
            Authentication authentication
    ) {
        try {
            // 1. 获取当前用户
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 2. 创建动态对象
            Moment moment = new Moment();
            moment.setContent(content);
            moment.setUser(user);

            // 3. 处理文件上传
            if (files != null && files.length > 0) {
                List<MediaItem> mediaItems = Arrays.stream(files)
                        .map(file -> {
                            try {
                                String url = supabaseStorageService.uploadFile(file);
                                return new MediaItem(
                                        file.getContentType().startsWith("image") ? "image" : "video",
                                        url
                                );
                            } catch (Exception e) {
                                throw new RuntimeException("文件上传失败: " + e.getMessage());
                            }
                        })
                        .collect(Collectors.toList());
                moment.setMedia(mediaItems);
            }

            // 4. 处理课程关联
            if (courseIdsJson != null && !courseIdsJson.isEmpty()) {
                List<Long> courseIds = new ObjectMapper().readValue(
                        courseIdsJson,
                        new TypeReference<List<Long>>() {}
                );
                List<Course> courses = courseRepository.findAllById(courseIds);
                moment.setCourses(courses);
            }

            // 5. 保存动态到数据库
            momentRepository.save(moment);

            return ResponseEntity.ok("动态发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("发布失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateMoment(@PathVariable Long id, @RequestBody Moment updatedMoment,
                                        Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Moment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        if (!moment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this moment");
        }

        // Update fields
        if (updatedMoment.getContent() != null) moment.setContent(updatedMoment.getContent());
        if (updatedMoment.getImages() != null) moment.setImages(updatedMoment.getImages());

        Moment savedMoment = momentRepository.save(moment);
        return ResponseEntity.ok(savedMoment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteMoment(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Moment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        if (!moment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this moment");
        }

        momentRepository.delete(moment);
        return ResponseEntity.ok("Moment deleted successfully");
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likeMoment(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Moment moment = momentService.likeMoment(id, user.getId());
        System.out.println("点赞后 moment id: " + moment.getId());
        System.out.println("当前点赞人数: " + moment.getLikes().size());
        System.out.println("用户 ID 列表:");
        moment.getLikes().forEach(u -> System.out.println(" - " + u.getId() + ": " + u.getUsername()));


        return ResponseEntity.ok(new MomentDTO(moment));
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unlikeMoment(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Moment moment = momentService.unlikeMoment(id, user.getId());
        return ResponseEntity.ok(new MomentDTO(moment));
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @GetMapping("/my-moments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyMoments(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Moment> moments = momentRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(moments);
    }
    // 新增评论接口
    /*@PostMapping("/{momentId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addComment(
            @PathVariable Long momentId,
            @RequestBody CommentRequest commentRequest,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setUser(user);
        comment.setMoment(moment);
        commentRepository.save(comment);

        return ResponseEntity.ok("评论添加成功");
    }

    // 删除评论接口
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权删除此评论");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("评论删除成功");
    }*/


} 
