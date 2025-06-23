package com.executive_documentation.workings.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.standard.model.Standard;
import com.executive_documentation.standard.repository.StandardRepository;
import com.executive_documentation.workings.dto.WorkingMapper;
import com.executive_documentation.workings.dto.WorkingRequestDto;
import com.executive_documentation.workings.dto.WorkingResponseDto;
import com.executive_documentation.workings.dto.WorkingUpdateDto;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WorkingServiceImplementation implements WorkingService {

    private final WorkingRepository workingRepository;
    private final StandardRepository standardRepository;
    private final WorkingMapper workingMapper;

    @Override
    public WorkingResponseDto get(Long id) {
        return workingMapper.toDto(findWorkingOrNot(id));
    }

    @Override
    public Page<WorkingResponseDto> getAll(long id, Pageable pageable) {
        return workingRepository.findAllBySubObjectIdOrderByIdAsc(id, pageable)
                .map(workingMapper::toDto);
    }

    @Override
    public List<WorkingResponseDto> getAllByPositiveDone(long id) {
        return workingRepository.findAllBySubObjectId(id)
                .stream().map(workingMapper::toDto).toList();
    }

    @Transactional
    @Override
    public Working create(WorkingRequestDto workingDto) {
        return workingRepository.save(workingMapper.toEntity(workingDto));
    }

    @Transactional
    @Override
    public WorkingResponseDto update(long id, WorkingUpdateDto dto) {
        Working updatedWorking = findWorkingOrNot(id);

        updatedWorking = workingMapper.updateDtoToEntity(dto, updatedWorking);

        return workingMapper.toDto(updatedWorking);
    }

    @Transactional
    @Override
    public void delete(long id) {
        findWorkingOrNot(id);
        workingRepository.deleteById(id);
    }

    @Override
    public Working findWorkingOrNot(long id) {
        return workingRepository.findById(id).orElseThrow(() -> new NotFoundException("Подобъект не найден"));
    }

    @Override
    public Map<String, Long> getWorksCountBySubObject() {
        return workingRepository.countWorksBySubObjectTitle().stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
    }
}
