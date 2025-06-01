package com.healthkeeper.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    // 文件存储根目录（示例路径，可根据需求修改）
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        init();
    }

    // 初始化存储目录
    public void init() {
        try {
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
            Files.copy(file.getInputStream(), rootLocation.resolve(filename));
            return "/uploads/" + filename; // 返回相对路径或完整URL
        } catch (Exception e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage());
        }
    }
}