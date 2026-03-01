package com.executive_documentation.materials.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.exception.ValidationException;
import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.LocalFileStorageService;
import com.executive_documentation.materials.dto.MaterialMapper;
import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.dto.MaterialUpdateDto;
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
    public MaterialResponseDto update(long id, MaterialUpdateDto dto, MultipartFile file) {
        Material existingMaterial = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material not found with id: " + id));

        log.info("Updating material with id: {}", id);
        log.info("Update data - name: {}, units: {}, standard: {}, author: {}, certificateName: {}",
                dto.getName(), dto.getUnits(), dto.getStandard(), dto.getAuthor(), dto.getCertificateName());

        // Обновляем основные поля
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            existingMaterial.setName(dto.getName());
        }

        if (dto.getUnits() != null && !dto.getUnits().isEmpty()) {
            existingMaterial.setUnits(dto.getUnits());
        }

        if (dto.getStandard() != null && !dto.getStandard().isEmpty()) {
            existingMaterial.setStandard(dto.getStandard());
        }

        // Обновляем автора (может быть null)
        existingMaterial.setAuthor(dto.getAuthor());

        // Обновляем название сертификата, если оно предоставлено
        if (dto.getCertificateName() != null && !dto.getCertificateName().isEmpty()) {
            existingMaterial.setCertificateName(dto.getCertificateName());
        }

        // Обработка файла
        if (file != null && !file.isEmpty()) {
            log.info("Processing new certificate file for material {}", existingMaterial.getName());

            // Удаляем старый файл, если он существует
            if (existingMaterial.getPath() != null) {
                try {
                    fileStorageService.deleteFile(existingMaterial.getPath());
                    log.info("Deleted old file: {}", existingMaterial.getPath());
                } catch (Exception e) {
                    log.warn("Could not delete old file: {}", e.getMessage());
                    // Продолжаем выполнение, даже если не удалось удалить старый файл
                }
            }

            // Валидируем новый файл
            validateFile(file);

            // Сохраняем новый файл
            FileStorageResponse response = fileStorageService.storeFile(file);

            existingMaterial.setPath(response.fileName());
            existingMaterial.setNumberOfPages(response.pageCount());

            log.info("New file saved: {}, pages: {}", response.fileName(), response.pageCount());
        } else {
            log.info("No new file provided, keeping existing file: {}", existingMaterial.getPath());
        }

        // Сохраняем обновленный материал
        log.info("Material with id {} successfully updated", id);

        // Маппим в ResponseDto и возвращаем
        return materialMapper.toResponseDto(existingMaterial);
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