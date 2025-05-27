package com.executive_documentation.materials.controller;

import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.service.MaterialService;
import com.executive_documentation.workings.model.Working;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
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
    ResponseEntity<Page<MaterialResponseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        log.info("Get all Materials");

        try {
            // Создаем объект сортировки
            Sort sorting = Sort.by(
                    sort[0].contains(",") ?
                            sort[0].split(",")[0] :
                            sort[0]
            );

            if (sort[0].contains(",")) {
                sorting = sort[0].split(",")[1].equalsIgnoreCase("desc") ?
                        sorting.descending() :
                        sorting.ascending();
            }

            Pageable pageable = PageRequest.of(page, size, sorting);
            Page<MaterialResponseDto> worksPage = materialService.getAll(pageable);

            log.info("Found {} materials out of {}",
                    worksPage.getNumberOfElements(),
                    worksPage.getTotalElements());

            log.info(worksPage.toString());

            return ResponseEntity.ok(worksPage);

        } catch (Exception e) {
            log.error("Error fetching works for material: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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
