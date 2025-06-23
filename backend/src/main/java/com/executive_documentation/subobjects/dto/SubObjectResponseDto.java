package com.executive_documentation.subobjects.dto;

import com.executive_documentation.projects.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubObjectResponseDto {
    private Long id;
    private String name;
    private String title;
    private Project project;
}
