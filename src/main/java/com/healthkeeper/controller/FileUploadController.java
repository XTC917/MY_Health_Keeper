package com.healthkeeper.controller;

import com.healthkeeper.service.SupabaseStorageService;
import com.healthkeeper.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private SupabaseConfig supabaseConfig;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = supabaseStorageService.uploadFile(file);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            String fileUrl = String.format("%s/storage/v1/object/public/%s/%s",
                supabaseConfig.getUrl(),
                supabaseConfig.getBucketName(),
                filename);
            supabaseStorageService.deleteFile(fileUrl);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("文件删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/default-avatar.png")
    public ResponseEntity<?> getDefaultAvatar() {
        try {
            // 从 resources 目录下读取默认头像
            Resource resource = new ClassPathResource("static/default-avatar.png");
            byte[] imageBytes = resource.getInputStream().readAllBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取默认头像失败: " + e.getMessage());
        }
    }
} 