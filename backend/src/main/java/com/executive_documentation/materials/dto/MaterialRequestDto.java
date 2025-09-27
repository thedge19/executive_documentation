package com.executive_documentation.materials.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class MaterialRequestDto {
    @NotBlank(message = "Наименование материала обязательно")
    private String name;

    @NotBlank(message = "Единицы измерения обязательны")
    private String units;

    @NotBlank(message = "ГОСТ/ТУ обязателен")
    private String standard;

    @Transient
    private MultipartFile file;

    @NotBlank(message = "Данные о сертификате обязательны")
    private String certificateName;

    @NotBlank(message = "Владелец сертификата?")
    private String author;
}
