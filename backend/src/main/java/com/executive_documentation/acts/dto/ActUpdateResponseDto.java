package com.executive_documentation.acts.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ActUpdateResponseDto {
    private String actNumber;

    private String works;

    private Long executiveSchemaId;
}
