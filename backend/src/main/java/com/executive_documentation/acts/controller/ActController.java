package com.executive_documentation.acts.controller;

import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.dto.EntranceControlResponseDto;
import com.executive_documentation.acts.service.ActService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/acts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class ActController {
    private final ActService actService;

    @GetMapping("/{id}")
    public ActResponseDto get(@PathVariable Long id) {
        return actService.get(id);
    }

    @GetMapping
    public List<ActResponseDto> getAll() {
        return actService.getAll();
    }

    @GetMapping("/entrance")
    public List<EntranceControlResponseDto> getEntranceControls() {
        return actService.getAllEntranceControl();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ActResponseDto> createAct(
            @RequestParam Map<String, String> formData,
            @RequestPart(required = false) MultipartFile file) {
        log.info("Здесь");
        actService.create(formData, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ActResponseDto updateAct(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return actService.actUpdate(id, file);
    }

    @DeleteMapping("/{id}")
    public void deleteAct(@PathVariable Long id) {
        actService.delete(id);
    }
}
