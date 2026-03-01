package com.executive_documentation.materials.dto;

import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.LocalFileStorageService;
import com.executive_documentation.materials.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialMapper {
    private final LocalFileStorageService fileStorageService;

    public MaterialResponseDto toResponseDto(Material material) {
        return MaterialResponseDto.builder()
                .id(material.getId())
                .name(material.getName())
                .units(material.getUnits())
                .standard(material.getStandard())
                .certificates(mapCertificates(material))
                .build();
    }

    public Material requestDtoToEntity(MaterialRequestDto dto) {

        FileStorageResponse response = fileStorageService.storeFile(dto.getFile());

        return Material.builder()
                .name(dto.getName())
                .units(dto.getUnits())
                .standard(dto.getStandard())
                .path(response.fileName())
                .certificateName(dto.getCertificateName())
                .numberOfPages(response.pageCount())
                .author(dto.getAuthor())
                .build();
    }

    private Map<String, String> mapCertificates(Material material) {

        Map<String, String> certificates = new LinkedHashMap<>();
        certificates.put(material.getCertificateName(), fileStorageService.getMinioFileUrl(material.getPath()));

        return certificates;
    }
}
