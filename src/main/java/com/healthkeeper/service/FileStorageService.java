package com.healthkeeper.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${server.port}")
    private String serverPort;

    public FileStorageService() {
        init();
    }

    // 初始化存储目录
    public void init() {
        try {
            Path rootLocation = Paths.get(uploadDir);
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建文件存储目录");
        }
    }

    // 保存文件并返回访问路径
    public String saveFile(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回完整的URL
            return String.format("http://localhost:%s/api/files/%s", serverPort, filename);
        } catch (Exception e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage());
        }
    }
}