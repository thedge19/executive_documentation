package com.executive_documentation.registries.service;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.acts.service.ActService;
import com.executive_documentation.exception.InternalErrorException;
import com.executive_documentation.exception.NotFoundException;
import com.executive_documentation.registries.dto.RegistryDto;
import com.executive_documentation.registries.dto.RegistryMapper;
import com.executive_documentation.registries.dto.RegistryResponseDto;
import com.executive_documentation.registries.model.Registry;
import com.executive_documentation.registries.repository.RegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistryServiceImplementation implements RegistryService {

    private final RegistryRepository registryRepository;
    private final ActRepository actRepository;
    private final ActService actService;
    private final EntranceControlRepository entranceControlRepository;
    private final RegistryMapper registryMapper;
    private final static String ENERGY = "ООО Энергомонтаж";

    @Override
    public Registry findRegistryOrNot(Long id) {
        return registryRepository.findById(id).orElseThrow(() -> new NotFoundException("Строка реестра не найдена"));
    }

    @Override
    public List<RegistryResponseDto> getAllByMonth(int monthId) {
        List<Registry> registries = registryRepository.findAllByOrderByAddingTimeAsc(monthId);
        return registries.stream().map(registryMapper::registryToRegistryResponseDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void create(RegistryDto dto) {

        if (dto.getActId() == null) {
            throw new InternalErrorException("Некорректные данные");
        }

        Act act = actService.findActOrThrow(dto.getActId());
        List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);

        long rowCounter = registryRepository.countByMonthId(dto.getMonthId());

        Registry registry = new Registry();
        if (rowCounter == 0) {
            registry.setDocumentName("Реестр исполнительной документации");
            registry.setDocumentNumber("б/н");
            registry.setDocumentAuthor(ENERGY);
            registry.setDocumentDate(LocalDate.of(2025, dto.getMonthId(), 28));
            registry.setMonthId(dto.getMonthId());
            registry.setNumberOfSheets(1);
            registry.setRowNumber(rowCounter + 1);
            registry.setAddingTime(LocalDateTime.now());

            registryRepository.save(registry);
        }


        Registry actRegistry = new Registry();
        assert act != null;

        String documentNumber = act.getActNumber();

        actRegistry.setMonthId(dto.getMonthId());
        actRegistry.setDocumentName("Акт освидетельствования скрытых работ: '" + act.getWorks() + "'");
        actRegistry.setDocumentNumber(documentNumber);
        actRegistry.setDocumentAuthor(ENERGY);
        actRegistry.setDocumentDate(act.getEndDate());
        actRegistry.setNumberOfSheets(1);
        actRegistry.setRowNumber(registryRepository.countByMonthId(dto.getMonthId()) + 1);
        actRegistry.setAddingTime(LocalDateTime.now());

        log.info("ACT {}", actRegistry);
        registryRepository.save(actRegistry);
        act.setInRegistry("in registry");

        Registry schemaRegistry = new Registry();

        schemaRegistry.setMonthId(dto.getMonthId());
        schemaRegistry.setDocumentName("Исполнительная схема: '" + act.getWorks() + "'");
        schemaRegistry.setDocumentNumber(documentNumber);
        schemaRegistry.setDocumentAuthor(ENERGY);
        schemaRegistry.setDocumentDate(act.getEndDate());
        schemaRegistry.setNumberOfSheets(1);
        schemaRegistry.setRowNumber(registryRepository.countByMonthId(dto.getMonthId()) + 1);
        schemaRegistry.setAddingTime(LocalDateTime.now());
        schemaRegistry.setCurrentActId(actRegistry.getId());

        registryRepository.save(schemaRegistry);

        if (!controls.isEmpty()) {
            for (EntranceControl control : controls) {
                Registry controlRegistry = new Registry();
                controlRegistry.setMonthId(dto.getMonthId());
                controlRegistry.setDocumentName("Акт результатов входного контроля МТР и оборудования "
                        + control.getMaterials());
                controlRegistry.setDocumentNumber(control.getControlNumber());
                controlRegistry.setDocumentAuthor(ENERGY);
                controlRegistry.setDocumentDate(control.getDate());
                controlRegistry.setNumberOfSheets(1);
                controlRegistry.setRowNumber(registryRepository.countByMonthId(dto.getMonthId()) + 1);
                controlRegistry.setAddingTime(LocalDateTime.now());
                controlRegistry.setCurrentActId(actRegistry.getId());


                registryRepository.save(controlRegistry);

                Registry certificateRegistry = getCertificateRegistry(dto, control, registryRepository
                        .countByMonthId(dto.getMonthId()));
                certificateRegistry.setCurrentActId(actRegistry.getId());

                registryRepository.save(certificateRegistry);
            }
        }
    }

    @Transactional
    @Override
    public void update(int monthId) throws IOException {
        List<Registry> registries = registryRepository.findAllByOrderByAddingTimeAsc(monthId);

        for (int i=0; i <registries.size(); i++) {
            if (i == 0) {
                registries.getFirst().setListInOrder(registries.getFirst().getNumberOfSheets());
            } else {
                registries.get(i).setListInOrder(registries.get(i-1).getListInOrder() + registries.get(i).getNumberOfSheets());
            }
        }
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Registry deletedRegistry = findRegistryOrNot(id);
        if (!deletedRegistry.getDocumentName().startsWith("Реестр")
                && !deletedRegistry.getDocumentName().startsWith("Общий")
                && !deletedRegistry.getDocumentName().startsWith("Журнал")) {
            Act act = actRepository.findByActNumber(deletedRegistry.getDocumentNumber().split(" ")[0]);
            act.setInRegistry(null);
            List<Registry> deletedRegistries = registryRepository.findAllByCurrentActId(id);
            registryRepository.deleteAll(deletedRegistries);
        }
        registryRepository.deleteById(id);
    }


    private static Registry getCertificateRegistry(RegistryDto dto, EntranceControl control, long row) {
        Registry certificateRegistry = new Registry();
        certificateRegistry.setMonthId(dto.getMonthId());

        String certificate = control.getDocuments();
        int delimiterIndex = certificate.indexOf("№");

        String certificateName = certificate.substring(0, delimiterIndex - 1);
        String certificateNumber = certificate.substring(delimiterIndex + 1, certificate.length() - 1);

        certificateRegistry.setDocumentName(certificateName);
        certificateRegistry.setDocumentNumber(certificateNumber);
        certificateRegistry.setDocumentAuthor(control.getAuthor());
        certificateRegistry.setDocumentDate(control.getDate());
        certificateRegistry.setNumberOfSheets(control.getControlSheetNumbers());
        certificateRegistry.setRowNumber(row + 1);
        certificateRegistry.setAddingTime(LocalDateTime.now());

        return certificateRegistry;
    }

    private int lastRow(Sheet sheet) {
        int rowNumber = 13;

        while (!Objects.equals(sheet.getRow(rowNumber), null)) {

            rowNumber++;
        }

        return rowNumber;
    }
}