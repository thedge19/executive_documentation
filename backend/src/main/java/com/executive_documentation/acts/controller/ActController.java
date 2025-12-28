package com.executive_documentation.acts.controller;

import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.dto.entrance.EntranceControlResponseDto;
import com.executive_documentation.acts.dto.registry.RegistryMapper;
import com.executive_documentation.acts.dto.registry.RegistryPeriodDto;
import com.executive_documentation.acts.dto.registry.RegistryRequestDto;
import com.executive_documentation.acts.dto.registry.SelectedActsRequestDto;
import com.executive_documentation.acts.dto.worklog.WorkLogDto;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.acts.pdf.service.ActPdfService;
import com.executive_documentation.acts.pdf.service.ControlLogPdfService;
import com.executive_documentation.acts.pdf.service.RegistryPdfService;
import com.executive_documentation.acts.pdf.service.WorkLogPdfService;
import com.executive_documentation.acts.service.ActService;
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
    private final WorkLogPdfService workLogPdfService;

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
        actService.create(formData, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/registries")
    public void getRegistry(@RequestBody
                            RegistryRequestDto registryRequestDto, HttpServletResponse response) throws DocumentException, IOException {

        RegistryPeriodDto dto = registryMapper.requestDtoToPeriodDto(registryRequestDto);
        registryPdfService.getPeriodList(dto, response);
    }

    @PostMapping("/registries/selected")
    public void generateSelectedActsPdf(@RequestBody SelectedActsRequestDto request, HttpServletResponse response) throws DocumentException, IOException {

        registryPdfService.getSelectedList(request, response);
    }

    @PatchMapping("/{id}")
    public ActResponseDto updateAct(
            @PathVariable Long id,
            @RequestParam("works") String works, // Добавляем параметр works
            @RequestParam(value = "file", required = false) MultipartFile file) { // file теперь необязательный

        log.info("Обновление акта № {} с works: {}", id, works);
        return actService.actUpdate(id, works, file);
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

    @GetMapping("/worklog")
    public List<WorkLogDto> getWorkLog3() {
        return actService.getWorkLog3();
    }

    @GetMapping("/worklog/6")
    public List<ActLogResponseDto> getWorkLog6() {
        return actService.getWorkLog6();
    }

    @GetMapping("/worklog/{section}/pdf")
    public void generateWorkLogPdf(
            @PathVariable int section,
            HttpServletResponse response) throws IOException {
        try {
            workLogPdfService.exportWorkLogToPdf(response, section);
        } catch (DocumentException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка генерации PDF");
        }
    }
}
