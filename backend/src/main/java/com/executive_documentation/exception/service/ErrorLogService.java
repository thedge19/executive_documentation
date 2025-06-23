package com.executive_documentation.exception.service;

import com.executive_documentation.exception.dto.ErrorStats;
import com.executive_documentation.exception.model.ErrorLevel;
import com.executive_documentation.exception.model.ErrorLog;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ErrorLogService {
    void logError(String message, String stackTrace, String endpoint,
                  String method, ErrorLevel level, Authentication authentication);

    List<ErrorLog> getAllErrors();

    ErrorStats getErrorStats();
}
