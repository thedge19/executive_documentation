package com.executive_documentation.fileStorage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    private final String storageBaseUrl;  // Базовый URL хранилища (например, "http://storage:80")
    private final String storagePublicUrl;

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${app.storage.base-url}") String storageBaseUrl,
            @Value("${app.storage.public-url}") String storagePublicUrl) {

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.storageBaseUrl = storageBaseUrl;
        this.storagePublicUrl = storagePublicUrl;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не удалось создать директорию для временного хранения файлов", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        log.info("Attempting to store file: {}", file.getOriginalFilename());
        log.info("Storage location: {}", this.fileStorageLocation);
        try {
            log.info("Directory exists: {}", Files.exists(this.fileStorageLocation));
            log.info("Directory writable: {}", Files.isWritable(this.fileStorageLocation));
        } catch (Exception e) {
            log.error("Directory check failed", e);
        }

        try {
            // Проверка на ../ в имени файла
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            // Создаем поддиректорию pdf если нужно
            Path pdfDir = this.fileStorageLocation.resolve("pdf");
            if (!Files.exists(pdfDir)) {
                Files.createDirectories(pdfDir);
            }

            String newFileName = UUID.randomUUID() + "_" + fileName;
            Path targetLocation = pdfDir.resolve(newFileName);

            // Сохраняем файл с проверкой
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File saved to: {}", targetLocation);
            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + fileName, ex);
        }
    }

    // Загрузка файла как Resource (теперь файлы отдаёт Nginx, поэтому этот метод может не понадобиться)
    public ResponseEntity<Resource> loadFileAsResource(String fileName) {
        // Перенаправляем запрос к Nginx
        String fileUrl = storageBaseUrl + "/" + fileName;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, fileUrl)
                .build();
    }

    public String getFilePublicUrl(String fileName) {
        return fileName != null ? storagePublicUrl + "/pdf/pdf/" + fileName : null;
    }

    public String getStorageBaseUrl(String fileName) {
        return fileName != null ? storageBaseUrl + "/pdf/pdf/" + fileName : null;
    }

    // Генерация уникального имени файла
    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + StringUtils.cleanPath(originalFilename);
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try {
            // Проверка на ../ в имени файла
            if (fileName.contains("..")) {
                throw new SecurityException("Недопустимое имя файла: " + fileName);
            }

            // Получаем путь к файлу в поддиректории pdf
            Path filePath = this.fileStorageLocation.resolve("pdf").resolve(fileName).normalize();

            // Проверяем, что путь находится внутри разрешенной директории
            if (!filePath.startsWith(this.fileStorageLocation.resolve("pdf"))) {
                throw new SecurityException("Попытка доступа к файлу вне разрешенной директории");
            }

            // Проверяем существование файла
            if (!Files.exists(filePath)) {
                log.warn("Файл {} не существует, удаление не требуется", fileName);
            }

            // Удаляем файл
            Files.delete(filePath);
            log.info("Файл {} успешно удален", fileName);
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось удалить файл: " + fileName, ex);
        }
    }
}
