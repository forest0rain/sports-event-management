package com.sports.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 * 提供安全的文件上传功能
 */
@Slf4j
@Service
public class FileUploadService {

    // 上传目录
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // 允许的文件类型（白名单）
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf"
    );

    // 允许的文件扩展名（白名单）
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "pdf"
    );

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传资质文件
     * @param file 上传的文件
     * @return 相对文件路径
     * @throws IOException IO异常
     */
    public String uploadQualificationFile(MultipartFile file) throws IOException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir, "qualifications");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成安全的文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String safeFilename = UUID.randomUUID().toString() + "." + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(safeFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("文件上传成功: {}", safeFilename);

        // 返回相对路径
        return "qualifications/" + safeFilename;
    }

    /**
     * 验证上传的文件
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名无效");
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 检查Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持图片(jpg/png/gif/webp)和PDF文件");
        }

        // 防止路径遍历攻击
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名（小写）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取文件的完整路径
     * @param relativePath 相对路径
     * @return 完整路径
     */
    public Path getFullPath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
    }

    /**
     * 判断是否为图片文件
     * @param filename 文件名
     * @return 是否为图片
     */
    public boolean isImageFile(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = getFileExtension(filename);
        return extension != null && Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(extension);
    }

    /**
     * 获取上传目录
     * @return 上传目录路径
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * 获取文件的Content-Type
     * @param filename 文件名
     * @return Content-Type
     */
    public String getContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String extension = getFileExtension(filename);
        if (extension == null) {
            return "application/octet-stream";
        }
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }
}
