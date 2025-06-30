package com.executive_documentation.materials.dto;

import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.model.Certificate;
import com.executive_documentation.materials.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialMapper {
    private final FileStorageService fileStorageService;

    public MaterialResponseDto toResponseDto(Material material) {
        return MaterialResponseDto.builder()
                .id(material.getId())
                .name(material.getName())
                .units(material.getUnits())
                .standard(material.getStandard())
                .certificates(mapCertificates(material.getCertificates()))
                .build();
    }

    private Map<String, String> mapCertificates(Set<Certificate> certificates) {
        return certificates.stream()
                .collect(Collectors.toMap(
                        Certificate::getName,
                        cert -> fileStorageService.getFilePublicUrl(cert.getPath()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public Material requestDtoToEntity(MaterialRequestDto dto) {
        return Material.builder()
                .name(dto.getName())
                .units(dto.getUnits())
                .standard(dto.getStandard())
                .certificates(new HashSet<>())
                .build();
    }

    public void updateMaterialFromDto(MaterialRequestDto dto, Material material) {
        material.setName(dto.getName());
        material.setUnits(dto.getUnits());
        material.setStandard(dto.getStandard());
    }

    public Certificate toCertificateEntity(CertificateRequestDto certDto, Material material) {
        try {
            FileStorageResponse response = fileStorageService.storeFile(certDto.getFile());
            return Certificate.builder()
                    .name(certDto.getName())
                    .author(certDto.getAuthor())
                    .material(material)
                    .path(response.fileName())
                    .numberOfPages(response.pageCount())
                    .build();
        } catch (Exception e) {
            log.error("Failed to store certificate file", e);
            return null;
        }
    }
}
