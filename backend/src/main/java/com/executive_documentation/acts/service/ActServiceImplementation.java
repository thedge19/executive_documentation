package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.ActMapper;
import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.dto.EntranceControlMapper;
import com.executive_documentation.acts.dto.EntranceControlResponseDto;
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
import com.executive_documentation.projects.service.ProjectService;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.service.SubObjectService;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.repository.WorkingRepository;
import com.executive_documentation.workings.service.WorkingService;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActServiceImplementation implements ActService {
    private final ActMapper actMapper;
    private final ActRepository actRepository;
    private final ProjectService projectService;
    private final SubObjectService subObjectService;
    private final WorkingService workingService;
    private final EntranceControlRepository entranceControlRepository;
    private final EntranceControlMapper entranceControlMapper;
    private final FileStorageService fileStorageService;
    private final ExecutiveSchemaRepository executiveSchemaRepository;
    private final MaterialRepository materialRepository;

    private final static String CONTROL_ACT = "Акт результатов входного контроля МТР и оборудования №";
    private final static String EXECUTIVE_SCHEMA = "Исполнительная схема №";
    private static final String SETS_OF_RULES = " СП 48.13330.2019 «Организация строительства»; СП 49.13330.2010" +
            "«Безопасность труда в строительстве» ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    @Value("${app.storage.base-url}") // Изменили с ${app.base-url}
    private String storageBaseUrl;

    @Override
    public ActResponseDto get(Long id) {

        Act act = actRepository.findById(id).get();

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
    public ExecutiveSchema getExecutiveSchema(long id) {
        return executiveSchemaRepository.findById(id).get();
    }

    @Override
    public List<ExecutiveSchema> getExecutiveSchemas() {
        return executiveSchemaRepository.findAll();
    }

    @Override
    public List<EntranceControlResponseDto> getAllEntranceControl() {
        return entranceControlRepository
                .findAll()
                .stream()
                .map(entranceControlMapper::toResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public void create(Map<String, String> formData, MultipartFile file) {

        Act createdAct = new Act();

        Project project = projectService.findProjectOrThrow(parseLong(formData.get("projectId")));
        createdAct.setProject(project);

        SubObject subObject = subObjectService.findSubObjectOrNot(parseLong(formData.get("subObjectId")));
        createdAct.setSubObject(subObject);

        Working working = workingService.findWorkingOrNot(parseLong(formData.get("workId")));
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
                    workingService.findWorkingOrNot(parseLong(nextWorkId)).getName();
        }

        createdAct.setNextWorks(nextWorking);

        String standard = working.getStandard().getName();
        String inAccordWith = project.getName() + "; " + SETS_OF_RULES + "; " + standard;
        createdAct.setInAccordWith(inAccordWith);
        createdAct.setWorkDone(parseBigDecimal(formData.get("workDone")));
        createdAct.setSubmittedDocuments(addSubmittedDocuments(formData, actNumber));

        createdAct.setMaterials(getMaterials(formData));
        createdAct.setExecutiveSchema(addExecutiveSchema(actNumber, file));

        actRepository.save(createdAct);

        if (getMaterials(formData) != null) {
            addEntranceControl(createdAct, formData);
        }

        log.info("Акт: {}", createdAct);
    }

    @Transactional
    @Override
    public ActResponseDto actUpdate(long id, MultipartFile file) {
        Act act = findActOrThrow(id);
        String oldSubmittedDocuments = act.getSubmittedDocuments();
        String newSubmittedDocuments = "Исполнительная схема №" + act.getActNumber() + " от "
         + dateToString(act.getEndDate()) + " г.; " + oldSubmittedDocuments;
        act.setSubmittedDocuments(newSubmittedDocuments);
        act.setExecutiveSchema(addExecutiveSchema(act.getActNumber(), file));
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

    @Override
    public Act findActOrThrow(long id) {
        return actRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Act not found with id: " + id));
    }

    @Transactional
    @Override
    public void deleteSchema(long id) {
        ExecutiveSchema schema = executiveSchemaRepository.findById(id).orElseThrow(() -> new NotFoundException("ExecutiveSchema not found with id: " + id));

        Act act = actRepository.findByExecutiveSchema(schema).orElseThrow(() -> new NotFoundException("Act not found with id: " + id));

        act.setExecutiveSchema(null);
        executiveSchemaRepository.deleteById(id);
        fileStorageService.deleteFile(schema.getSchemaPath());
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

    private String getMaterials(Map<String, String> formData) {
        List<Long> materialIds = getMaterialIds(formData);
        List<BigDecimal> quantities = getMaterialQuantities(formData);

        if (materialIds.isEmpty() || quantities.isEmpty() || materialIds.size() != quantities.size()) {
            return null;
        }

        List<String> materialStrings = new ArrayList<>();
        for (int i = 0; i < materialIds.size(); i++) {
            Material material = materialRepository.findById(materialIds.get(i)).orElseThrow(); // Предполагается, что у вас есть materialService
            BigDecimal quantity = quantities.get(i);

            String materialString = material.getName() + " - " + quantity + " " + material.getUnits() + ", " + material.getDocuments();
            materialStrings.add(materialString);
        }

        return String.join("; ", materialStrings);
    }

    private void addEntranceControl(Act act, Map<String, String> formData) {

        List<Long> materialIds = getMaterialIds(formData);
        List<BigDecimal> materialQuantities = getMaterialQuantities(formData);

        List<Material> materials = materialRepository.findAllById(materialIds);
        int counter = 1;

        LocalDate controlDate = parseDate(formData.get("controlDate"));

        for (int count = 0; count < materials.size(); count++) {
            EntranceControl entranceControl = new EntranceControl();

            Material material = materials.get(count);

            String addedMaterial = material.getName() + " - " + materialQuantities.get(count) + " " + materials.get(count).getUnits();
            String addedDocuments = material.getDocuments();
            String controlActNumber = materials.size() == 1 ? act.getActNumber() : act.getActNumber() + "-" + counter;

            entranceControl.setAct(act);
            entranceControl.setProject(act.getProject());
            entranceControl.setSubObjectName(act.getSubObject().getName());
            entranceControl.setControlNumber(controlActNumber);
            entranceControl.setDate(controlDate);
            entranceControl.setMaterials(addedMaterial);
            entranceControl.setDocuments(addedDocuments);
            entranceControl.setStandard(material.getStandard());
            entranceControl.setControlSheetNumbers(material.getNumberOfPages());
            entranceControl.setAuthor(material.getAuthor());
            entranceControl.setMaterial(material);

            counter++;

            log.info("Added Entrance Control: {}", entranceControl);

            try {
                entranceControlRepository.save(entranceControl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private List<Long> getMaterialIds(Map<String, String> formData) {
        return parseMaterials(formData.get("materials")).stream()
                .map(MaterialQuantityDto::getMaterialId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<BigDecimal> getMaterialQuantities(Map<String, String> formData) {
        return parseMaterials(formData.get("materials")).stream()
                .map(MaterialQuantityDto::getQuantity)
                .filter(Objects::nonNull)
                .toList();
    }

    private ExecutiveSchema addExecutiveSchema(String actNumber, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            validateFile(file); // Добавили валидацию файла

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
}
