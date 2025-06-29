package com.executive_documentation.materials.dto;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
public class MaterialResponseDto {
    private Long id;

    private String name;

    private String units;

    private String standard;

    @Builder.Default
    private Map<String, String> certificates = new LinkedHashMap<>();
}
