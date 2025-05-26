package com.executive_documentation.acts.controller;

import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.service.ActService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
