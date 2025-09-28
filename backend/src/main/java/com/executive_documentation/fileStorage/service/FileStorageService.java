package com.executive_documentation.fileStorage.service;

import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
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

    public FileStorageResponse storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Path pdfDir = this.fileStorageLocation.resolve("pdf");
            if (!Files.exists(pdfDir)) {
                Files.createDirectories(pdfDir);
            }

            String newFileName = UUID.randomUUID() + "_" + fileName;
            Path targetLocation = pdfDir.resolve(newFileName);
            int pageCount = 0;

            // Создаем временный файл с уникальным именем в целевой директории
            Path tempFile = pdfDir.resolve("temp_" + UUID.randomUUID() + ".pdf");

            try (InputStream inputStream = file.getInputStream()) {
                // Сохраняем во временный файл
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                // Подсчет страниц
                int pages = countPdfPages(tempFile);
                log.info("Сейчас страниц {}", pages);

                // Проверяем, нужно ли добавить пустую страницу
                if (pages % 2 != 0) {
                    log.info("Документ содержит нечётное количество страниц ({}). Добавляем пустую страницу.", pages);
                    addBlankPageToFile(tempFile, pages);
                    pages++; // Увеличиваем счетчик страниц
                }

                log.info("А сейчас страниц {}", countPdfPages(tempFile));

                pageCount = pages / 2; // Преобразуем в количество листов

                // Переименовываем временный файл в постоянный
                Files.move(tempFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                Files.setPosixFilePermissions(targetLocation, PosixFilePermissions.fromString("rw-r--r--"));

            } finally {
                // Всегда удаляем временный файл
                if (Files.exists(tempFile)) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        log.warn("Не удалось удалить временный файл: {}", tempFile, e);
                    }
                }
            }

            return new FileStorageResponse(newFileName, pageCount);

        } catch (IOException | DocumentException ex) {
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

    private int countPdfPages(Path pdfFile) throws IOException {
        RandomAccessFileOrArray raf = null;
        PdfReader reader = null;

        try {
            raf = new RandomAccessFileOrArray(pdfFile.toString());
            reader = new PdfReader(raf, null);
            int pages = reader.getNumberOfPages();

            log.info("Страниц в документе: {}", pages);

            // Просто возвращаем количество страниц без проверки на чётность
            return pages;

        } catch (Exception e) {
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

    private void addBlankPageToFile(Path pdfFile, int originalPageCount) throws IOException, DocumentException {
        // Создаем временный файл для результата
        Path tempPdf = Files.createTempFile("pdf_with_blank_page", ".pdf");

        try {
            addBlankPage(pdfFile, tempPdf, originalPageCount);

            // Заменяем оригинальный файл
            Files.move(tempPdf, pdfFile, StandardCopyOption.REPLACE_EXISTING);

        } finally {
            // Всегда пытаемся удалить временный файл
            if (Files.exists(tempPdf)) {
                try {
                    Files.delete(tempPdf);
                } catch (IOException e) {
                    log.warn("Не удалось удалить временный файл: {}", tempPdf);
                }
            }
        }
    }

    private void addBlankPage(Path sourceFile, Path targetFile, int originalPageCount) throws IOException, DocumentException {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            reader = new PdfReader(sourceFile.toString());
            stamper = new PdfStamper(reader, new FileOutputStream(targetFile.toFile()));

            Rectangle pageSize = reader.getPageSize(originalPageCount);
            stamper.insertPage(originalPageCount + 1, pageSize);

            log.info("Добавлена пустая страница №{} размером {}x{}",
                    originalPageCount + 1, pageSize.getWidth(), pageSize.getHeight());

        } finally {
            if (stamper != null) {
                stamper.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
