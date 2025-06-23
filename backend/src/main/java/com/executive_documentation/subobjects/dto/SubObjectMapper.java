package com.executive_documentation.subobjects.dto;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.subobjects.model.SubObject;

public class SubObjectMapper {

    public static SubObject toEntity(SubObjectRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return SubObject.builder()
                .name(dto.getName())
                .title(dto.getTitle())
                .project(Project.builder().id(dto.getProjectId()).build())
                .build();
    }

    public static SubObjectResponseDto toResponseDto(SubObject subObject) {
        if (subObject == null) {
            return null;
        }

        return SubObjectResponseDto.builder()
                .id(subObject.getId())
                .name(subObject.getName())
                .title(subObject.getTitle())
                .project(subObject.getProject() != null ?
                        Project.builder()
                                .id(subObject.getProject().getId())
                                .name(subObject.getProject().getName())
                                .build() :
                        null)
                .build();
    }
}
