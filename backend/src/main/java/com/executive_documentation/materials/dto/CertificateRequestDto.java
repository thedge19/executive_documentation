package com.executive_documentation.materials.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CertificateRequestDto {
    @NotBlank(message = "Название сертификата обязательно")
    private String name;

    @NotBlank(message = "Автор сертификата обязателен")
    private String author;

    // Файл будет обрабатываться отдельно
    private transient MultipartFile file;

    // Дополнительные поля, если нужны
    private String type;
    private String number;
    private String date;
}
