package com.executive_documentation.workings.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.dto.*;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WorkingServiceImplementation implements WorkingService {

    private final WorkingRepository workingRepository;
    private final SubObjectRepository subObjectRepository;
    private final WorkingMapper workingMapper;

    @Override
    public WorkingResponseDto get(Long id) {
        return workingMapper.toDto(findWorkingOrNot(id));
    }

    @Override
    public List<WorkingResponseDto> getAll(long id) {
        return workingRepository.findAllBySubObjectIdOrderByIdAsc(id).stream()
                .map(workingMapper::toDto).toList();
    }

    @Override
    public List<WorkingResponseDto> getAllByPositiveDone(long id) {
        return workingRepository.findAllBySubObjectId(id)
                .stream().map(workingMapper::toDto).toList();
    }

    @Override
    public BigDecimal getTotalAmountBySubObject(long subObjectId) {
        SubObject subObject = subObjectRepository.findById(subObjectId).orElse(null);
        return workingRepository.sumTotalAmountBySubObject(subObject);
    }

    @Transactional
    @Override
    public WorkingResponseDto create(WorkingRequestDto workingDto) {
        Working working = workingMapper.toEntity(workingDto);
        log.info("Created new working with id {}", working);
        return workingMapper.toDto(workingRepository.save(working));
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

    @Override
    public Map<String, FinancialStats> getFinancialStatsBySubObject() {
        List<Working> workings = workingRepository.findAll();

        return workings.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getSubObject().getTitle(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new FinancialStats(
                                        list.stream()
                                                .map(w -> w.getTotalAmount() != null ? w.getTotalAmount() : BigDecimal.ZERO)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                                        list.stream()
                                                .map(w -> w.getDoneAmount() != null ? w.getDoneAmount() : BigDecimal.ZERO)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                )
                        )
                ));
    }

    @Override
    public TotalFinancialStats getTotalFinancialStats() {
        List<Working> workings = workingRepository.findAll();

        BigDecimal totalDone = workings.stream()
                .map(w -> w.getDoneAmount() != null ? w.getDoneAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = workings.stream()
                .map(w -> w.getTotalAmount() != null ? w.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalDone = totalDone.add(totalDone.multiply(new BigDecimal("0.20")));
        totalAmount = totalAmount.add(totalAmount.multiply(new BigDecimal("0.20")));

        return new TotalFinancialStats(totalDone, totalAmount);
    }
}
