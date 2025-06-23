package com.executive_documentation.exception.service;

import com.executive_documentation.auth.model.AppUser;
import com.executive_documentation.auth.repository.UserRepository;
import com.executive_documentation.exception.dto.ErrorStats;
import com.executive_documentation.exception.model.ErrorLevel;
import com.executive_documentation.exception.model.ErrorLog;
import com.executive_documentation.exception.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorLogServiceImplementation implements ErrorLogService {
    private final ErrorLogRepository errorLogRepository;
    private final UserRepository userRepository;

    public void logError(String message, String stackTrace, String endpoint,
                         String method, ErrorLevel level, Authentication authentication) {
        AppUser user = null;
        if (authentication != null) {
            user = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        ErrorLog error = ErrorLog.builder()
                .message(message)
                .stackTrace(stackTrace)
                .endpoint(endpoint)
                .method(method)
                .level(level)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        errorLogRepository.save(error);
    }

    public List<ErrorLog> getAllErrors() {
        return errorLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public ErrorStats getErrorStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);
        LocalDateTime last7Days = now.minusDays(7);

        return ErrorStats.builder()
                .totalErrors(errorLogRepository.countAllErrors())
                .last24Hours(errorLogRepository.countErrorsSince(last24Hours))
                .last7Days(errorLogRepository.countErrorsSince(last7Days))
                .countByLevel(convertToLevelCountMap(errorLogRepository.countByErrorLevel()))
                .countByDay(convertToDayCountMap(errorLogRepository.countByDaySince(last7Days)))
                .mostCommonErrorMessage(getMostCommonMessage(errorLogRepository.findMostCommonMessage()))
                .mostFrequentEndpoint(getMostFrequentEndpoint(errorLogRepository.findMostFrequentEndpoint()))
                .build();
    }

    private Map<ErrorLevel, Long> convertToLevelCountMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        obj -> (ErrorLevel) obj[0],
                        obj -> (Long) obj[1]
                ));
    }

    private Map<LocalDate, Long> convertToDayCountMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        obj -> ((java.sql.Date) obj[0]).toLocalDate(),
                        obj -> (Long) obj[1]
                ));
    }

    private String getMostCommonMessage(List<Object[]> results) {
        if (results == null || results.isEmpty() || results.getFirst() == null || results.getFirst().length == 0) {
            return "No errors";
        }
        return (String) results.getFirst()[0];
    }

    private String getMostFrequentEndpoint(List<Object[]> results) {
        if (results == null || results.isEmpty() || results.getFirst() == null || results.getFirst().length == 0) {
            return "No endpoints";
        }
        return (String) results.getFirst()[0];
    }
}