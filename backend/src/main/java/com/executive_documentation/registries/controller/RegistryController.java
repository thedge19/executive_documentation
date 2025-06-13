package com.executive_documentation.registries.controller;

import com.executive_documentation.registries.dto.RegistryMapper;
import com.executive_documentation.registries.dto.RegistryPeriodDto;
import com.executive_documentation.registries.dto.RegistryRequestDto;
import com.executive_documentation.registries.pdf.RegistryPdfService;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "/registries")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class RegistryController {

//    private final RegistryService registryService;
    private final RegistryMapper registryMapper;
    private final RegistryPdfService registryPdfService;

//    @GetMapping("/{monthId}")
//    public List<RegistryResponseDto> getAllByMonth(@PathVariable int monthId) {
//        return registryService.getAllByMonth(monthId);
//    }

    @PostMapping
    public void getRegistry(@RequestBody
                            RegistryRequestDto registryRequestDto, HttpServletResponse response) throws DocumentException, IOException {

        RegistryPeriodDto dto = registryMapper.requestDtoToPeriodDto(registryRequestDto);
        registryPdfService.createRegistryForPeriod(dto, response);
    }

//    @PatchMapping("/{monthId}")
//    public void update(@PathVariable int monthId) throws IOException {
//        registryService.update(monthId);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        log.info("Delete registry: {}", id);
//        registryService.delete(id);
//    }
}
