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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.util.List;
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

    @Value("${app.base-url}") // Добавьте в application.properties: app.base-url=http://localhost:8080
    private String baseUrl;

    @Override
    public MaterialResponseDto get(Long id) {
        return materialMapper.toResponseDto(findMaterialOrNot(id));
    }

    @Override
    public List<MaterialResponseDto> getAll() {
        return materialRepository.findAllByOrderByName()
                .stream()
                .map(material -> {
                    MaterialResponseDto dto = materialMapper.toResponseDto(material);
                    // Добавляем ссылку на сертификат, если он есть
                    if (material.getCertificate() != null && material.getCertificate().getPath() != null) {
                        // Формируем полный URL для скачивания файла
                        String fileUrl = baseUrl + "/files/" + material.getCertificate().getPath();
                        dto.setCertificateUrl(fileUrl);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
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