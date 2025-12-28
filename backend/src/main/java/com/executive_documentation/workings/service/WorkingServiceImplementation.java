package com.executive_documentation.workings.service;

import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.standard.model.Standard;
import com.executive_documentation.standard.repository.StandardRepository;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.dto.*;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final StandardRepository standardRepository;

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

    @Transactional
    @Override
    public int importFromExcel(Long subObjectId) throws IOException {
        log.info("Importing works from Excel for subObjectId: {}", subObjectId);

        SubObject subObject = subObjectRepository.findById(subObjectId)
                .orElseThrow(() -> new NotFoundException("SubObject not found with id: " + subObjectId));

        String filePath = "KOR.xlsx";
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Excel файл не найден: " + filePath);
        }

        List<Working> worksToSave = new ArrayList<>();
        Standard standard = standardRepository.findById(4L).orElse(null);

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Пропускаем заголовок, если есть
                if (row.getRowNum() == 0) continue;

                // Читаем данные из Excel
                Long excelSubObjectId = getLongValue(row.getCell(0)); // Столбец A
                String name = getStringValue(row.getCell(1));        // Столбец B
                String units = getStringValue(row.getCell(2));       // Столбец C
                BigDecimal quantity = getBigDecimalValue(row.getCell(3)); // Столбец D

                // Проверяем, что это нужный subObject
                if (excelSubObjectId != null && excelSubObjectId.equals(subObjectId)) {
                    if (name != null && !name.isEmpty() &&
                            units != null && !units.isEmpty() &&
                            quantity != null) {

                        Working work = Working.builder()
                                .name(name.trim())
                                .units(units.trim())
                                .quantity(quantity)
                                .done(BigDecimal.ZERO)
                                .unitPrice(BigDecimal.ZERO) // Можно задать дефолтное значение
                                .subObject(subObject)
                                .standard(standard)
                                .build();

                        worksToSave.add(work);
                    }
                }
            }
        }

        // Сохраняем все работы
        if (!worksToSave.isEmpty()) {
            workingRepository.saveAll(worksToSave);
            log.info("Imported {} works for subObjectId: {}", worksToSave.size(), subObjectId);
        }

        return worksToSave.size();
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        return new BigDecimal(value.replace(",", "."));
                    }
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Error parsing BigDecimal from cell: {}", e.getMessage());
            return null;
        }
    }
}
