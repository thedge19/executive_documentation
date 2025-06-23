package com.executive_documentation.acts.controller;

import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.dto.EntranceControlResponseDto;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.acts.pdf.ActPdfService;
import com.executive_documentation.acts.pdf.ControlLogPdfService;
import com.executive_documentation.acts.service.ActService;
import com.executive_documentation.registries.dto.RegistryMapper;
import com.executive_documentation.registries.dto.RegistryPeriodDto;
import com.executive_documentation.registries.dto.RegistryRequestDto;
import com.executive_documentation.registries.pdf.RegistryPdfService;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/acts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class ActController {
    private final ActService actService;
    private final ActPdfService actPdfService;
    private final ControlLogPdfService controlLogPdfService;
    private final RegistryMapper registryMapper;
    private final RegistryPdfService registryPdfService;

    @GetMapping("/{id}")
    public ActResponseDto get(@PathVariable Long id) {
        return actService.get(id);
    }

    @GetMapping
    public List<ActResponseDto> getAll() {
        return actService.getAll();
    }

    @GetMapping("/filterBySubObject")
    public List<ActResponseDto> filterBySubObject() {
        return actService.filterBySubObject();
    }

    @GetMapping("/schema/{id}")
    public ExecutiveSchema getSchema(@PathVariable Long id) {
        return actService.getExecutiveSchema(id);
    }

    @GetMapping("/schemaAsc")
    public List<ExecutiveSchema> getAllSchemaAsc() {
        return actService.getExecutiveSchemasAsc();
    }

    @GetMapping("/schemaDesc")
    public List<ExecutiveSchema> getAllSchemaDesc() {
        return actService.getExecutiveSchemasDesc();
    }

    @GetMapping("/schema/byName")
    public List<ExecutiveSchema> getAllSchemaFilteredByName() {
        return actService.getExecutiveSchemasFilteredByName();
    }

    @GetMapping("/entrance")
    public List<EntranceControlResponseDto> getEntranceControls() {
        return actService.getAllEntranceControl();
    }

    @GetMapping("/{id}/pdf")
    public void generateActPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        try {
            actPdfService.exportCombinedDocuments(id, response);
        } catch (DocumentException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка генерации PDF");
        }
    }

    @GetMapping("/pdf/controlLog")
    public void generateControlLogPdf(HttpServletResponse response) throws IOException {
        try {
            controlLogPdfService.exportEntranceControlLogToPdf(response);
        } catch (DocumentException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка генерации PDF");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ActResponseDto> createAct(
            @RequestParam Map<String, String> formData,
            @RequestPart(required = false) MultipartFile file) {
        log.info("startDate: {}, endDate: {}", formData.get("startDate"), formData.get("endDate"));
        actService.create(formData, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/registries")
    public void getRegistry(@RequestBody
                            RegistryRequestDto registryRequestDto, HttpServletResponse response) throws DocumentException, IOException {

        RegistryPeriodDto dto = registryMapper.requestDtoToPeriodDto(registryRequestDto);
        registryPdfService.createRegistryForPeriod(dto, response);
    }

    @PatchMapping("/{id}")
    public ActResponseDto updateAct(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return actService.actUpdate(id, file);
    }

    @DeleteMapping("/{id}")
    public void deleteAct(@PathVariable Long id) {
        actService.delete(id);
    }

    @DeleteMapping("/schema/{id}")
    void deleteSchema(@PathVariable Long id) {
        actService.deleteSchema(id);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Double>> getActStats() {
        return ResponseEntity.ok(actService.getActStats());
    }

    @GetMapping("/globalStats")
    public ResponseEntity<Long> getGlobalStats() {
        return ResponseEntity.ok(actService.getGlobalStats());
    }
}
