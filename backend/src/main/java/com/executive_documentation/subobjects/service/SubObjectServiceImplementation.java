package com.executive_documentation.subobjects.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.subobjects.dto.SubObjectMapper;
import com.executive_documentation.subobjects.dto.SubObjectRequestDto;
import com.executive_documentation.subobjects.dto.SubObjectResponseDto;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubObjectServiceImplementation implements SubObjectService {

    private final SubObjectRepository subObjectRepository;
    private final WorkingRepository workingRepository;

    @Override
    public SubObject get(Long id) {
        return subObjectRepository.findById(id).orElse(null);
    }

    @Override
    public SubObject getSubObject(long subObjectId) {
        return subObjectRepository.findById(subObjectId).orElse(null);
    }

    @Override
    public List<SubObjectResponseDto> getAllByProjectId(long id) {
        return subObjectRepository.findAllByProjectIdOrderByIdAsc(id)
                .stream()
                .map(SubObjectMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<SubObject> getAll() {
        return subObjectRepository.findAll();
    }


    @Transactional
    @Override
    public SubObject create(SubObjectRequestDto dto) {
        return subObjectRepository.save(SubObjectMapper.toEntity(dto));
    }

    @Transactional
    @Override
    public SubObject update(long id, SubObject subObject) {
        SubObject updatedSubObject = findSubObjectOrNot(id);

        if (subObject.getName() != null) {
            updatedSubObject.setName(subObject.getName());
        }

        if (subObject.getTitle() != null) {
            updatedSubObject.setTitle(subObject.getTitle());
        }

        return updatedSubObject;
    }

    @Transactional
    @Override
    public void delete(long id) {
        SubObject subObject = findSubObjectOrNot(id);

        // Удаляем все связанные работы
        workingRepository.deleteAllBySubObjectId(id);

        // Теперь можно безопасно удалить подобъект
        subObjectRepository.delete(subObject);
    }

    @Override
    public SubObject findSubObjectOrNot(long id) {
        return subObjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Подобъект не найден"));
    }
}
