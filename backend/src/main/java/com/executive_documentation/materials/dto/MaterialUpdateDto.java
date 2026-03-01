package com.executive_documentation.materials.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialUpdateDto {

    @NotBlank(message = "Наименование материала обязательно")
    private String name;

    @NotBlank(message = "Единицы измерения обязательны")
    private String units;

    @NotBlank(message = "ГОСТ/ТУ обязателен")
    private String standard;

    private String author;

    private String certificateName;
}
