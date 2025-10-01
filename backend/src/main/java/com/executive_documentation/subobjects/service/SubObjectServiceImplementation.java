package com.executive_documentation.subobjects.service;

import com.executive_documentation.subobjects.dto.SubObjectMapper;
import com.executive_documentation.subobjects.dto.SubObjectRequestDto;
import com.executive_documentation.subobjects.dto.SubObjectResponseDto;
import com.executive_documentation.subobjects.dto.SubObjectUpdateDto;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    public SubObjectResponseDto getSubObject(long subObjectId) {
        SubObject subObject = subObjectRepository.findById(subObjectId).orElse(null);
        return SubObjectMapper.toResponseDto(subObject);
    }

    @Override
    public List<SubObjectResponseDto> getAllByProjectId(long id) {
        return subObjectRepository.findAllByProjectIdOrderByIdAsc(id)
                .stream()
                .map(SubObjectMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<SubObjectResponseDto> getAll() {
        return subObjectRepository.findAll()
                .stream()
                .map(SubObjectMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SubObject create(SubObjectRequestDto dto) {
        return subObjectRepository.save(SubObjectMapper.toEntity(dto));
    }

    @Transactional
    @Override
    public SubObject update(long id, SubObjectUpdateDto updateDto) {
        SubObject updatedSubObject = subObjectRepository.findById(id).orElse(null);

        if (updateDto.getName() != null) {
            assert updatedSubObject != null;
            updatedSubObject.setName(updateDto.getName());
        }

        if (updateDto.getTitle() != null) {
            assert updatedSubObject != null;
            updatedSubObject.setTitle(updateDto.getTitle());
        }

        return updatedSubObject;
    }

    @Transactional
    @Override
    public void delete(long id) {
        SubObject subObject = subObjectRepository.findById(id).orElse(null);

        // Удаляем все связанные работы
        workingRepository.deleteAllBySubObjectId(id);

        // Теперь можно безопасно удалить подобъект
        assert subObject != null;
        subObjectRepository.delete(subObject);
    }
}
