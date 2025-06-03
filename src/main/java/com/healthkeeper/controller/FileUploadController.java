package com.healthkeeper.controller;

import com.healthkeeper.service.SupabaseStorageService;
import com.healthkeeper.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
} 