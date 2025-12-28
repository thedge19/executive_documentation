package com.executive_documentation.materials.controller;

import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.service.MaterialService;
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
    public Material update(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        log.info("Здесь");
        return materialService.update(id, file);
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
