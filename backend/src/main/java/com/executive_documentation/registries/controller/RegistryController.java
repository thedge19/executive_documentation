package com.executive_documentation.registries.controller;

import com.executive_documentation.registries.dto.RegistryDto;
import com.executive_documentation.registries.dto.RegistryResponseDto;
import com.executive_documentation.registries.service.RegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/registries")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class RegistryController {

    private final RegistryService registryService;

    @GetMapping("/{monthId}")
    public List<RegistryResponseDto> getAllByMonth(@PathVariable int monthId) {
        return registryService.getAllByMonth(monthId);
    }

    @PostMapping
    public void create(@RequestBody RegistryDto dto) {
        log.info("Create registry: {}", dto);
        registryService.create(dto);
    }

    @PatchMapping("/{monthId}")
    public void update(@PathVariable int monthId) throws IOException {
        registryService.update(monthId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete registry: {}", id);
        registryService.delete(id);
    }
}
