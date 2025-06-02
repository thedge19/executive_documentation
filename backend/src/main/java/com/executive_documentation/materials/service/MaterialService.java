package com.executive_documentation.materials.service;

import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialService {
    MaterialResponseDto get(Long id);

    Page<MaterialResponseDto> getAll(Pageable pageable);

    List<Material> getAllNotPageable();

    Material create(Material material, MultipartFile file);

    Material update(long id, Material material);

    void delete(long id);

    Material findMaterialOrNot(long id);

//    void addCertificate(long id, MultipartFile file);
}
