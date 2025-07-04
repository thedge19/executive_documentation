package com.executive_documentation.worklogs.controller;

import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.worklogs.dto.WorkLogDto;
import com.executive_documentation.worklogs.pdf.WorkLogPdfService;
import com.executive_documentation.worklogs.service.WorkLogService;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/worklog")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class WorkLogController {

    private final WorkLogService workLogService;
    private final WorkLogPdfService workLogPdfService;

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

    @GetMapping("/3/pdf")
    public void generateWorkLog3Pdf(HttpServletResponse response) throws IOException {
        try {
            workLogPdfService.exportWorkLogToPdf(response, 3);
        } catch (DocumentException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка генерации PDF");
        }
    }

    @GetMapping("/6/pdf")
    public void generateWorkLog6Pdf(HttpServletResponse response) throws IOException {
        try {
            workLogPdfService.exportWorkLogToPdf(response, 6);
        } catch (DocumentException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка генерации PDF");
        }
    }
}