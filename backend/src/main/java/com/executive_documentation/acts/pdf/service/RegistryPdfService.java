package com.executive_documentation.acts.pdf.service;

import com.executive_documentation.acts.dto.act.ActMapper;
import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.dto.font.Fonts;
import com.executive_documentation.acts.dto.registry.RegistryPeriodDto;
import com.executive_documentation.acts.dto.registry.SelectedActsRequestDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.model.Registry;
import com.executive_documentation.acts.pdf.utils.PdfCellCreator;
import com.executive_documentation.acts.pdf.utils.PdfUtils;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.model.Material;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistryPdfService {
    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final AtomicInteger globalPageCounter = new AtomicInteger(0);

    private final ActRepository actRepository;
    private final EntranceControlRepository entranceControlRepository;
    private final ActPdfService actPdfService;
    private final ControlPdfService controlPdfService;
    private final ActMapper actMapper;
    private final FileStorageService fileStorageService;
    private final PdfCellCreator creator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final WorkLogPdfService workLogPdfService;
    private final ControlLogPdfService controlLogPdfService;

    private final static String ENERGY = "ООО «Энергомонтаж»";
    private final Path fileStorageLocation;
    @Value("${file.upload-dir}")
    String uploadDir;

    private Font f1;
    private Font f2;
    private Font f3;
    private Font fontForPageNumbers;

    @PostConstruct
    public void initFonts() {
        Fonts fonts = PdfUtils.initFonts();
        this.f1 = fonts.f1();
        this.f2 = fonts.f2();
        this.f3 = fonts.f3();
        this.fontForPageNumbers = fonts.fontForPageNumbers();
    }

    public void getPeriodList(RegistryPeriodDto periodDto, HttpServletResponse response) throws DocumentException, IOException {
        List<Act> acts = actRepository.findByEndDateBetween(
                periodDto.getStartDate(),
                periodDto.getEndDate()
        );

        createRegistry(acts, periodDto, response);
    }

    public void getSelectedList(SelectedActsRequestDto selectedDto, HttpServletResponse response) throws DocumentException, IOException {
        List<Act> acts = actRepository.findAllByIdInOrderByEndDateAscActNumberAsc(selectedDto.getActIds());

        Act firstAct = acts.getFirst();
        Act lastAct = acts.getLast();

        LocalDate startDate = firstAct.getEndDate();
        LocalDate endDate = lastAct.getEndDate();
        RegistryPeriodDto periodDto = createPeriodDtoFromSortedActs(startDate, endDate);
        createRegistry(acts, periodDto, response);
    }

    private void createRegistry(List<Act> acts, RegistryPeriodDto periodDto, HttpServletResponse response)
            throws IOException, DocumentException {

        // Сброс счетчика перед созданием нового реестра
        globalPageCounter.set(0);

        // 1. Создаем запись реестра
        Registry registryHeader = createRegistryHeader(periodDto);

        List<Registry> journalRegistries = createJournalRegistryEntries();

        ByteArrayOutputStream workLog3Pdf = workLogPdfService.generateWorkLogPdf(true, 3);
        ByteArrayOutputStream workLog6Pdf = workLogPdfService.generateWorkLogPdf(true, 6);
        ByteArrayOutputStream controlLogPdf = controlLogPdfService.generateControlLogPdf(true);

        // Обновляем количество страниц для каждого журнала
        for (Registry journal : journalRegistries) {
            if ("Общий журнал работ (раздел 3)".equals(journal.getDocumentName())) {
                journal.setNumberOfSheets(getPageCount(new ByteArrayInputStream(workLog3Pdf.toByteArray())) / 2);
            } else if ("Общий журнал работ (раздел 6)".equals(journal.getDocumentName())) {
                journal.setNumberOfSheets(getPageCount(new ByteArrayInputStream(workLog6Pdf.toByteArray())) / 2);
            } else if ("Журнал входного контроля".equals(journal.getDocumentName())) {
                journal.setNumberOfSheets(getPageCount(new ByteArrayInputStream(controlLogPdf.toByteArray())) / 2);
            }
        }

        // 3. Создаем итоговый PDF
        ByteArrayOutputStream mergedPdf = new ByteArrayOutputStream();
        Document mergedDoc = new Document();
        PdfCopy copy = new PdfSmartCopy(mergedDoc, mergedPdf);
        mergedDoc.open();

        try {
            // 3.1. Добавляем реестр

            ByteArrayOutputStream registryPdf = generateRegistryPdf(registryHeader, acts, journalRegistries);
            int registryPages = addDocumentToMerge(copy, new ByteArrayInputStream(registryPdf.toByteArray()));
            registryHeader.setNumberOfSheets(registryPages);

            // 3.2. Добавляем акты и связанные документы
            for (Act act : acts) {
                // Обработка акта
                Registry actRegistry = createActRegistryEntry(act);
                ActResponseDto dto = actMapper.actToActResponseDto(act);
                String filePath = act.getExecutiveSchema().getSchemaPath();
                dto.setExecutiveSchemaUrl(
                        filePath
                );


                ByteArrayOutputStream actPdf = actPdfService.generateActPdf(dto);
                int actPages = addDocumentToMerge(copy, new ByteArrayInputStream(actPdf.toByteArray()));
                actRegistry.setNumberOfSheets(actPages);

                // Обработка исполнительной схемы
                if (act.getExecutiveSchema().getSchemaPath() != null) {
                    Registry schemaRegistry = createSchemaRegistryEntry(act);
                    try {
                        log.info("HERE");
                        int schemaPages = addRemoteDocumentToMerge(copy,
                                fileStorageService.getFilePublicUrl(dto.getExecutiveSchemaUrl()));
                        schemaRegistry.setNumberOfSheets(schemaPages);
                    } catch (Exception e) {
                        log.warn("!!!Не удалось добавить исполнительную схему: {}", e.getMessage());
                    }
                }

                // Обработка входного контроля
                List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);
                for (EntranceControl control : controls) {
                    Registry controlRegistry = createControlRegistryEntry(control);

                    Material material = control.getMaterial();

                    ByteArrayOutputStream controlPdf = controlPdfService.generateControlPdf(control);
                    int controlPages = addDocumentToMerge(copy, new ByteArrayInputStream(controlPdf.toByteArray()));
                    controlRegistry.setNumberOfSheets(controlPages);

                    // Обработка сертификатов

                    if (control.getMaterial() != null) {
                        Registry certRegistry = createCertificateRegistryEntry(control);
                        try {
                            int certPages = addRemoteDocumentToMerge(copy, fileStorageService.getFilePublicUrl(material.getPath()));
                            log.info("СТраниц {}", certPages);
                            certRegistry.setNumberOfSheets(certPages);
                        } catch (Exception e) {
                            log.warn("!!!!!Не удалось добавить сертификат: {}", e.getMessage());
                        }
                    }
                }
            }

            // 3.3. Добавляем журналы в конце реестра
            addJournalDocuments(copy, journalRegistries);

            mergedDoc.close();

            // 4. Отправляем PDF клиенту
            response.setContentType("application/pdf");
            String filename = String.format("Реестр_%d_%d.pdf",
                    periodDto.getMonthId(), periodDto.getYear());
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            log.info("Имя файла {}", encodedFilename);

            response.setHeader("Content-Disposition",
                    "inline; filename=\"registry.pdf\"; filename*=UTF-8''" + encodedFilename);
            response.getOutputStream().write(mergedPdf.toByteArray());

        } catch (Exception e) {
            log.error("Ошибка при создании реестра: {}", e.getMessage());
            throw e;
        } finally {
            if (mergedDoc.isOpen()) mergedDoc.close();
            copy.close();
        }
    }

    private Registry createRegistryHeader(RegistryPeriodDto periodDto) {
        Registry registry = new Registry();
        registry.setDocumentName("Реестр исполнительной документации");
        registry.setDocumentNumber("б/н");
        registry.setDocumentAuthor(ENERGY);
        registry.setDocumentDate(LocalDate.of(periodDto.getYear(), periodDto.getMonthId(), 28));
        registry.setRowNumber(1L);
        registry.setAddingTime(LocalDateTime.now());
        return registry;
    }

    private ByteArrayOutputStream generateRegistryPdf(Registry registryHeader, List<Act> acts, List<Registry> journalRegistries) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = null;
        try {
            // 1. Инициализация PDF-документа
            writer = PdfWriter.getInstance(document, outputStream);
//            writer.setPageEvent(new GlobalPageNumberFooter(f1));
            document.open();

            // 2. Подготовка данных для таблицы

            List<Registry> registries = prepareRegistryData(registryHeader, acts, journalRegistries);

            // 3. Создаем временный PDF для подсчета страниц всего реестра
            ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
            Document tempDoc = new Document();
            PdfWriter.getInstance(tempDoc, tempStream);
            tempDoc.open();

            PdfPTable tempTable = new PdfPTable(6);
            tempTable.setWidthPercentage(105);
            float[] widths = new float[]{20f, 200f, 90f, 78f, 52f, 60f};
            tempTable.setWidths(widths);

            // Добавляем все элементы
            addPdfRegistryHeader(tempTable);
            addTableRegistryHeader(tempTable);

            // Добавляем все строки реестра
            for (Registry registry : registries) {
                tempTable.addCell(creator.createCell(String.valueOf(registry.getRowNumber()), "CB", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentName(), "CB", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentNumber(), "CB", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentAuthor(), "CB", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell("", "CB", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell("", "CB", f1, 1, 1, 0.0F));
            }

            addPdfFooter(tempTable);
            tempDoc.add(tempTable);
            tempDoc.close();

            // Получаем количество страниц всего реестра
            PdfReader reader = new PdfReader(new ByteArrayInputStream(tempStream.toByteArray()));
            int registryPages = reader.getNumberOfPages();
            reader.close();

            // 4. Добавляем пустую страницу если нужно

            // 5. Устанавливаем количество листов
            registries.getFirst().setNumberOfSheets((registryPages + 1) / 2); // Округляем вверх
            registries.getFirst().setListInOrder(1);

            // 6. Создаем основную таблицу
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(105);
            table.setWidths(widths);
            addRegistryTableData(table, registries);

            // 7. Добавляем в итоговый документ
            document.add(table);
            if (registryPages % 2 != 0) {
                document.newPage();
                document.add(new Paragraph(" "));
            }

        } finally {
            if (document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

        return outputStream;
    }

    private void addRegistryTableData(PdfPTable table, List<Registry> registries) {
        addPdfRegistryHeader(table);
        addTableRegistryHeader(table);

        int counter = 0;
        for (Registry registry : registries) {

            String documentNumber = registry.getDocumentNumber();

            table.addCell(creator.createCell(String.valueOf(registry.getRowNumber()), "CB", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(registry.getDocumentName()), "CB", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(documentNumber, "CB", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(registry.getDocumentAuthor(), "CB", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(registry.getNumberOfSheets()), "CB", f1, 1, 1, 0.0F));
            if (counter > 0) {
                registry.setListInOrder(registries.get(counter - 1).getListInOrder() + registries.get(counter - 1).getNumberOfSheets());
            }
            table.addCell(creator.createCell(String.valueOf(registry.getListInOrder()), "CB", f1, 1, 1, 0.0F));
            counter++;
        }

        addPdfFooter(table);
    }

    private void addPdfRegistryHeader(PdfPTable table) {
//      1 строка
        table.addCell(creator.createCell("", "CNB", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Форма", "lCNB", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("№1.2", "lCNB", f1, 2, 1, 0.0F));
//      2 строка
        table.addCell(creator.createCell("Заказчик: АО «Черномортранснефть»", "lCNB", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Основание", "lCNB", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("ВСН 012-88 (часть II)", "lCNB", f1, 2, 1, 0.0F));
//      3 строка
        table.addCell(creator.createCell("Подрядчик: ООО «Энергомонтаж»", "lTNB", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Строительство", "lTNB", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("14.295.24 ТЕКУЩИЙ РЕМОНТ ЗДАНИЙ И СООРУЖЕНИЙ ПК «ШЕСХАРИС»", "lCNB", f1, 2, 1, 0.0F));
//      4 строка
        table.addCell(creator.createCell("Субподрядчик:", "lTNB", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Объект: ", "lTNB", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("«Текущий ремонт зданий ПП «Грушовая» ПК «Шесхарис». " +
                "Текущий ремонт». «Текущий ремонт зданий ПП «Шесхарис». " +
                "ПК «Шесхарис». Текущий ремонт", "lTNB", f1, 2, 1, 0.0F));
//      Заголовок
        table.addCell(creator.createCell("РЕЕСТР", "cBNB", f1, 6, 1, 50F));
        table.addCell(creator.createCell("исполнительной  документации", "cTNB", f1, 6, 1, 30F));
    }

    private void addTableRegistryHeader(PdfPTable table) {
        table.addCell(creator.createCell("№ п/п", "CB", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Наименование документа", "CB", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("№ документа, дата", "CB", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Организация, составившая документ", "CB", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Кол-во листов", "CB", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Страница по списку", "CB", f2, 1, 1, 0.0F));
    }

    private void addPdfFooter(PdfPTable table) {
        table.addCell(creator.createCell("Сдал", "lBNB", f1, 2, 1, 50F));
        addRegistrySignature(table);
        table.addCell(creator.createCell("Принял", "lBNB", f1, 2, 1, 40F));
        addRegistrySignature(table);
    }

    private void addRegistrySignature(PdfPTable table) {
        table.addCell(creator.createCell("", "CBB", f1, 4, 1, 50F));
        table.addCell(creator.createCell("", "CNB", f1, 2, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, инициалы)", "cTNB", f3, 2, 1, 0.0F));
        table.addCell(creator.createCell("(подпись)", "cTNB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("(дата)", "cTNB", f3, 1, 1, 0.0F));
    }

    private List<Registry> prepareRegistryData(Registry registryHeader, List<Act> acts, List<Registry> journalRegistries) {
        List<Registry> registries = new ArrayList<>();


        // 1. Добавляем заголовок реестра
        registries.add(registryHeader);

        // 2. Добавляем акты и связанные документы
        for (Act act : acts) {
            // Создаем запись для акта
            Registry actRegistry = createActRegistryEntry(act);
            registries.add(actRegistry);

            // Добавляем исполнительную схему (если есть)
            if (act.getExecutiveSchema() != null) {
                log.info(act.getActNumber());
                Registry schemaRegistry = createSchemaRegistryEntry(act);
                registries.add(schemaRegistry);
            }
            // Добавляем акты входного контроля
            List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);
            for (EntranceControl control : controls) {
                Registry controlRegistry = createControlRegistryEntry(control);
                registries.add(controlRegistry);

                // Добавляем сертификат (если есть)

                if (control.getMaterial() != null && control.getMaterial().getPath() != null) {
            log.info(act.getActNumber());
                    Registry certRegistry = createCertificateRegistryEntry(control);
                    registries.add(certRegistry);
                }
            }
        }

        // 3.3. Добавляем журналы в конце реестра

        registries.addAll(journalRegistries);

        // 3. Обновляем номера строк
        for (int i = 0; i < registries.size(); i++) {
            registries.get(i).setRowNumber((long) (i + 1));
        }

        return registries;
    }

    private Registry createActRegistryEntry(Act act) {
        Registry registry = new Registry();
        registry.setDocumentName("Акт освидетельствования скрытых работ: «" + act.getWorks() + "»");
        String numberWithDate = act.getActNumber() + " от " + formatter.format(act.getEndDate()) + " г.";
        registry.setDocumentNumber(numberWithDate);
        registry.setDocumentAuthor(ENERGY);
        registry.setDocumentDate(act.getEndDate() != null ? act.getEndDate() : LocalDate.now());
        registry.setNumberOfSheets(1);
        registry.setAddingTime(LocalDateTime.now());
        registry.setCurrentActId(act.getId());
        return registry;
    }

    private Registry createSchemaRegistryEntry(Act act) {
        Registry registry = new Registry();
        registry.setDocumentName("Исполнительная схема: «" + act.getWorks() + "»");
        String numberWithDate = act.getActNumber() + " от " + formatter.format(act.getEndDate()) + " г.";
        registry.setDocumentNumber(numberWithDate);
        registry.setDocumentAuthor(ENERGY);
        registry.setDocumentDate(act.getEndDate() != null ? act.getEndDate() : LocalDate.now());
        registry.setNumberOfSheets(act.getExecutiveSchema().getNumberOfPages());
        registry.setAddingTime(LocalDateTime.now());
        registry.setCurrentActId(act.getId());
        return registry;
    }

    private Registry createControlRegistryEntry(EntranceControl control) {
        Registry registry = new Registry();
        registry.setDocumentName("Акт результатов входного контроля МТР и оборудования " +
                (control.getMaterial() != null ? control.getMaterial().getName() : ""));

        String numberWithDate = control.getControlNumber() + " от " + formatter.format(control.getDate()) + " г.";
        registry.setDocumentNumber(numberWithDate);
        registry.setDocumentAuthor(ENERGY);
        registry.setDocumentDate(control.getDate() != null ? control.getDate() : LocalDate.now());
        registry.setNumberOfSheets(1);
        registry.setAddingTime(LocalDateTime.now());
        registry.setCurrentActId(control.getAct().getId());
        return registry;
    }

    private Registry createCertificateRegistryEntry(EntranceControl control) {
        Material material = control.getMaterial();
        Registry registry = new Registry();
        String[] certificateArray = material.getCertificateName().split("№");
        String certificateType = certificateArray[0];
        String certificateNumber = certificateArray[1];
        registry.setDocumentName(certificateType.trim() + " на " + (material.getName() != null ? material.getName() : "материал"));
        registry.setDocumentNumber(certificateNumber != null ? certificateNumber : "б/н");
        registry.setDocumentAuthor(material.getAuthor() != null ? material.getAuthor() : "Производитель не указан");
        registry.setDocumentDate(LocalDate.now());

        registry.setNumberOfSheets(material.getNumberOfPages());
        registry.setAddingTime(LocalDateTime.now());
        registry.setCurrentActId(control.getAct().getId());
        return registry;
    }

    @SneakyThrows
    private void addJournalDocuments(PdfCopy copy, List<Registry> journalRegistries) {
        // Добавляем PDF журналов и обновляем количество страниц
        for (Registry journalRegistry : journalRegistries) {
            String journalName = journalRegistry.getDocumentName();
            try {
                ByteArrayOutputStream pdfStream;

                // Выбираем нужный генератор PDF в зависимости от типа журнала
                switch (journalName) {
                    case "Общий журнал работ (раздел 3)" -> pdfStream = workLogPdfService.generateWorkLogPdf(true, 3);
                    case "Общий журнал работ (раздел 6)" -> pdfStream = workLogPdfService.generateWorkLogPdf(true, 6);
                    case "Журнал входного контроля" -> pdfStream = controlLogPdfService.generateControlLogPdf(true);
                    case null, default -> {
                        continue; // Пропускаем неизвестные типы журналов
                    }
                }

                // Добавляем PDF и получаем количество страниц
                int pages = addDocumentToMerge(copy, new ByteArrayInputStream(pdfStream.toByteArray())) / 2;
                // Обновляем количество листов в записи реестра
                journalRegistry.setNumberOfSheets(pages);

                log.info("Добавлен {} ({} страниц)", journalName, pages);
            } catch (Exception e) {
                log.error("Ошибка при добавлении {}: {}", journalName, e.getMessage());
                throw e;
            }
        }
    }

    private List<Registry> createJournalRegistryEntries() {
        List<Registry> journals = new ArrayList<>();

        // Журнал работ (раздел 3)
        Registry workLog3 = new Registry();
        workLog3.setDocumentName("Общий журнал работ (раздел 3)");
        workLog3.setDocumentNumber("1 от 02.09.2024г.");
        workLog3.setDocumentAuthor(ENERGY);
        workLog3.setDocumentDate(LocalDate.now());
        workLog3.setNumberOfSheets(1); // Временное значение, будет обновлено
        journals.add(workLog3);

        // Журнал работ (раздел 6)
        Registry workLog6 = new Registry();
        workLog6.setDocumentName("Общий журнал работ (раздел 6)");
        workLog6.setDocumentNumber("1 от 02.09.2024г.");
        workLog6.setDocumentAuthor(ENERGY);
        workLog6.setDocumentDate(LocalDate.now());
        workLog6.setNumberOfSheets(1); // Временное значение, будет обновлено
        journals.add(workLog6);

        // Журнал входного контроля
        Registry controlLog = new Registry();
        controlLog.setDocumentName("Журнал входного контроля");
        controlLog.setDocumentNumber("б/н от 02.09.2024 г.");
        controlLog.setDocumentAuthor(ENERGY);
        controlLog.setDocumentDate(LocalDate.now());
        controlLog.setNumberOfSheets(1); // Временное значение, будет обновлено
        journals.add(controlLog);

        return journals;
    }

    /**
     * Добавляет документ в объединенный PDF и возвращает количество страниц
     */
    private int addDocumentToMerge(PdfCopy copy, InputStream inputStream)
            throws IOException, DocumentException {

        // 1. Читаем исходный документ
        PdfReader reader = new PdfReader(inputStream);
        ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();

        // 2. Создаем временный документ с нумерацией
        Document tempDoc = new Document();
        PdfWriter writer = PdfWriter.getInstance(tempDoc, tempOutput);
        writer.setPageEvent(new GlobalPageNumberFooter(f1));
        tempDoc.open();

        // 3. Копируем страницы с нумерацией
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            tempDoc.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, i);
            writer.getDirectContent().addTemplate(page, 0, 0);
        }

        tempDoc.close();
        writer.close();
        reader.close();

        // 4. Добавляем в итоговый документ
        PdfReader numberedReader = new PdfReader(tempOutput.toByteArray());
        int pageCount = numberedReader.getNumberOfPages();
        for (int i = 1; i <= pageCount; i++) {
            copy.addPage(copy.getImportedPage(numberedReader, i));
        }
        numberedReader.close();

        return pageCount;
    }

    /**
     * Добавляет удаленный PDF-документ в объединенный PDF
     *
     * @param copy        объект PdfCopy для объединения документов
     * @param documentUrl URL документа в хранилище (полный URL)
     * @return количество добавленных страниц
     * @throws IOException       если произошла ошибка загрузки документа
     * @throws DocumentException если документ поврежден или не является PDF
     */
    private int addRemoteDocumentToMerge(PdfCopy copy, String documentUrl)
            throws IOException, DocumentException {


        log.info("{}!!!!!", documentUrl);
        Path filePath = getFilePathFromUrl(documentUrl);
        if (!Files.exists(filePath)) {
            log.info(filePath.toString());
            throw new IOException("Файл не найден: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return addDocumentToMerge(copy, inputStream);
        }
    }

    private Path getFilePathFromUrl(String documentUrl) {
        // Извлекаем имя файла из URL
        String fileName = extractFileNameFromUrl(documentUrl);
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Неверный URL документа: " + documentUrl);
        }

        // Строим полный путь к файлу
        return fileStorageLocation.resolve("pdf").resolve(fileName).normalize();
    }

    private String extractFileNameFromUrl(String url) {
        if (url == null) return null;
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    private int getPageCount(InputStream inputStream) throws IOException {
        PdfReader reader = new PdfReader(inputStream);
        int pages = reader.getNumberOfPages();
        reader.close();
        return pages;
    }

    private class GlobalPageNumberFooter extends PdfPageEventHelper {
        private final Font font;

        public GlobalPageNumberFooter(Font font) {
            this.font = font;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            int currentPhysicalPage = writer.getPageNumber();

            // Нумеру��м только нечётные физические страницы
            if (currentPhysicalPage % 2 != 0) {
                int logicalPageNumber = globalPageCounter.incrementAndGet();
                addPageNumber(writer, document, logicalPageNumber);
            }
        }

        private void addPageNumber(PdfWriter writer, Document document, int number) {
            PdfContentByte cb = writer.getDirectContent();

            Phrase footer = new Phrase(String.format("Лист %d", number), fontForPageNumbers);

            // Позиционирование в правом нижнем углу с отступами
            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,  // Выравнивание по правому краю
                    footer,
                    document.right() - 30,  // 30 пунктов от правого края
                    document.bottom() - 3, // 3 пункта от нижнего края
                    0
            );
        }
    }

    private RegistryPeriodDto createPeriodDtoFromSortedActs(LocalDate startDate, LocalDate endDate) {

        // Используем дату последнего акта для месяца/года в заголовке
        LocalDate referenceDate = endDate != null ? endDate :
                startDate != null ? startDate :
                        LocalDate.now();

        // Проверяем, что даты не null (или обрабатываем по-другому)
        if (startDate == null) startDate = referenceDate;
        if (endDate == null) endDate = referenceDate;

        return RegistryPeriodDto.builder()
                .monthId(referenceDate.getMonthValue())
                .year(referenceDate.getYear())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
