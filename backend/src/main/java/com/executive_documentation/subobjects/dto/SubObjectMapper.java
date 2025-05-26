package com.executive_documentation.subobjects.dto;

import com.executive_documentation.subobjects.model.SubObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubObjectMapper {

    SubObjectMapper INSTANCE = Mappers.getMapper(SubObjectMapper.class);

    @Mapping(target = "project.id", source = "projectId")
    SubObject toEntity(SubObjectRequestDto dto);
}
