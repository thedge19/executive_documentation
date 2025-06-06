package com.executive_documentation.registries.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistryDto {

    private Long actId;

    private Integer monthId;
}
