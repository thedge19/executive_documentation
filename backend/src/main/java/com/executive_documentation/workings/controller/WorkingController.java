package com.executive_documentation.workings.controller;

import com.executive_documentation.workings.dto.WorkingRequestDto;
import com.executive_documentation.workings.dto.WorkingResponseDto;
import com.executive_documentation.workings.dto.WorkingUpdateDto;
import com.executive_documentation.workings.service.WorkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<WorkingResponseDto>> getAllWorksBySubObject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        log.info("Get all works for subObjectId: {}, page: {}, size: {}, sort: {}",
                id, page, size, Arrays.toString(sort));

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
            Page<WorkingResponseDto> worksPage = workingService.getAll(id, pageable);

            log.info("Found {} works out of {}",
                    worksPage.getNumberOfElements(),
                    worksPage.getTotalElements());

            log.info(worksPage.toString());

            return ResponseEntity.ok(worksPage);

        } catch (Exception e) {
            log.error("Error fetching works for subObjectId {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count-by-subobject")
    public ResponseEntity<Map<String, Long>> getWorksCountBySubObject() {
        return ResponseEntity.ok(workingService.getWorksCountBySubObject());
    }

    @GetMapping("/undone/{id}")
    public ResponseEntity<List<WorkingResponseDto>> getAllByPositiveDone(@PathVariable Long id) {
        log.info("Get All By Positive Done");
        return ResponseEntity.ok(workingService.getAllByPositiveDone(id));
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
}