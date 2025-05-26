package com.executive_documentation.materials.controller;

import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public List<MaterialResponseDto> getAll() {
        log.info("Get all Materials");
        return materialService.getAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Material> createMaterial(
            @RequestPart("material") Material material,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Material createdMaterial = materialService.create(material, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMaterial);
    }

    @PostMapping("/certificate/{id}")
    public void addCertificate(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        log.info("Certificate Material: {}", id);
        materialService.addCertificate(id, file);
    }

    @PatchMapping("/{id}")
    public Material update(@PathVariable long id,
                            @RequestBody Material material) {
        log.info("Update Material: {}", material.getName());
        Material materialUpdated = materialService.update(id, material);
        log.info("Update Material: {}", materialUpdated);
        return materialUpdated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete Material: {}", id);
        materialService.delete(id);
        log.info("Material with id: {} deleted", id);
    }
}
