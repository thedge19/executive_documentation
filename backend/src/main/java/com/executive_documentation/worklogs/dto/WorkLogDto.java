package com.executive_documentation.worklogs.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkLogDto {
    private Long id;

    private String workDate;

    private String name;

    private Integer workLogNumber;
}

