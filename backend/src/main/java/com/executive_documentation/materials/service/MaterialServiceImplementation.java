package com.executive_documentation.materials.service;

import com.executive_documentation.exception.NotFoundException;
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

import java.io.FileOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImplementation implements MaterialService {

    private final MaterialRepository materialRepository;
    private final CertificateRepository certificateRepository;
    private final MaterialMapper materialMapper;
    private final FileStorageService fileStorageService;

    @Value("${app.base-url}") // Добавьте в application.properties: app.base-url=http://localhost:8080
    private String baseUrl;

    @Override
    public MaterialResponseDto get(Long id) {
        return materialMapper.toResponseDto(findMaterialOrNot(id));
    }

    @Override
    public Page<MaterialResponseDto> getAll(Pageable pageable) {
        return materialRepository.findAllByOrderByName(pageable)
                .map(material -> {
                    MaterialResponseDto dto = materialMapper.toResponseDto(material);
                    if (material.getCertificate() != null && material.getCertificate().getPath() != null) {
                        String fileUrl = baseUrl + "/files/" + material.getCertificate().getPath();
                        dto.setCertificateUrl(fileUrl);
                    }
                    return dto;
                });
    }

    @Transactional
    @Override
    public Material create(Material material, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(file);
            Certificate certificate = new Certificate();
            certificate.setPath(storedFileName);

            Certificate savedCertificate = certificateRepository.save(certificate);

            material.setCertificate(certificateRepository.save(savedCertificate));
        }

        return materialRepository.save(material);
    }

    @Transactional
    @Override
    public Material update(long id, Material material) {
        Material updatedMaterial = findMaterialOrNot(id);

        if (material.getName() != null) {
            updatedMaterial.setName(material.getName());
        }

        if (material.getUnits() != null) {
            updatedMaterial.setUnits(material.getUnits());
        }

        if (material.getDocuments() != null) {
            updatedMaterial.setDocuments(material.getDocuments());
        }

        if (material.getStandard() != null) {
            updatedMaterial.setStandard(material.getStandard());
        }

        if (material.getAuthor() != null) {
            updatedMaterial.setAuthor(material.getAuthor());
        }

        if (material.getNumberOfPages() != null) {
            updatedMaterial.setNumberOfPages(material.getNumberOfPages());
        }

        return updatedMaterial;
    }

    @Transactional
    @Override
    public void delete(long id) {
        findMaterialOrNot(id);
        materialRepository.deleteById(id);
    }

    @Override
    public Material findMaterialOrNot(long id) {
        return materialRepository.findById(id).orElseThrow(() -> new NotFoundException("Подобъект не найден"));
    }

    @Transactional
    @Override
    public void addCertificate(long id, MultipartFile file) {
        Material material = findMaterialOrNot(id);
        Certificate certificate = new Certificate();
        certificate.setMaterial(material);

        String PATH_FOLDER = "C:\\Users\\PC\\IdeaProjects\\AOSR\\AOSR\\act\\certificates\\";
        String path = PATH_FOLDER + material.getId() + ".pdf";
        certificate.setPath(path);

        try {
            // Creating an object of FileOutputStream class
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(file.getBytes());

            // Closing the connection
            fos.close();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        certificateRepository.save(certificate);

        log.info("Номер загруженного сертификата: {}", certificate.getId());

        material.setCertificate(certificate);
    }
}