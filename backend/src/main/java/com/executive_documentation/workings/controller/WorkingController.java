package com.executive_documentation.workings.controller;

import com.executive_documentation.workings.dto.*;
import com.executive_documentation.workings.service.WorkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/workings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class WorkingController {

    private final WorkingService workingService;

    @GetMapping("/working/{id}")
    public WorkingResponseDto get(@PathVariable Long id) {
        log.info("Get Working by id: {}", id);
        WorkingResponseDto dto = workingService.get(id);
        log.info("Get Working: {}", dto);
        return dto;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<WorkingResponseDto>> getAllWorksBySubObject(@PathVariable Long id) {
        List<WorkingResponseDto> works = workingService.getAll(id);
        return ResponseEntity.ok(works);
    }

    @GetMapping("/count-by-subobject")
    public ResponseEntity<Map<String, Long>> getWorksCountBySubObject() {
        return ResponseEntity.ok(workingService.getWorksCountBySubObject());
    }

    @GetMapping("/financial-stats")
    public Map<String, FinancialStats> getFinancialStats() {
        return workingService.getFinancialStatsBySubObject();
    }

    @GetMapping("/total-financial-stats")
    public ResponseEntity<TotalFinancialStats> getTotalFinancialStats() {
        return ResponseEntity.ok(workingService.getTotalFinancialStats());
    }

    @GetMapping("/undone/{id}")
    public ResponseEntity<List<WorkingResponseDto>> getAllByPositiveDone(@PathVariable Long id) {
        log.info("Get All By Positive Done");
        return ResponseEntity.ok(workingService.getAllByPositiveDone(id));
    }

    @GetMapping("/subobject/{subObjectId}/total-sum")
    public ResponseEntity<BigDecimal> getTotalAmountBySubObject(@PathVariable Long subObjectId) {
        return ResponseEntity.ok(workingService.getTotalAmountBySubObject(subObjectId));
    }

    @PostMapping
    public ResponseEntity<WorkingResponseDto> create(
            @RequestBody WorkingRequestDto workingDto) {
        log.info("Create Working: {}", workingDto);
        WorkingResponseDto workingCreated = workingService.create(workingDto);
        log.info("Created Working: {}", workingCreated);
        return ResponseEntity.ok(workingCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkingResponseDto> update(@PathVariable long id,
                          @RequestBody WorkingUpdateDto dto) {
        log.info("Update Working: {}", id);
        WorkingResponseDto workingUpdated = workingService.update(id, dto);
        log.info("Update Working: {}", workingUpdated);
        return ResponseEntity.ok(workingUpdated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete Working: {}", id);
        workingService.delete(id);
        log.info("Working with id: {} deleted", id);
    }

    @PostMapping("/import-excel/{subObjectId}")
    public ResponseEntity<String> importFromExcel(@PathVariable Long subObjectId) {
        log.info("Importing works from Excel for subObject: {}", subObjectId);
        try {
            int importedCount = workingService.importFromExcel(subObjectId);
            return ResponseEntity.ok("Успешно импортировано " + importedCount + " работ");
        } catch (Exception e) {
            log.error("Error importing from Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при импорте: " + e.getMessage());
        }
    }
}