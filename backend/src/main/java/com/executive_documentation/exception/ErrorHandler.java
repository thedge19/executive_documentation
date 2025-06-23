package com.executive_documentation.exception;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.exception.model.ErrorLevel;
import com.executive_documentation.exception.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ErrorHandler {

    private final ErrorLogService errorLogService;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e, WebRequest request) {
        logError(e, request, ErrorLevel.WARNING);
        return Map.of(
                "error", "Объект не найден",
                "errorMessage", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(InternalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalErrorException(InternalErrorException e, WebRequest request) {
        logError(e, request, ErrorLevel.ERROR);
        return Map.of(
                "error", "Ошибка сервера",
                "errorMessage", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleFileStorageException(FileStorageException e, WebRequest request) {
        logError(e, request, ErrorLevel.ERROR);
        return Map.of(
                "error", "Ошибка обращения к хранилищу",
                "errorMessage", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException e, WebRequest request) {
        logError(e, request, ErrorLevel.WARNING);
        return Map.of(
                "error", "Ошибка валидации",
                "errorMessage", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllExceptions(Exception e, WebRequest request) {
        logError(e, request, ErrorLevel.CRITICAL);
        return Map.of(
                "error", "Внутренняя ошибка сервера",
                "errorMessage", "Произошла непредвиденная ошибка",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    private void logError(Exception ex, WebRequest request, ErrorLevel level) {
        String endpoint = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();

        errorLogService.logError(
                ex.getMessage(),
                Arrays.toString(ex.getStackTrace()),
                endpoint,
                method,
                level,
                SecurityContextHolder.getContext().getAuthentication()
        );

        log.error("Ошибка {} в {} {}: {}", level, method, endpoint, ex.getMessage(), ex);
    }
}