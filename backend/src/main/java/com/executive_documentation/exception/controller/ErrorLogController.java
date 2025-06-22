package com.executive_documentation.exception.controller;

import com.executive_documentation.exception.dto.ErrorStats;
import com.executive_documentation.exception.model.ErrorLog;
import com.executive_documentation.exception.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/errors")
@RequiredArgsConstructor
public class ErrorLogController {
    private final ErrorLogService errorLogService;

    @GetMapping
    public ResponseEntity<List<ErrorLog>> getAllErrors() {
        return ResponseEntity.ok(errorLogService.getAllErrors());
    }

    @GetMapping("/stats")
    public ResponseEntity<ErrorStats> getErrorStats() {
        // Логика для статистики
        return ResponseEntity.ok(errorLogService.getErrorStats());
    }
}
