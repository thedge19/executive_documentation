package com.executive_documentation.worklogs.controller;

import com.executive_documentation.acts.dto.ActLogResponseDto;
import com.executive_documentation.worklogs.dto.WorkLogDto;
import com.executive_documentation.worklogs.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/worklog")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class WorkLogController {

    private final WorkLogService workLogService;

    @GetMapping
    public List<WorkLogDto> getWorkLog3() {
        return workLogService.getWorkLog3();
    }

    @GetMapping("/6")
    public List<ActLogResponseDto> getWorkLog6() {
        return workLogService.getWorkLog6();
    }

    @GetMapping("/fill3")
    void fillInTheLog3() {
        workLogService.fillInTheLog3();
    }
}