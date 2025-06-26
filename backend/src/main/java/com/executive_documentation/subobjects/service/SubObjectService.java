package com.executive_documentation.subobjects.service;

import com.executive_documentation.subobjects.dto.SubObjectRequestDto;
import com.executive_documentation.subobjects.dto.SubObjectResponseDto;
import com.executive_documentation.subobjects.model.SubObject;

import java.util.List;

public interface SubObjectService {
    SubObject get(Long id);

    SubObjectResponseDto getSubObject(long subObjectId);

    List<SubObjectResponseDto> getAll();

    List<SubObjectResponseDto> getAllByProjectId(long id);

    SubObject create(SubObjectRequestDto dto);

    SubObject update(long id, SubObject subObject);

    void delete(long id);

    SubObject findSubObjectOrNot(long id);
}
