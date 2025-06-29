package com.executive_documentation.materials.service;

import com.executive_documentation.materials.dto.MaterialRequestDto;
import com.executive_documentation.materials.dto.MaterialResponseDto;
import com.executive_documentation.materials.model.Material;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialService {
    MaterialResponseDto get(Long id);

    List<MaterialResponseDto> getAll();

    Material create(MaterialRequestDto dto);

    Material update(long id, MultipartFile file);

    void delete(long id);

    void deleteCertificate(long id);
}
