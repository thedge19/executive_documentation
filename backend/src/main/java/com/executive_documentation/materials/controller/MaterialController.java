package com.executive_documentation.materials.controller;

import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.dto.MaterialUpdateDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/materials")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/{id}")
    public MaterialResponseDto get(@PathVariable Long id) {
        log.info("Get Material by id: {}", id);
        MaterialResponseDto material = materialService.get(id);
        log.info("Get Material: {}", material);
        return material;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponseDto>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Material> createMaterial(
            @RequestPart("material") MaterialRequestDto materialDto,
            @RequestPart("file") MultipartFile file) {

        // Связываем файлы с соответствующими сертификатами

        materialDto.setFile(file);

        log.info("Create material: {}", materialDto);
        Material createdMaterial = materialService.create(materialDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMaterial);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MaterialResponseDto> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestPart("material") MaterialUpdateDto materialDto,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        log.info("Запрос на обновление материала с ID: {}", id);
        log.info("Данные для обновления: name={}, units={}, standard={}, author={}, certificateName={}",
                materialDto.getName(), materialDto.getUnits(), materialDto.getStandard(),
                materialDto.getAuthor(), materialDto.getCertificateName());

        if (file != null && !file.isEmpty()) {
            log.info("Получен новый файл: {}, размер: {} байт", file.getOriginalFilename(), file.getSize());
        } else {
            log.info("Новый файл не предоставлен, будет использован существующий");
        }

        MaterialResponseDto updatedMaterial = materialService.update(id, materialDto, file);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(@PathVariable Long id) {
        log.info("Delete Material: {}", id);
        materialService.delete(id);
        log.info("Material with id: {} deleted", id);
    }

    @DeleteMapping("/certificate/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCertificate(@PathVariable Long id) {
        log.info("Delete Certificate: {}", id);
        materialService.deleteCertificate(id);
        log.info("Certificate with id: {} deleted", id);
    }
}
