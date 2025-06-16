package com.executive_documentation.materials.service;

import com.executive_documentation.exception.FileStorageException;
import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.exception.ValidationException;
import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.dto.MaterialMapper;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Certificate;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.repository.CertificateRepository;
import com.executive_documentation.materials.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Material material = findMaterialOrThrow(id);
        return materialMapper.toResponseDto(material);
    }

    @Override
    public Page<MaterialResponseDto> getAll(Pageable pageable) {
        return materialRepository.findAllByOrderByName(pageable)
                .map(material -> {
                    MaterialResponseDto dto = materialMapper.toResponseDto(material);
                    if (material.getCertificate() != null) {
                        dto.setCertificateUrl(
                                fileStorageService.getFilePublicUrl(material.getCertificate().getPath())
                        );
                    }
                    return dto;
                });
    }

    @Override
    public List<Material> getAllNotPageable() {
        return materialRepository.findAll();
    }

    @Transactional
    @Override
    public Material create(Material material, MultipartFile file) {
        validateMaterial(material);

        if (file != null && !file.isEmpty()) {
            Certificate certificate = createCertificate(file);
            material.setCertificate(certificate);
        }

        return materialRepository.save(material);
    }

    @Transactional
    @Override
    public Material update(long id, MultipartFile file) {
        Material existingMaterial = findMaterialOrThrow(id);
        existingMaterial.setCertificate(addCertificate(file));
        return existingMaterial;
    }

    @Transactional
    @Override
    public void delete(long id) {
        Material material = findMaterialOrThrow(id);
        deleteCertificateIfExists(material);
        materialRepository.delete(material);
    }

    @Transactional
    @Override
    public void addCertificate(long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        Material material = findMaterialOrThrow(id);
        deleteCertificateIfExists(material);

        Certificate certificate = createCertificate(file);
        material.setCertificate(certificate);
        materialRepository.save(material);
    }

    @Override
    @Transactional
    public void deleteCertificate(long id) {
        Material material = findMaterialOrThrow(id);
        Certificate certificate = material.getCertificate();
        fileStorageService.deleteFile(material.getCertificate().getPath());
        material.setCertificate(null);
        certificateRepository.delete(certificate);
    }

    // Вспомогательные методы
    private Material findMaterialOrThrow(long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material not found with id: " + id));
    }

    private Certificate createCertificate(MultipartFile file) {
        validateFile(file);

        FileStorageResponse response = fileStorageService.storeFile(file);
        Certificate certificate = new Certificate();
        certificate.setNumberOfPages(response.pageCount());
        certificate.setPath(response.fileName());
        return certificateRepository.save(certificate);
    }

    private void deleteCertificateIfExists(Material material) {
        if (material.getCertificate() != null) {
            try {
                fileStorageService.deleteFile(material.getCertificate().getPath());
                certificateRepository.delete(material.getCertificate());
            } catch (Exception e) {
                log.error("Failed to delete certificate file", e);
                throw new FileStorageException("Could not delete certificate file");
            }
        }
    }

    private void validateMaterial(Material material) {
        if (material.getName() == null || material.getName().isBlank()) {
            throw new ValidationException("Material name cannot be empty");
        }
    }

    private void validateFile(MultipartFile file) {
        if (!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")) {
            throw new ValidationException("Only PDF files are allowed");
        }
    }

    private void updateMaterialFields(Material existing, Material updated) {
        Optional.ofNullable(updated.getName()).ifPresent(existing::setName);
        Optional.ofNullable(updated.getUnits()).ifPresent(existing::setUnits);
        Optional.ofNullable(updated.getDocuments()).ifPresent(existing::setDocuments);
        Optional.ofNullable(updated.getStandard()).ifPresent(existing::setStandard);
        Optional.ofNullable(updated.getAuthor()).ifPresent(existing::setAuthor);
        Optional.ofNullable(updated.getNumberOfPages()).ifPresent(existing::setNumberOfPages);
    }

    private Certificate addCertificate(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            validateFile(file); // Добавили валидацию файла

            // Сохранение нового файла
            FileStorageResponse response = fileStorageService.storeFile(file);

            // Создание нового сертификата
            Certificate certificate = createNewCertificate(response.fileName());
            certificate.setNumberOfPages(response.pageCount());

            certificateRepository.save(certificate);

            log.info("Updated certificate with file: {}", response.fileName());

            return certificate;
        } else {
            return null;
        }
    }

    private Certificate createNewCertificate(String fileName) {
        Certificate certificate = new Certificate();
        certificate.setPath(fileName);
        return certificateRepository.save(certificate);
    }
}