package com.executive_documentation.acts.dto.registry;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SelectedActsRequestDto {
    private List<Long> actIds;
}
