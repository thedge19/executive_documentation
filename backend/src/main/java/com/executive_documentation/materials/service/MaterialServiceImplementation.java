package com.executive_documentation.materials.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.exception.ValidationException;
import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.LocalFileStorageService;
import com.executive_documentation.materials.dto.MaterialMapper;
import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImplementation implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final LocalFileStorageService fileStorageService;

    @Value("${app.storage.base-url}")
    private String storageBaseUrl;

    @Override
    public MaterialResponseDto get(Long id) {
        Material material = materialRepository.findById(id).orElseThrow();
        return materialMapper.toResponseDto(material);
    }

    @Override
    public List<MaterialResponseDto> getAll() {
        return materialRepository.findAllByOrderByName()
                .stream()
                .map(materialMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Material create(MaterialRequestDto dto) {
        Material material = materialMapper.requestDtoToEntity(dto);
        return materialRepository.save(material);
    }

    @Transactional
    @Override
    public Material update(long id, MultipartFile file) {
        Material existingMaterial = materialRepository.findById(id).orElseThrow(() -> new NotFoundException("Material not found"));

        if (file != null && !file.isEmpty()) {
            if (existingMaterial.getPath() != null) {
                fileStorageService.deleteFile(existingMaterial.getPath());
            }

            validateFile(file);
            log.info("Add updated certificate for material {}", existingMaterial.getName());

            FileStorageResponse response = fileStorageService.storeFile(file);

            existingMaterial.setPath(response.fileName());
            existingMaterial.setNumberOfPages(response.pageCount());
        }



        return null;
    }

    @Transactional
    @Override
    public void delete(long id) {
        Material material = materialRepository.findById(id).orElseThrow();
        fileStorageService.deleteFile(material.getPath());
        materialRepository.delete(material);
    }

    @Transactional
    @Override
    public void deleteCertificate(long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + id));

        fileStorageService.deleteFile(material.getPath());
    }

    private void validateFile(MultipartFile file) {
        if (!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")) {
            throw new ValidationException("Only PDF files are allowed");
        }
    }
}