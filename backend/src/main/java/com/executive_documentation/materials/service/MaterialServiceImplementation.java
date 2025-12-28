package com.executive_documentation.materials.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.fileStorage.service.FileStorageService;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImplementation implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final FileStorageService fileStorageService;

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
//        Material existingMaterial = findMaterialOrThrow(id);
//        existingMaterial.setCertificate(addCertificate(file));
//        return existingMaterial;
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
}