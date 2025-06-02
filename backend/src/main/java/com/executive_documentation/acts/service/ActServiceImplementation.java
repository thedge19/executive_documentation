package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.*;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.acts.repository.ExecutiveSchemaRepository;
import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.service.ProjectService;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.service.SubObjectService;
import com.executive_documentation.workings.model.Working;
import com.executive_documentation.workings.service.WorkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActServiceImplementation  implements ActService {
    private final ActMapper actMapper;
    private final ActRepository actRepository;
    private final ProjectService projectService;
    private final SubObjectService subObjectService;
    private final WorkingService workingService;
    private final EntranceControlRepository entranceControlRepository;
    private final EntranceControlMapper entranceControlMapper;
    private final FileStorageService fileStorageService;
    private final ExecutiveSchemaRepository executiveSchemaRepository;

    private static final String SETS_OF_RULES = " СП 48.13330.2019 «Организация строительства»; СП 49.13330.2010" +
            "«Безопасность труда в строительстве» ";

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ActResponseDto get(Long id) {
        return actMapper.ActToActResponseDto(findActOrNot(id));
    }

    @Override
    public List<ActResponseDto> getAll() {

        return actRepository.findAllByOrderByEndDateAscActNumberAsc()
                .stream()
                .map(act -> {
                    ActResponseDto actResponseDto = actMapper.ActToActResponseDto(act);
                    if (act.getExecutiveSchema() != null && act.getExecutiveSchema().getSchemaPath() != null) {
                        String fileUrl = baseUrl + "/api/files/" + act.getExecutiveSchema().getSchemaPath();
                        actResponseDto.setExecutiveSchemaUrl(fileUrl);
                    }
                    return actResponseDto;
                }).toList();
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
    public void create(ActRequestDto requestDto, MultipartFile file) {
        log.info("Create Act {}", requestDto);

        Act createdAct = new Act();

        Project project = projectService.findProjectOrThrow(requestDto.getProjectId());
        createdAct.setProject(project);

        SubObject subObject = subObjectService.findSubObjectOrNot(requestDto.getSubObjectId());
        createdAct.setSubObject(subObject);

        Working working = workingService.findWorkingOrNot(requestDto.getWorkId());
        String currentWorks = working.getName() + " - " + requestDto.getWorkDone() + " " + working.getUnits();
        createdAct.setWorks(currentWorks);

        String actSequenceNumber = String.valueOf(actRepository.countBySubObject(subObject) + 1);
        actSequenceNumber = actSequenceNumber.length() == 1 ? "0" + actSequenceNumber : actSequenceNumber;
        String actNumber = subObject.getTitle() + "/" + actSequenceNumber;
        createdAct.setActNumber(actNumber);

        createdAct.setStartDate(requestDto.getStartDate());
        createdAct.setEndDate(requestDto.getEndDate());

        String nextWorking = requestDto.getNextWorkId() == null ?
                "н/п" :
                subObject.getName() + ": " +
                        workingService.findWorkingOrNot(requestDto.getNextWorkId()).getName();
        createdAct.setNextWorks(nextWorking);

        String standard = working.getStandard().getName();
        String inAccordWith = project.getName() + "; " + SETS_OF_RULES + "; " + standard;
        createdAct.setInAccordWith(inAccordWith);

        createdAct.setWorkDone(requestDto.getWorkDone());

        log.info("Create Act {}", createdAct);

    }

    @Transactional
    @Override
    public ActResponseDto actUpdate(long id, MultipartFile file) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Act not found with id: " + id));

        if (file != null && !file.isEmpty()) {
            // Удаляем старую схему и файл, если они существуют
            if (act.getExecutiveSchema() != null) {
                try {
                    fileStorageService.deleteFile(act.getExecutiveSchema().getSchemaPath());
                } catch (Exception e) {
                    log.warn("Failed to delete old schema file: {}", e.getMessage());
                }
                executiveSchemaRepository.delete(act.getExecutiveSchema());
            }

            // Сохраняем новый файл
            String storedFileName = fileStorageService.storeFile(file);

            // Создаем новую схему
            ExecutiveSchema schema = new ExecutiveSchema();
            schema.setSchemasActNumber(act.getActNumber());
            schema.setSchemaPath(storedFileName); // Сохраняем только имя файла

            // Сохраняем и связываем с актом
            executiveSchemaRepository.save(schema);
            act.setExecutiveSchema(schema);
        }

        return actMapper.ActToActResponseDto(actRepository.save(act));
    }


    public Act findActOrNot(long id) {
        return actRepository.findById(id).orElseThrow(() -> new NotFoundException("Акт найден"));
    }
}
