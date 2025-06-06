package com.executive_documentation.registries.service;

import com.executive_documentation.registries.dto.RegistryDto;
import com.executive_documentation.registries.dto.RegistryResponseDto;
import com.executive_documentation.registries.model.Registry;

import java.io.IOException;
import java.util.List;

public interface RegistryService {
    Registry findRegistryOrNot(Long id);

    List<RegistryResponseDto> getAllByMonth(int monthId);

    void create(RegistryDto dto);

    void update(int monthId) throws IOException;

//    void updateNumberOfPages(long id, int numberOfSheets);

    void delete(Long id);
}
