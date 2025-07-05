package com.executive_documentation.acts.dto.worklog;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class WorkLogDto {
    private Integer workLogNumber;

    private LocalDate workDate;

    private String name;
}

