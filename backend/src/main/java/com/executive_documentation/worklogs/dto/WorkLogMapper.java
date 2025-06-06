package com.executive_documentation.worklogs.dto;

import com.executive_documentation.worklogs.model.WorkLog;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkLogMapper {

    public WorkLogDto logToLogDto(WorkLog workLog) {
        if (workLog == null) {
            return null;
        }

        WorkLogDto dto = new WorkLogDto();
        dto.setId(workLog.getId());
        dto.setName(workLog.getName());
        dto.setWorkLogNumber(workLog.getWorkLogNumber());

        // Преобразование LocalDate в строку (формат по умолчанию ISO-8601)
        if (workLog.getWorkDate() != null) {
            dto.setWorkDate(workLog.getWorkDate().toString());
        }

        return dto;
    }

    public WorkLog logDtoToLog(WorkLogDto dto) {
        if (dto == null) {
            return null;
        }

        WorkLog workLog = new WorkLog();
        workLog.setId(dto.getId());
        workLog.setName(dto.getName());
        workLog.setWorkLogNumber(dto.getWorkLogNumber());

        // Преобразование строки в LocalDate
        if (dto.getWorkDate() != null && !dto.getWorkDate().isEmpty()) {
            workLog.setWorkDate(LocalDate.parse(dto.getWorkDate()));
        }

        return workLog;
    }
}
