package com.executive_documentation.materials.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.dto.MaterialMapper;
import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Certificate;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.repository.CertificateRepository;
import com.executive_documentation.materials.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImplementation implements MaterialService {

    private final MaterialRepository materialRepository;
    private final CertificateRepository certificateRepository;
    private final MaterialMapper materialMapper;
    private final FileStorageService fileStorageService;

    @Value("${app.storage.base-url}")
    private String storageBaseUrl;

    @Override
    public MaterialResponseDto get(Long id) {
        Material material = findMaterialWithCertificatesOrThrow(id);
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
        Material savedMaterial = materialRepository.save(material);

        Set<Certificate> certificates = dto.getCertificates().stream()
                .map(certDto -> materialMapper.toCertificateEntity(certDto, savedMaterial))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        certificateRepository.saveAll(certificates);
        savedMaterial.setCertificates(certificates);

        return savedMaterial;
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
        Material material = findMaterialWithCertificatesOrThrow(id);

        // Удаляем файлы сертификатов
        material.getCertificates().forEach(cert ->
                fileStorageService.deleteFile(cert.getPath()));

        certificateRepository.deleteAllByMaterialId(material.getId());
        materialRepository.delete(material);
    }

    @Transactional
    @Override
    public void deleteCertificate(long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + certificateId));

        fileStorageService.deleteFile(certificate.getPath());
        certificateRepository.delete(certificate);
    }

    // Вспомогательные методы
    private Material findMaterialWithCertificatesOrThrow(long id) {
        return materialRepository.findByIdWithCertificates(id)
                .orElseThrow(() -> new NotFoundException("Material not found with id: " + id));
    }

    private void updateMaterialFields(Material existing, Material updated) {
        Optional.ofNullable(updated.getName()).ifPresent(existing::setName);
        Optional.ofNullable(updated.getUnits()).ifPresent(existing::setUnits);
        Optional.ofNullable(updated.getStandard()).ifPresent(existing::setStandard);
    }
}