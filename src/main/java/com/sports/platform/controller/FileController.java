package com.sports.platform.controller;

import com.sports.platform.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{*filePath}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filePath) {
        try {
            Path fullPath = fileUploadService
                    .getFullPath(filePath)
                    .toAbsolutePath()
                    .normalize();

            Resource resource = new UrlResource(fullPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = fileUploadService.getContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline()
                                    .filename(fullPath.getFileName().toString(), StandardCharsets.UTF_8)
                                    .build()
                                    .toString()
                    )
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}