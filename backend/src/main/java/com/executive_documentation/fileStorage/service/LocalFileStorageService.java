package com.executive_documentation.fileStorage.service;

import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.x.file.storage.core.FileInfo;

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
public class LocalFileStorageService {

    private final Path fileStorageLocation;
    private final String storageBaseUrl;
    private final String storagePublicUrl;
    private final org.dromara.x.file.storage.core.FileStorageService xFileStorageService;
    private final MinioClient minioClient;

    public LocalFileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${app.storage.base-url}") String storageBaseUrl,
            @Value("${app.storage.public-url}") String storagePublicUrl,
            org.dromara.x.file.storage.core.FileStorageService xFileStorageService,
            MinioClient minioClient) {

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.storageBaseUrl = storageBaseUrl;
        this.storagePublicUrl = storagePublicUrl;
        this.xFileStorageService = xFileStorageService;
        this.minioClient = minioClient;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не удалось создать директорию для временного хранения файлов", ex);
        }
    }

    public FileStorageResponse storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            String newFileName = UUID.randomUUID() + "_" + fileName;
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);

            log.info(targetLocation.toString());

            int pageCount = 0;

            try (InputStream inputStream = file.getInputStream()) {
                // Сохраняем во временный файл
                Path tempFile = Files.createTempFile("pdf_", ".tmp");
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                // Подсчет страниц
                pageCount = countPdfPages(tempFile);
                log.info("Обработка файла: {}, размер: {} bytes, страниц: {}", fileName, file.getSize(), pageCount);

                // Загружаем в MinIO (через X-File-Storage) из временного файла
                try {
                    xFileStorageService.of(tempFile.toFile())
                            .setSaveFilename(newFileName)          // имя файла в bucket
                            .upload();
                    log.info("Файл успешно загружен в MinIO: {}", newFileName);
                } catch (Exception e) {
                    // Логируем ошибку, но не прерываем процесс – локальное сохранение всё равно выполняется
                    log.error("Ошибка при загрузке файла в MinIO: {}", e.getMessage(), e);
                }

                // Переносим временный файл в постоянное локальное хранилище
                Files.move(tempFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return new FileStorageResponse(newFileName, pageCount);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + fileName, ex);
        }
    }

    public String getFilePublicUrl(String fileName) {
        return fileName != null ? storagePublicUrl + "/pdf/" + fileName : null;
    }

    public String getStorageBaseUrl(String fileName) {
        return fileName != null ? storageBaseUrl + "/pdf/" + fileName : null;
    }

    public String getMinioFileUrl(String fileName) {
        if (fileName == null) return null;
        // Убираем префикс, если fileName уже содержит полный URL
        if (fileName.startsWith("http")) {
            // Извлекаем только имя файла из полного URL
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return "http://95.81.123.142:9000/executive-docs/pdf/" + fileName;
    }

    public void deleteFile(String fileName) {
        try {
            String cleanFileName = fileName;
            if (cleanFileName.startsWith("http")) {
                cleanFileName = cleanFileName.substring(cleanFileName.lastIndexOf("/") + 1);
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("executive-docs")
                            .object("pdf/" + cleanFileName)
                            .build()
            );

            log.info("✅ Файл {} удален из MinIO через прямой вызов", cleanFileName);

        } catch (Exception e) {
            log.error("Ошибка при прямом удалении из MinIO: {}", e.getMessage(), e);
        }
    }

    private int countPdfPages(Path pdfFile) throws IOException {
        RandomAccessFileOrArray raf = null;
        PdfReader reader = null;

        try {
            raf = new RandomAccessFileOrArray(pdfFile.toString());
            reader = new PdfReader(raf, null);
            int pages = reader.getNumberOfPages();

            // Проверка на чётность
            if (pages % 2 != 0) {
                throw new IllegalStateException(
                        "PDF должен содержать чётное количество страниц. Найдено: " + pages
                );
            }

            return pages / 2; // Возвращаем количество листов (каждый лист = 2 страницы)

        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw e; // Пробрасываем нашу проверку на чётность выше
            }
            log.warn("Failed to count PDF pages, assuming 1", e);
            return 1;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                log.warn("Error closing PDF resources", e);
            }
        }
    }
}