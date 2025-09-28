package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.act.ActLogDto;
import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.acts.dto.act.ActMapper;
import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.dto.entrance.EntranceControlMapper;
import com.executive_documentation.acts.dto.entrance.EntranceControlResponseDto;
import com.executive_documentation.acts.dto.materials.MaterialDto;
import com.executive_documentation.acts.dto.worklog.WorkLogDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.acts.repository.ExecutiveSchemaRepository;
import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.exception.ValidationException;
import com.executive_documentation.fileStorage.dto.FileStorageResponse;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.dto.MaterialQuantityDto;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.repository.MaterialRepository;
import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.repository.ProjectRepository;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.repository.WorkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActServiceImplementation implements ActService {
    private final ActMapper actMapper;
    private final ActRepository actRepository;
    private final ProjectRepository projectRepository;
    private final SubObjectRepository subObjectRepository;
    private final EntranceControlRepository entranceControlRepository;
    private final EntranceControlMapper entranceControlMapper;
    private final FileStorageService fileStorageService;
    private final ExecutiveSchemaRepository executiveSchemaRepository;
    private final WorkingRepository workingRepository;
    private final MaterialRepository materialRepository;

    private static final String CONTROL_ACT = "Акт результатов входного контроля МТР и оборудования №";
    private static final String EXECUTIVE_SCHEMA = "Исполнительная схема №";
    private static final String SETS_OF_RULES = " СП 48.13330.2019 «Организация строительства»; СП 49.13330.2010" +
            "«Безопасность труда в строительстве» ";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    @Value("${app.storage.base-url}") // Изменили с ${app.base-url}
    private String storageBaseUrl;

    @Override
    public ActResponseDto get(Long id) {

        Act act = actRepository.findById(id).orElseThrow();

        ActResponseDto dto = actMapper.actToActResponseDto(act);
        if (act.getExecutiveSchema() != null) {
            dto.setExecutiveSchemaUrl(act.getExecutiveSchema().getSchemaPath());
        }

        return dto;
    }

    @Override
    public List<ActResponseDto> getAll() {
        return actRepository.findAllByOrderByEndDateAscActNumberAsc()
                .stream()
                .map(act -> {
                    ActResponseDto dto = actMapper.actToActResponseDto(act);
                    if (act.getExecutiveSchema() != null) {
                        dto.setExecutiveSchemaUrl(
                                fileStorageService.getFilePublicUrl(act.getExecutiveSchema().getSchemaPath())
                        );
                    }
                    return dto;
                }).toList();
    }

    @Override
    public List<ActResponseDto> filterBySubObject() {
        return actRepository.findAllByOrderByActNumberAsc()
                .stream()
                .map(actMapper::actToActResponseDto)
                .collect(Collectors
                        .toList());
    }

    @Override
    public ExecutiveSchema getExecutiveSchema(long id) {
        return executiveSchemaRepository.findById(id).orElseThrow();
    }

    @Override
    public List<ExecutiveSchema> getExecutiveSchemasAsc() {

        return executiveSchemaRepository.findAllByOrderBySchemasActNumberAsc()
                .stream()
                .peek(schema -> {
                    String path = fileStorageService.getFilePublicUrl(schema.getSchemaPath());
                    schema.setSchemaPath(path);
                }).collect(Collectors.toList());
    }

    @Override
    public List<ExecutiveSchema> getExecutiveSchemasDesc() {

        return executiveSchemaRepository.findAllByOrderBySchemasActNumberDesc()
                .stream()
                .peek(schema -> {
                    String path = fileStorageService.getFilePublicUrl(schema.getSchemaPath());
                    schema.setSchemaPath(path);
                }).collect(Collectors.toList());
    }

    @Override
    public List<ExecutiveSchema> getExecutiveSchemasFilteredByName() {
        return executiveSchemaRepository.findAllByOrderBySchemasActNumberAsc();
    }

    @Override
    // Оптимизированная версия с жадной загрузкой
    public List<EntranceControlResponseDto> getAllEntranceControl() {
        return entranceControlRepository.findAllWithRelations()
                .stream()
                .map(entranceControlMapper::toResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public void create(Map<String, String> formData, MultipartFile file) {

        Act createdAct = new Act();

        Project project = projectRepository.findById(parseLong(formData.get("projectId"))).orElseThrow();
        createdAct.setProject(project);

        SubObject subObject = subObjectRepository.findById(parseLong(formData.get("subObjectId"))).orElseThrow();
        createdAct.setSubObject(subObject);

        Working working = workingRepository.findById(parseLong(formData.get("workId"))).orElseThrow();
        createdAct.setWorking(working);

        working.setDone(parseBigDecimal(formData.get("workDone")));

        String currentWorks = subObject.getName() + ": " + working.getName() + " - " + parseBigDecimal(formData.get("workDone")) + " " + working.getUnits();
        createdAct.setWorks(currentWorks);

        String actSequenceNumber = String.valueOf(actRepository.countBySubObject(subObject) + 1);
        actSequenceNumber = actSequenceNumber.length() == 1 ? "0" + actSequenceNumber : actSequenceNumber;
        String actNumber = subObject.getTitle() + "/" + actSequenceNumber;
        createdAct.setActNumber(actNumber);

        createdAct.setStartDate(parseDate(formData.get("startDate")));
        createdAct.setEndDate(parseDate(formData.get("endDate")));

        log.info("nextWorkId {}", formData.get("nextWorkId"));

        String nextWorking = "н/п";

        String nextWorkId = formData.get("nextWorkId"); // Сохраняем в переменную один раз
        log.info("nextWorkId {}", nextWorkId);
        if (!Objects.equals(nextWorkId, "null")) {
            nextWorking = subObject.getName() + ": " +
                    workingRepository.findById(parseLong(nextWorkId)).orElseThrow().getName();
        }

        createdAct.setNextWorks(nextWorking);

        String standard = working.getStandard().getName();
        String inAccordWith = project.getName() + "; " + SETS_OF_RULES + "; " + standard;
        createdAct.setInAccordWith(inAccordWith);
        createdAct.setWorkDone(parseBigDecimal(formData.get("workDone")));
        createdAct.setSubmittedDocuments(addSubmittedDocuments(formData, actNumber));

        List<MaterialDto> materials = extractMaterialsFromFormData(formData);

        String controlDate = formData.get("controlDate");

        if (!materials.isEmpty()) {
            addEntranceControl(createdAct, materials, controlDate);
        }

        StringBuilder addedMaterials = new StringBuilder();

        if (!materials.isEmpty()) {
            for (MaterialDto dto : materials) {
                Material material = materialRepository.findById(dto.getMaterialId()).orElseThrow();
                String quantity = String.valueOf(dto.getQuantity());
                addedMaterials.append(material.getName())
                        .append(" - ")
                        .append(quantity)
                        .append(" ")
                        .append(material.getUnits())
                        .append(", ")
                        .append(material.getCertificateName())
                        .append("; ");
            }
            createdAct.setMaterials(addedMaterials.substring(0, addedMaterials.length() - 2));
        }

        log.info("materials {}", createdAct.getMaterials());
        createdAct.setExecutiveSchema(addExecutiveSchema(actNumber, file));

        actRepository.save(createdAct);

        log.info("Акт: {}", createdAct);
    }

    @Transactional
    @Override
    public ActResponseDto actUpdate(long id, String works, MultipartFile file) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Act not found with id: " + id));

        act.setWorks(works);

        if (file != null) {
            act.setExecutiveSchema(addExecutiveSchema(act.getActNumber(), file));
        }

        return actMapper.actToActResponseDto(actRepository.save(act));
    }

    @Transactional
    @Override
    public void delete(Long actId) {
        Act act = actRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Act not found with id: " + actId));

        Working working = act.getWorking();
        working.setDone(BigDecimal.valueOf(0));

        // 1. Обрабатываем связанные EntranceControl (без удаления Material)
        List<EntranceControl> entranceControls = entranceControlRepository.findAllByAct(act);
        entranceControls.forEach(control -> {
            // Отвязываем Material перед удалением
            control.setMaterial(null);
            entranceControlRepository.save(control);
        });
        entranceControlRepository.deleteAll(entranceControls);

        // 2. Удаляем ExecutiveSchema и файл
        if (act.getExecutiveSchema() != null) {
            ExecutiveSchema schema = act.getExecutiveSchema();
            if (schema.getSchemaPath() != null) {
                deleteSchema(schema.getId());
            }
        }

        // 3. Удаляем сам Act
        actRepository.delete(act);
    }

    @Transactional
    @Override
    public void deleteSchema(long id) {
        ExecutiveSchema schema = executiveSchemaRepository.findById(id).orElseThrow(() -> new NotFoundException("ExecutiveSchema not found with id: " + id));

        actRepository.findByExecutiveSchema(schema).ifPresent(act -> act.setExecutiveSchema(null));

        executiveSchemaRepository.deleteById(id);
        fileStorageService.deleteFile(schema.getSchemaPath());
    }

    @Override
    public Map<String, Double> getActStats() {
        // Получаем количество актов по подобъектам
        Map<String, Long> actCounts = actRepository.countActsBySubObjectTitle().stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        // Получаем количество работ по подобъектам
        Map<String, Long> workCounts = workingRepository.countWorksBySubObjectTitle().stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        // Вычисляем процентное соотношение
        Map<String, Double> result = new HashMap<>();

        // Добавляем все подобъекты, которые есть в работах
        for (Map.Entry<String, Long> entry : workCounts.entrySet()) {
            String subObjectTitle = entry.getKey();
            Long workCount = entry.getValue();
            Long actCount = actCounts.getOrDefault(subObjectTitle, 0L);

            double percentage = workCount == 0 ? 0 : (actCount * 100.0 / workCount);
            result.put(subObjectTitle, percentage);
        }

        return result;
    }

    @Override
    public long getGlobalStats() {
        return 100 * actRepository.countAllActs() / workingRepository.countAllWorkings();
    }

    @Override
    public List<WorkLogDto> getWorkLog3() {

        List<ActLogDto> acts = actRepository.findAllByOrderByStartDateAscActNumberAsc()
                .stream()
                .map(ActMapper::actToActLogDto)
                .toList();
        List<LocalDate> workLogDates = new ArrayList<>();
        Map<LocalDate, Map<String, List<String>>> logRows = new HashMap<>();

        for (ActLogDto act : acts) {
            ArrayList<LocalDate> actDates = new ArrayList<>();
            for (LocalDate d = act.getStartDate(); !d.isAfter(act.getEndDate()); d = d.plusDays(1)) {
                actDates.add(d);
                if (!workLogDates.contains(d)) {
                    workLogDates.add(d);
                }
            }

            String subObjectName = act.getWorks().split(":", 2)[0].trim();
            double workDone = act.getWorkDone().doubleValue();

            String currentWork = getCurrentWork(act.getWorks())[0];

            double rowWorkDone = workDone / actDates.size();

            for (int k = 0; k < actDates.size(); k++) {
                LocalDate rowDate = actDates.get(k);

                if (actDates.size() > 1) {
                    if (k != actDates.size() - 1) {
                        workDone = workDone - rowWorkDone;
                    } else {
                        rowWorkDone = workDone;
                    }
                }
                String rowCurrentWork = currentWork + " - " + String.format("%.2f", rowWorkDone) + " " + getCurrentWork(act.getWorks())[1];

                if (logRows.get(rowDate) == null) {
                    Map<String, List<String>> row = new HashMap<>();
                    List<String> rowWorks = new ArrayList<>();
                    rowWorks.add(rowCurrentWork);
                    row.put(subObjectName, rowWorks);
                    logRows.put(actDates.get(k), row);
                } else {
                    if (logRows.get(rowDate).get(subObjectName) == null) {
                        List<String> rowWorks = new ArrayList<>();
                        rowWorks.add(rowCurrentWork);
                        logRows.get(rowDate).put(subObjectName, rowWorks);
                    } else {
                        logRows.get(rowDate).get(subObjectName).add(rowCurrentWork);
                    }
                }
            }
        }

        return getWorkLogs(workLogDates, logRows);
    }

    @Override
    public List<ActLogResponseDto> getWorkLog6() {
        List<Act> acts = actRepository.findAllByOrderByEndDateAscActNumberAsc();

        return acts.stream().map(ActMapper::actToActLogResponseDto).toList();
    }

    // Вспомогательные методы
    private void validateFile(MultipartFile file) {
        if (!Objects.requireNonNull(file.getContentType()).equalsIgnoreCase("application/pdf")) {
            throw new ValidationException("Only PDF files are allowed");
        }
    }

    private static Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long value: " + value, e);
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal value: " + value, e);
        }
    }

    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString +
                    ". Expected format: yyyy-MM-dd", e);
        }
    }

    private static String dateToString(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    private static List<MaterialQuantityDto> parseMaterials(String materialsJson) {
        if (materialsJson == null || materialsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Упрощенный парсинг JSON формата: [{"materialId":1,"quantity":10.5},...]
        try {
            List<MaterialQuantityDto> materials = new ArrayList<>();
            String jsonContent = materialsJson.trim();

            // Проверяем, что строка начинается с '[' и заканчивается ']'
            if (!jsonContent.startsWith("[") || !jsonContent.endsWith("]")) {
                throw new IllegalArgumentException("Invalid JSON array format");
            }

            // Удаляем внешние скобки
            jsonContent = jsonContent.substring(1, jsonContent.length() - 1).trim();

            // Разделяем элементы массива
            String[] items = jsonContent.split("\\s*},\\s*\\{");

            for (String item : items) {
                // Чистим каждый элемент
                item = item.replaceAll("[{}\"]", "").trim();
                if (item.isEmpty()) continue;

                Long materialId = null;
                BigDecimal quantity = null;

                // Разбираем пары key:value
                String[] pairs = item.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length != 2) continue;

                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    switch (key) {
                        case "materialId":
                            materialId = Long.parseLong(value);
                            break;
                        case "quantity":
                            quantity = new BigDecimal(value);
                            break;
                    }
                }

                if (materialId != null && quantity != null) {
                    materials.add(MaterialQuantityDto.builder()
                            .materialId(materialId)
                            .quantity(quantity)
                            .build());
                }
            }

            return materials;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse materials: " + e.getMessage(), e);
        }
    }

    private String addSubmittedDocuments(Map<String, String> formData, String actNumber) {
        List<String> submittedDocuments = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate endDate = parseDate(formData.get("endDate"));
        String formattedEndDate = endDate.format(formatter);

        if (Objects.equals(formData.get("executiveSchema"), "Есть")) {
            submittedDocuments.add(EXECUTIVE_SCHEMA + actNumber + " от " + formattedEndDate + " г.");
        }

        List<MaterialQuantityDto> materials = parseMaterials(formData.get("materials"));

        String formattedControlDate = !materials.isEmpty() ? parseDate(formData.get("controlDate")).format(formatter) : null;

        switch (materials.size()) {
            case 0:
                break;
            case 1:
                submittedDocuments.add(CONTROL_ACT + actNumber + " от " + formattedControlDate + " г.");
                break;
            default:
                for (int count = 0; count < materials.size(); count++) {
                    submittedDocuments.add(CONTROL_ACT + actNumber + "-" + (count + 1) + " от " + formattedControlDate + " г.");
                }
                break;
        }

        return String.join("; ", submittedDocuments);
    }

    private void addEntranceControl(Act act, List<MaterialDto> materialsList, String strControlDate) {

        int counter = 1;

        LocalDate controlDate = parseDate(strControlDate);

        for (MaterialDto dto : materialsList) {
            EntranceControl entranceControl = new EntranceControl();

            Long materialId = dto.getMaterialId();

            Material material = materialRepository.findById(materialId).orElse(null);

            log.info("Фйдишник материала {}", materialId);
            String controlActNumber = materialsList.size() == 1 ? act.getActNumber() : act.getActNumber() + "-" + counter;

            entranceControl.setAct(act);
            entranceControl.setControlNumber(controlActNumber);
            entranceControl.setDate(controlDate);
            entranceControl.setMaterial(material);
            entranceControl.setQuantity(dto.getQuantity());

            counter++;

            log.info("Added Entrance Control: {}", entranceControl);

            try {
                entranceControlRepository.save(entranceControl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private ExecutiveSchema addExecutiveSchema(String actNumber, MultipartFile file) {
        if (file != null && !file.isEmpty()) {

            validateFile(file); // Добавили валидацию файла
            log.info("Add ExecutiveSchema: {}", actNumber);

            // Сохранение нового файла
            FileStorageResponse response = fileStorageService.storeFile(file);

            // Создание новой схемы
            ExecutiveSchema schema = createNewSchema(actNumber, response.fileName());
            schema.setNumberOfPages(response.pageCount());

            executiveSchemaRepository.save(schema);

            log.info("Updated schema for Act number {} with file: {}", actNumber, response.fileName());

            return schema;
        } else {
            return null;
        }
    }

    private ExecutiveSchema createNewSchema(String actNumber, String fileName) {
        ExecutiveSchema schema = new ExecutiveSchema();
        schema.setSchemasActNumber(actNumber);
        schema.setSchemaPath(fileName);
        return schema;
    }

    private String[] getCurrentWork(String currentWork) {
        currentWork = currentWork.split(":", 2)[1].trim();

        String[] works = currentWork.split(" ");
        int worksSize = works.length;

        StringBuilder workString = new StringBuilder();
        String units = works[worksSize - 1];

        for (int j = 0; j < worksSize - 3; j++) {
            workString.append(works[j]);
            workString.append(" ");
        }

        return new String[]{workString.toString().trim(), units.trim()};
    }

    private List<WorkLogDto> getWorkLogs(List<LocalDate> workLogDates, Map<LocalDate, Map<String, List<String>>> logRows) {
        int i = 1;

        List<WorkLogDto> workLogs = new ArrayList<>();

        for (LocalDate d : workLogDates) {

            WorkLogDto workLog = new WorkLogDto();

            workLog.setWorkDate(d);
            Set<String> rowKeySet = logRows.get(d).keySet();
            StringBuilder workLogName = new StringBuilder();

            for (String rowKey : rowKeySet) {
                workLogName.append(rowKey).append(": ");
                for (String work : logRows.get(d).get(rowKey)) {
                    workLogName.append(work).append("; ");
                }
            }
            workLog.setName(workLogName.toString());
            workLog.setWorkLogNumber(i);

            workLogs.add(workLog);

            i++;
        }
        return workLogs;
    }

    public List<MaterialDto> extractMaterialsFromFormData(Map<String, String> formData) {
        Pattern pattern = Pattern.compile("materials\\[(\\d+)\\]\\.(materialId|quantity)");
        Map<Integer, MaterialDto> materialMap = new HashMap<>();

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.matches()) {
                int index = Integer.parseInt(matcher.group(1));
                String field = matcher.group(2);

                MaterialDto material = materialMap.getOrDefault(index, new MaterialDto());

                if ("materialId".equals(field)) {
                    material.setMaterialId(Long.valueOf(entry.getValue()));
                } else if ("quantity".equals(field)) {
                    material.setQuantity(new BigDecimal(entry.getValue()));
                }

                materialMap.put(index, material);
            }
        }

        return materialMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .filter(m -> m.getMaterialId() != null && m.getQuantity() != null)
                .collect(Collectors.toList());
    }
}
