package com.executive_documentation.materials.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialResponseDto {
    private Long id;

    private String name;

    private String units;

    private String documents;

    private String author;

    private Integer numberOfPages;

    private String standard;

    private String certificateUrl; // Ссылка на скачивание сертификата
}
