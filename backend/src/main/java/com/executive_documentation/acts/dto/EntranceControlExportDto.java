package com.executive_documentation.acts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntranceControlExportDto {
    private Long id;

    private String date;

    private String materials;

    private String documents;
}
