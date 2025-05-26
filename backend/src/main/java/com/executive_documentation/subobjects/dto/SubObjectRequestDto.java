package com.executive_documentation.subobjects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubObjectRequestDto {
    private String name;
    private String title;
    private Long projectId;  // Убедитесь, что это поле есть
}
