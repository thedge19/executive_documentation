package com.executive_documentation.materials.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MaterialRequestDto {
    @NotBlank(message = "Наименование материала обязательно")
    private String name;

    @NotBlank(message = "Единицы измерения обязательны")
    private String units;

    @NotBlank(message = "ГОСТ/ТУ обязателен")
    private String standard;

    // Список данных о сертификатах
    @NotNull(message = "Данные сертификатов обязательны")
    private List<CertificateRequestDto> certificates;
}
