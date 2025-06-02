package com.executive_documentation.fileStorage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не удалось создать директорию для хранения файлов", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Неверное имя файла: " + fileName);
            }

            String newFileName = UUID.randomUUID() + "_" + fileName;
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось сохранить файл " + fileName, ex);
        }
    }

    public ResponseEntity<Resource> loadFileAsResource(String fileName) {
        log.info("Здесь");
        try {
            Resource resource = getResource(fileName);
            String originalFilename = extractOriginalFilename(fileName);

            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            String contentDisposition = String.format(
                    "inline; filename*=UTF-8''%s",
                    encodedFilename
            );


            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    private String extractOriginalFilename(String storedFilename) {
        // Извлекаем оригинальное имя файла после UUID
        int underscoreIndex = storedFilename.indexOf('_');
        return underscoreIndex > 0 ? storedFilename.substring(underscoreIndex + 1) : storedFilename;
    }

    public Resource getResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Файл не найден: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Файл не найден: " + fileName, ex);
        }
    }

    public boolean fileExists(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Проверяем, что файл существует и находится в разрешенной директории
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Файл не существует: " + fileName);
            }

            // Дополнительная проверка безопасности
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new RuntimeException("Попытка удалить файл вне разрешенной директории");
            }

            Files.delete(filePath);
            log.info("Файл успешно удален: {}", filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось удалить файл: " + fileName, ex);
        }
    }
}
