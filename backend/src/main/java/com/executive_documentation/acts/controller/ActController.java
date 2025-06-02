package com.executive_documentation.acts.controller;

import com.executive_documentation.acts.dto.ActRequestDto;
import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.dto.EntranceControlResponseDto;
import com.executive_documentation.acts.service.ActService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public void createAct(
            @RequestPart("dto") ActRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        actService.create(dto, file);
    }

    @PatchMapping("/{id}")
    public ActResponseDto updateAct(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return actService.actUpdate(id, file);
    }
}
