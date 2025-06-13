package com.executive_documentation.registries.pdf;

import com.executive_documentation.acts.dto.ActMapper;
import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.pdf.ActPdfService;
import com.executive_documentation.acts.pdf.ControlPdfService;
import com.executive_documentation.acts.pdf.PdfCellCreator;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.registries.dto.RegistryPeriodDto;
import com.executive_documentation.registries.model.Registry;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
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

    private final static String ENERGY = "ООО «Энергомонтаж»";
    private final Path fileStorageLocation;

    private Font f1;
    private Font f2;
    private Font f3;
    private Font fontForPageNumbers;

    public RegistryPdfService(ActRepository actRepository,
                              EntranceControlRepository entranceControlRepository,
                              ActPdfService actPdfService,
                              ControlPdfService controlPdfService,
                              ActMapper actMapper,
                              FileStorageService fileStorageService,
                              @Value("${file.upload-dir}") String uploadDir, PdfCellCreator creator, Path fileStorageLocation) {
        this.actRepository = actRepository;
        this.creator = creator;
        this.fileStorageLocation = fileStorageLocation;
        this.entranceControlRepository = entranceControlRepository;
        this.actPdfService = actPdfService;
        this.controlPdfService = controlPdfService;

        this.actMapper = actMapper;
        this.fileStorageService = fileStorageService;
    }

    @PostConstruct
    public void initFonts() {
        try {
            // Загрузка шрифта из ресурсов
            InputStream fontStream = getClass().getResourceAsStream(FONT_PATH);
            BaseFont baseFont;
            if (fontStream == null) {
                // Попробуем альтернативный путь
                String alternativePath = "src/main/resources" + FONT_PATH;
                File fontFile = new File(alternativePath);
                if (fontFile.exists()) {
                    baseFont = BaseFont.createFont(
                            alternativePath,
                            BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED
                    );
                } else {
                    // Используем системный шрифт как последнее средство
                    baseFont = BaseFont.createFont(
                            "c:/windows/fonts/arial.ttf",
                            BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED
                    );
                    log.warn("Using fallback font (Arial) as main font was not found");
                }
            } else {
                baseFont = BaseFont.createFont(
                        FONT_PATH,
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED,
                        true,
                        fontStream.readAllBytes(),
                        null
                );
                fontStream.close();
            }

            // Инициализация всех шрифтов
            this.f1 = new Font(baseFont, 11);
            this.f2 = new Font(baseFont, 12, Font.BOLD);
            this.f3 = new Font(baseFont, 9, Font.ITALIC);
            this.fontForPageNumbers = new Font(baseFont, 9, Font.BOLDITALIC);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public void createRegistryForPeriod(RegistryPeriodDto periodDto, HttpServletResponse response)
            throws IOException, DocumentException {

        // Сброс счетчика перед созданием нового реестра
        globalPageCounter.set(0);

        // 1. Создаем запись реестра
        Registry registryHeader = createRegistryHeader(periodDto);

        // 2. Получаем акты за период
        List<Act> acts = actRepository.findByEndDateBetween(
                periodDto.getStartDate(),
                periodDto.getEndDate()
        );

        // 3. Создаем итоговый PDF
        ByteArrayOutputStream mergedPdf = new ByteArrayOutputStream();
        Document mergedDoc = new Document();
        PdfCopy copy = new PdfSmartCopy(mergedDoc, mergedPdf);
        mergedDoc.open();

        try {
            // 3.1. Добавляем реестр
            ByteArrayOutputStream registryPdf = generateRegistryPdf(registryHeader, acts);
            int registryPages = addDocumentToMerge(copy, new ByteArrayInputStream(registryPdf.toByteArray()));
            registryHeader.setNumberOfSheets(registryPages);

            // 3.2. Добавляем акты и связанные документы
            for (Act act : acts) {
                // Обработка акта
                Registry actRegistry = createActRegistryEntry(act);
                ActResponseDto dto = actMapper.actToActResponseDto(act);
                dto.setExecutiveSchemaUrl(
                        fileStorageService.getFilePublicUrl(act.getExecutiveSchema().getSchemaPath())
                );

                ByteArrayOutputStream actPdf = actPdfService.generateActPdf(dto);
                int actPages = addDocumentToMerge(copy, new ByteArrayInputStream(actPdf.toByteArray()));
                actRegistry.setNumberOfSheets(actPages);

                // Обработка исполнительной схемы
                if (act.getExecutiveSchema().getSchemaPath() != null) {
                    Registry schemaRegistry = createSchemaRegistryEntry(act);
                    try {
                        int schemaPages = addRemoteDocumentToMerge(copy,
                                fileStorageService.getStorageBaseUrl(dto.getExecutiveSchemaUrl()));
                        schemaRegistry.setNumberOfSheets(schemaPages);
                    } catch (Exception e) {
                        log.warn("Не удалось добавить исполнительную схему: {}", e.getMessage());
                    }
                }

                // Обработка входного контроля
                List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);
                for (EntranceControl control : controls) {
                    Registry controlRegistry = createControlRegistryEntry(control);

                    ByteArrayOutputStream controlPdf = controlPdfService.generateControlPdf(control);
                    int controlPages = addDocumentToMerge(copy, new ByteArrayInputStream(controlPdf.toByteArray()));
                    controlRegistry.setNumberOfSheets(controlPages);

                    // Обработка сертификатов
                    if (control.getMaterial() != null && control.getMaterial().getCertificate() != null) {
                        Registry certRegistry = createCertificateRegistryEntry(control);
                        try {
                            int certPages = addRemoteDocumentToMerge(copy,
                                    fileStorageService.getStorageBaseUrl(
                                            control.getMaterial().getCertificate().getPath()));
                            certRegistry.setNumberOfSheets(certPages);
                        } catch (Exception e) {
                            log.warn("Не удалось добавить сертификат: {}", e.getMessage());
                        }
                    }
                }
            }

            mergedDoc.close();

            // 4. Отправляем PDF клиенту
            response.setContentType("application/pdf");
            String filename = String.format("Реестр_%d_%d.pdf",
                    periodDto.getMonthId(), periodDto.getYear());
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

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

    private ByteArrayOutputStream generateRegistryPdf(Registry registryHeader, List<Act> acts) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = null;
        try {
            // 1. Инициализация PDF-документа
            writer = PdfWriter.getInstance(document, outputStream);
//            writer.setPageEvent(new GlobalPageNumberFooter(f1));
            document.open();

            // 2. Подготовка данных для таблицы
            List<Registry> registries = prepareRegistryData(registryHeader, acts);

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
                tempTable.addCell(creator.createCell(String.valueOf(registry.getRowNumber()), "centerBorder", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentName(), "centerBorder", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentNumber(), "centerBorder", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell(registry.getDocumentAuthor(), "centerBorder", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell("", "centerBorder", f1, 1, 1, 0.0F));
                tempTable.addCell(creator.createCell("", "centerBorder", f1, 1, 1, 0.0F));
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
            if (registryPages % 2 != 0) {
                registries.getFirst().setNumberOfSheets(registryPages % 2 + 1);
            } else {
                registries.getFirst().setNumberOfSheets(registryPages / 2);
            }
            registries.getFirst().setListInOrder(1);

            // 6. Создаем основную таблицу
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(105);
            table.setWidths(widths);
            addRegistryTableData(table, registries);

            // 7. Добавляем в итоговый документ
            document.add(table);
            if(registryPages % 2 != 0) {
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


            log.info("Документ {}, {}", registry.getDocumentNumber(), registry.getDocumentDate() );
            table.addCell(creator.createCell(String.valueOf(registry.getRowNumber()), "centerBorder", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(registry.getDocumentName()), "centerBorder", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(documentNumber, "centerBorder", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(registry.getDocumentAuthor(), "centerBorder", f1, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(registry.getNumberOfSheets()), "centerBorder", f1, 1, 1, 0.0F));
            if (counter > 0) {
                registry.setListInOrder(registries.get(counter - 1).getListInOrder() + registries.get(counter - 1).getNumberOfSheets());
            }
            table.addCell(creator.createCell(String.valueOf(registry.getListInOrder()), "centerBorder", f1, 1, 1, 0.0F));
            counter++;
        }

        addPdfFooter(table);
    }

    private void addPdfRegistryHeader(PdfPTable table) {
//      1 строка
        table.addCell(creator.createCell("", "centerNoBorder", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Форма", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("№1.2", "leftCenterNoBorder", f1, 2, 1, 0.0F));
//      2 строка
        table.addCell(creator.createCell("Заказчик: АО «Черномортранснефть»", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Основание", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("ВСН 012-88 (часть II)", "leftCenterNoBorder", f1, 2, 1, 0.0F));
//      3 строка
        table.addCell(creator.createCell("Подрядчик: ООО «Энергомонтаж»", "leftTopNoBorder", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Строительство", "leftTopNoBorder", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("14.295.24 ТЕКУЩИЙ РЕМОНТ ЗДАНИЙ И СООРУЖЕНИЙ ПК «ШЕСХАРИС»", "leftCenterNoBorder", f1, 2, 1, 0.0F));
//      4 строка
        table.addCell(creator.createCell("Субподрядчик:", "leftTopNoBorder", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Объект: ", "leftTopNoBorder", f1, 1, 1, 0.0F));
        table.addCell(creator.createCell("«Текущий ремонт зданий ПП «Грушовая» ПК «Шесхарис». " +
                "Текущий ремонт». «Текущий ремонт зданий ПП «Шесхарис». " +
                "ПК «Шесхарис». Текущий ремонт", "leftTopNoBorder", f1, 2, 1, 0.0F));
//      Заголовок
        table.addCell(creator.createCell("РЕЕСТР", "centerBottomNoBorder", f1, 6, 1, 50F));
        table.addCell(creator.createCell("исполнительной  документации", "centerTopNoBorder", f1, 6, 1, 30F));
    }

    private void addTableRegistryHeader(PdfPTable table) {
        table.addCell(creator.createCell("№ п/п", "centerBorder", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Наименование документа", "centerBorder", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("№ документа, дата", "centerBorder", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Организация, составившая документ", "centerBorder", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Кол-во листов", "centerBorder", f2, 1, 1, 0.0F));
        table.addCell(creator.createCell("Страница по списку", "centerBorder", f2, 1, 1, 0.0F));
    }

    private void addPdfFooter(PdfPTable table) {
        table.addCell(creator.createCell("Сдал", "leftBottomNoBorder", f1, 2, 1, 50F));
        addRegistrySignature(table);
        table.addCell(creator.createCell("Принял", "leftBottomNoBorder", f1, 2, 1, 40F));
        addRegistrySignature(table);
    }

    private void addRegistrySignature(PdfPTable table) {
        table.addCell(creator.createCell("", "emptyCellBottomBorder", f1, 4, 1, 50F));
        table.addCell(creator.createCell("", "centerNoBorder", f1, 2, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, инициалы)", "centerTopNoBorder", f3, 2, 1, 0.0F));
        table.addCell(creator.createCell("(подпись)", "centerTopNoBorder", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("(дата)", "centerTopNoBorder", f3, 1, 1, 0.0F));
    }

    private List<Registry> prepareRegistryData(Registry registryHeader, List<Act> acts) {
        List<Registry> registries = new ArrayList<>();


        // 1. Добавляем заголовок реестра
        registries.add(registryHeader);

        log.info("В списке актов {} актов", acts.size());
        // 2. Добавляем акты и связанные документы
        for (Act act : acts) {
            // Создаем запись для акта
            Registry actRegistry = createActRegistryEntry(act);
            registries.add(actRegistry);

            // Добавляем исполнительную схему (если есть)
            if (act.getExecutiveSchema() != null) {
                Registry schemaRegistry = createSchemaRegistryEntry(act);
                registries.add(schemaRegistry);
            }

            // Добавляем акты входного контроля
            List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);
            for (EntranceControl control : controls) {
                Registry controlRegistry = createControlRegistryEntry(control);
                registries.add(controlRegistry);

                // Добавляем сертификаты (если есть)
                if (control.getMaterial() != null && control.getMaterial().getCertificate() != null) {
                    Registry certRegistry = createCertificateRegistryEntry(control);
                    registries.add(certRegistry);
                }
            }
        }

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
                (control.getMaterials() != null ? control.getMaterials() : ""));

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

        String certificateType = material.getDocuments().split("№")[0];
        String certificateNumber = material.getDocuments().split("№")[1];
        registry.setDocumentName(certificateType.trim() + " на " + (material.getName() != null ? material.getName() : "материал"));
        registry.setDocumentNumber(material.getDocuments() != null ? certificateNumber : "б/н");
        registry.setDocumentAuthor(material.getAuthor() != null ?
                material.getAuthor() : "Производитель не указан");
        registry.setDocumentDate(LocalDate.now());

        log.info("Документы {}", material.getDocuments());
        registry.setNumberOfSheets(material.getNumberOfPages());
        registry.setAddingTime(LocalDateTime.now());
        registry.setCurrentActId(control.getAct().getId());
        return registry;
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

        Path filePath = getFilePathFromUrl(documentUrl);
        if (!Files.exists(filePath)) {
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

    private class GlobalPageNumberFooter extends PdfPageEventHelper {
        private final Font font;

        public GlobalPageNumberFooter(Font font) {
            this.font = font;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            int currentPhysicalPage = writer.getPageNumber();

            // Нумеруем только нечётные физические страницы
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
}
