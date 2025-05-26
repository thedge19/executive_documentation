package com.executive_documentation.materials.dto;

import com.executive_documentation.materials.model.Material;
import org.springframework.stereotype.Component;

@Component
public class MaterialMapper {

    public MaterialResponseDto toResponseDto(Material material) {
        return MaterialResponseDto.builder()
                .id(material.getId())
                .name(material.getName())
                .units(material.getUnits())
                .documents(material.getDocuments())
                .author(material.getAuthor())
                .numberOfPages(material.getNumberOfPages())
                .standard(material.getStandard())
                .build();
    }
}
