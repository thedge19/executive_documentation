package com.executive_documentation.subobjects.service;

import com.executive_documentation.subobjects.dto.SubObjectRequestDto;
import com.executive_documentation.subobjects.model.SubObject;

import java.util.List;

public interface SubObjectService {
    SubObject get(Long id);

    SubObject getSubObject(long subObjectId);

    List<SubObject> getAll();

    List<SubObject> getAllByProjectId(long id);

    SubObject create(SubObjectRequestDto dto);

    SubObject update(long id, SubObject subObject);

    void delete(long id);

    SubObject findSubObjectOrNot(long id);
}
