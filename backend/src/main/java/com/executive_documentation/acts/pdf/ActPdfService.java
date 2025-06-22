package com.executive_documentation.acts.pdf;

import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.acts.service.ActService;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ActPdfService {
    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final ActService actService;
    private final ControlPdfService controlPdfService;
    private final FileStorageService fileStorageService;
    private final EntranceControlRepository entranceControlRepository;
    private final ActRepository actRepository;
    private final PdfCellCreator creator;

    private Font f5;
    private Font fontToFillIn;
    private Font subscript;
    private Font f13;
    private Font f14;

    public ActPdfService(ActService actService, ControlPdfService controlPdfService, FileStorageService fileStorageService, EntranceControlRepository entranceControlRepository, ActRepository actRepository, PdfCellCreator creator) {
        this.actService = actService;
        this.controlPdfService = controlPdfService;
        this.fileStorageService = fileStorageService;
        this.entranceControlRepository = entranceControlRepository;
        this.creator = creator;
        this.actRepository = actRepository;
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
            this.f5 = new Font(baseFont, 9);
            this.fontToFillIn = new Font(baseFont, 9, Font.BOLDITALIC);
            this.subscript = new Font(baseFont, 6);
            this.f13 = new Font(baseFont, 11, Font.BOLD);
            this.f14 = new Font(baseFont, 10, Font.BOLD);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public void exportCombinedDocuments(long actId, HttpServletResponse response)
            throws IOException, DocumentException {

        // 1. Получаем основной акт
        ActResponseDto actDto = actService.get(actId);
        Act act = actRepository.findById(actId).orElseThrow(EntityNotFoundException::new);


        // 2. Подготавливаем файл для скачивания
        String fileName = "Комплект_документов_" + actDto.getActNumber().replace("/", "_") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        // 3. Генерируем PDF для акта
        ByteArrayOutputStream actPdf = generateActPdf(actDto);
        log.info("Filename {}", fileName);

        // 4. Объединяем документы
        ByteArrayOutputStream mergedPdf = new ByteArrayOutputStream();
        Document mergedDoc = new Document();
        PdfCopy copy = new PdfCopy(mergedDoc, mergedPdf);
        mergedDoc.open();

        try {
            // 4.1. Добавляем основной акт
            addDocumentToMerge(copy, new ByteArrayInputStream(actPdf.toByteArray()));

            // 4.2. Добавляем исполнительную схему (если есть)
            if (actDto.getExecutiveSchemaUrl() != null) {
                try {
                    addRemoteDocumentToMerge(copy,
                            fileStorageService.getStorageBaseUrl(actDto.getExecutiveSchemaUrl()));
                } catch (Exception e) {
                    log.warn("Не удалось добавить исполнительную схему: {}", e.getMessage());
                }
            }

            // 4.3. Пытаемся найти и добавить акты входного контроля (если есть)
            List<EntranceControl> controls = entranceControlRepository.findAllByAct(act);
            if (!controls.isEmpty()) {
                for (EntranceControl control : controls) {
                    ByteArrayOutputStream controlPdf = controlPdfService.generateControlPdf(control);
                    addDocumentToMerge(copy, new ByteArrayInputStream(controlPdf.toByteArray()));

                    // 4.4. Добавляем сертификат (если есть у контроля)
                    if (control.getMaterial() != null &&
                            control.getMaterial().getCertificate() != null &&
                            control.getMaterial().getCertificate().getPath() != null) {

                        try {
                            addRemoteDocumentToMerge(copy, fileStorageService.getStorageBaseUrl(
                                    control.getMaterial().getCertificate().getPath()));
                        } catch (Exception e) {
                            log.warn("Не удалось добавить сертификат: {}", e.getMessage());
                        }
                    }
                }
            }

            mergedDoc.close();
            response.getOutputStream().write(mergedPdf.toByteArray());

        } catch (Exception e) {
            log.error("Ошибка при объединении документов: {}", e.getMessage());
            // Если не удалось объединить, отдаем хотя бы основной акт
            response.getOutputStream().write(actPdf.toByteArray());
        } finally {
            if (mergedDoc.isOpen()) mergedDoc.close();
            copy.close();
        }

        log.info("Сформирован комплект документов для акта {}", actDto.getActNumber());
    }

    public ByteArrayOutputStream generateActPdf(ActResponseDto dto) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable table = new PdfPTable(36);
            table.setWidthPercentage(105);
            float[] widths = new float[36];
            Arrays.fill(widths, 1f);
            table.setWidths(widths);

            addAOSRTableData(table, dto);
            document.add(table);

        } finally {
            if (document.isOpen()) document.close();
        }

        return outputStream;
    }

    private void addDocumentToMerge(PdfCopy copy, InputStream inputStream) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(inputStream);
        try {
            copy.addDocument(reader);
        } finally {
            reader.close();
        }
    }

    private void addRemoteDocumentToMerge(PdfCopy copy, String url) throws IOException {
        PdfReader reader = new PdfReader(new URL(url));
        try {
            copy.addDocument(reader);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } finally {
            reader.close();
        }
    }

    // АОСР
// --------------------------------------------------------------------------------------------------------------------------------
    private void addAOSRTableData(PdfPTable table, ActResponseDto act) {
        addFirstStaticBlock(table);


        table.addCell(creator.createCell(act.getProjectName(), "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));

        addSecondStaticBlock(table);

        table.addCell(creator.createCell("№", "rightBottomNoBorder", f14, 2, 1, 20F));
        table.addCell(creator.createCell(act.getActNumber(), "centerBottomBorderBottom", fontToFillIn, 5, 1, 20F));
        table.addCell(creator.createCell("", "rightBottomNoBorder", f14, 16, 1, 20F));
        table.addCell(creator.createCell("«", "rightBottomNoBorder", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[0], "centerBottomBorderBottom", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("»", "leftBottomNoBorder", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[1], "centerBottomBorderBottom", fontToFillIn, 5, 1, 20F));
        table.addCell(creator.createCell("", "rightBottomNoBorder", f14, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[2], "centerBottomBorderBottom", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("г.", "leftBottomNoBorder", f5, 1, 1, 20F));

        addThirdStaticBlock(table);

        addLongString(act.getWorks(), table, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименование скрытых работ)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("2. Работы выполнены по проектной документации", "leftCenterNoBorder", f5, 36, 1, 20F));
        table.addCell(creator.createCell(act.getProjectName(), "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(номер, другие реквизиты чертежа, наименование проектной и/или рабочей документации, " +
                        "сведения о лицах, осуществляющих подготовку раздела проектной и/или рабочей документации)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("3. При выполнении работ применены", "leftCenterNoBorder", f5, 15, 1, 0.0F));
        log.info(act.getMaterials());
        addMaterials(act.getMaterials(), table);

        table.addCell(creator.createCell("4. Предъявлены документы, подтверждающие соответствие работ предъявляемым к ним  требованиям",
                "leftCenterNoBorder", f5, 36, 1, 0.0F));
        addLongString(act.getSubmittedDocuments(), table, fontToFillIn, 36);
        table.addCell(creator.createCell("(исполнительные схемы и чертежи, результаты экспертиз, обследований, лабораторных " +
                        "и иных испытаний выполненных работ, проведенных в процессе строительного контроля)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("5. Даты:", "leftBottomNoBorder", f5, 3, 1, 20F));
        table.addCell(creator.createCell("начала работ", "leftBottomNoBorder", f5, 8, 1, 20F));
        table.addCell(creator.createCell("«", "rightBottomNoBorder", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[0], "centerBottomBorderBottom", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("»", "leftBottomNoBorder", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[1], "centerBottomBorderBottom", fontToFillIn, 4, 1, 20F));
        table.addCell(creator.createCell("", "rightBottomNoBorder",
                f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[2], "centerBottomBorderBottom",
                fontToFillIn, 3, 1, 20F));
        table.addCell(creator.createCell("г.", "leftBottomNoBorder", f5, 13, 1, 20F));

        table.addCell(creator.createCell("", "leftBottomNoBorder", f5, 3, 1, 0.0F));
        table.addCell(creator.createCell("окончания работ", "leftBottomNoBorder", f5, 8, 1, 0.0F));
        table.addCell(creator.createCell("«", "rightBottomNoBorder", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[0], "centerBottomBorderBottom", fontToFillIn, 2, 1, 0.0F));
        table.addCell(creator.createCell("»", "leftBottomNoBorder", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[1], "centerBottomBorderBottom", fontToFillIn, 4, 1, 0.0F));
        table.addCell(creator.createCell("", "rightBottomNoBorder", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[2], "centerBottomBorderBottom",
                fontToFillIn, 3, 1, 0.0F));
        table.addCell(creator.createCell("г.", "leftBottomNoBorder", f5, 13, 1, 0.0F));

        table.addCell(creator.createCell("6. Работы выполнены в соответствии с", "leftCenterNoBorder", f5, 36, 1, 0.0F));
        addLongString(act.getInAccordWith(), table, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименования и структурные единицы технических регламентов, иных нормативных " +
                        "правовых актов, разделы проектной и (или) рабочей документации)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("7. Разрешается  производство   последующих  работ", "leftCenterNoBorder", f5, 36, 1, 0.0F));
        addLongString(act.getNextWorks(), table, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименование работ, строительных конструкций, участков сетей инженерно-технического обеспечения)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("Дополнительные сведения", "leftCenterNoBorder", f5, 11, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "centerBottomBorderBottom", fontToFillIn, 25, 1, 0.0F));
        table.addCell(creator.createCell("Акт составлен в  ", "leftCenterNoBorder", f5, 7, 1, 0.0F));
        table.addCell(creator.createCell("3", "centerBottomBorderBottom", fontToFillIn, 2, 1, 0.0F));
        table.addCell(creator.createCell("экземплярах (в случае заполнения акта на бумажном носителе).", "leftCenterNoBorder", f5, 27, 1, 0.0F));

        table.addCell(creator.createCell("Приложения:", "leftCenterNoBorder", f5, 36, 1, 0.0F));
        addLongString(act.getSubmittedDocuments(), table, fontToFillIn, 36);

        addForthStaticBlock(table);
    }

    private void addFirstStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("", "centerNoBorder", f5, 23, 1, 0.0F));
        table.addCell(creator.createCell("Приказ Минстроя  №344/пр от 16.05.2023", "rightCenterNoBorder", f5, 13, 1, 0.0F));
        table.addCell(creator.createCell("", "centerNoBorder", f5, 31, 1, 0.0F));
        table.addCell(creator.createCell("приложение №3", "rightCenterNoBorder", f5, 5, 1, 0.0F));
        table.addCell(creator.createCell("Объект капитального строительства", "leftCenterNoBorder", f5, 36, 1, 0.0F));
    }

    private void addSecondStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("РФ, Краснодарский кр., г. Новороссийск, ш. Сухумское, д. 85, к. 1", "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(наименование объекта капитального строительства в соответствии с проектной документацией, почтовый или строительный адрес объекта капитального строительства)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Застройщик, технический заказчик, лицо, ответственное за  эксплуатацию здания, сооружения, или региональный оператор",
                "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("АО «Черномортранснефть», ОГРН 1022302384136, ИНН 2315072242, 353911, Россия, Краснодарский край,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее -  при наличии), адрес места жительства, ОРГНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,  ", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г. Новороссийск, Сухумское шоссе, д.85, к.1, (8617) 60-34-51, 60-92-61, 60-92-80, Факс: (8617) 64-55-81",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Саморегулируемая организация «Союз Професиональных Строителей Южного Региона» ОГРН 1092300003400,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("ИНН2310141990, 350015, Краснодарский Край, г. Краснодар, ул. Коммунаров, д. 258, тел. (факс) +7(861)2981178",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального " +
                "строительства не требуется);", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Лицо, осуществляющее строительство, реконструкцию, капитальный ремонт",
                "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» ОГРН 1157456011899, ИНН7456028407,455025,РФ,Челябинская область,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее - при наличии), адрес места жительства, ОГРНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г.о. Магнитогорский, г. Магнитогорск, ул. Лесопарковая, д. 93, к.3, пом. 6, тел +7 (951)244-35-65, +7(3519)33-01-04",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("«Союз строительных компаний Урала и Сибири» ОГРН 1087400001897 ИНН 7453198672, 454092, Челябинская область,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г. Челябинск, ул. Елькина, д. 84, тел. (факс) +7 351 280-41-14",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального" +
                " строительства не требуется)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Лицо, осуществляющее подготовку проектной документации",
                "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Проектно-сметное бюро, АО «Черномортранснефть», ОГРН 1022302384136 ИНН 2315072242, РФ,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее - при наличии), адрес места жительства, ОГРНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Краснодарский край, г. Новороссийск, Сухумское шоссе, д.85, к.1, (8617) 60-34-51, 60-92-61, Факс: (8617) 64-55-81",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Саморегулируемая организация «Союз Професиональных Строителей Южного Региона» ОГРН 1092300003400,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("ИНН2310141990, 350015, Краснодарский Край, г. Краснодар, ул. Коммунаров, д. 258, тел. (факс) +7(861)2981178",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального" +
                " строительства не требуется)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("АКТ", "centerBottomNoBorder", f13, 36, 1, 30F));
        table.addCell(creator.createCell("освидетельствования скрытых работ", "centerNoBorder", f13, 36, 1, 0.0F));
    }

    private void addThirdStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("", "centerNoBorder", f5, 23, 1, 20F));
        table.addCell(creator.createCell("(дата составления акта)", "centerTopNoBorder", subscript, 13, 1, 20F));

        table.addCell(creator.createCell("Представитель застройщика, технического заказчика, лица, ответственного за " +
                        "эксплуатацию здания, сооружения, или регионального оператора по вопросам строительного контроля",
                "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Ведущий инженер ОКС ПК «Шесхарис» А.А. Челебиев, приказ № 155 от 19.02.2024 г.",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, идентификационный номер в национальном " +
                "реестре специалистов в области строительства (за исключением случаев,", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("АО «Черномортранснефть», ОГРН 1022302384136, ИНН 2315072242, почтовый адрес: 353911, Россия,",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" когда членство в саморегулируемых организациях в области строительства, реконструкции, " +
                "капитального ремонта объектов капитального строительства не требуется),", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Краснодарский край, г. Новороссийск, Шесхарис-11, тел. +7 (8617) 645740, факс +7 (8617) 645581",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" реквизиты распорядительного документа, подтверждающего полномочия, с указанием полного " +
                "и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица в пределах его места" +
                " нахождения (в случае осуществления строительного контроля на основании договора с застройщиком или " +
                "техническим заказчиком), фамилии, имени, отчества (последнее - при наличии), адреса места " +
                "жительства, ОГРНИП, ИНН индивидуального предпринимателя (в случае осуществления строительного " +
                "контроля на основании договора с застройщиком или техническим заказчиком)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт",
                "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Руководитель работ ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов, приказ №696/16 от 16.07.2024 г.",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт, " +
                "по вопросам строительного контроля", "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Начальник СКК ООО «Энергомонтаж» Л.С. Попова, приказ №176/14.295.24-ЧТН-2024 от 21.06.2024 г.",
                "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, идентификационный номер" +
                        " в национальном реестре специалистов в области строительства (за исключением случаев, когда членство в " +
                        "саморегулируемых организациях в области строительства, реконструкции, капитального ремонта объектов капитального " +
                        "строительства не требуется), реквизиты распорядительного документа, подтверждающего полномочия)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего подготовку проектной документации (в случае привлечения " +
                        "застройщиком лица, осуществляющего подготовку проектной документации, для проверки соответствия выполняемых работ " +
                        "проектной документации согласно части 2 статьи 53 Градостроительного кодекса Российской Федерации)",
                "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия, с указанием полного и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица " +
                "в пределах его места нахождения, фамилии, имени, отчества (последнее - при наличии), адреса места жительства, ОГРНИП, " +
                "ИНН индивиуального предпринимателя)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, выполнившего работы, подлежащие освидетельствованию (в случае " +
                "выполнения работ по договорам о строительстве, реконструкции, капитальном ремонте объектов капитального строительства, " +
                "заключенным с иными лицами)", "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия, с указанием полного и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица " +
                "в пределах его места нахождения, фамилии, имени, отчества (последнее - при наличии), адреса места жительства, ОГРНИП, " +
                "ИНН индивиуального предпринимателя)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("произвели осмотр работ, выполненных", "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ»", "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(полное и (или) сокращенное наименование или фамилия, имя, отчество (последнее - при наличии) " +
                "лица,  выполнившего работы, подлежащие освидетельствованию)", "centerTopNoBorder", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("и составили настоящий акт о нижеследующем:", "leftTopNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("1. К освидетельствованию предъявлены следующие работы:", "leftTopNoBorder", f5, 36, 1, 0.0F));
    }

    private void addForthStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("(исполнительные схемы и чертежи, результаты экспертиз, обследований, лабораторных и иных испытаний)",
                "centerTopNoBorder", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("Представитель застройщика, технического заказчика, лица, ответственного за " +
                        "эксплуатацию здания, сооружения, или регионального оператора по вопросам строительного контроля", "leftCenterNoBorder",
                f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Челебиев А.А.", "centerBottomBorderBottom", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт", "leftCenterNoBorder",
                f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Трифонов А.Е.", "centerBottomBorderBottom", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт, " +
                "по вопросам строительного контроля", "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Попова Л.С.", "centerBottomBorderBottom", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего подготовку проектной документации (в случае привлечения " +
                        "застройщиком лица, осуществляющего подготовку проектной документации, для проверки соответствия " +
                        "выполняемых работ проектной документации  согласно части 2 статьи 53 Градостроительного кодекса Российской Федерации)",
                "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "centerBottomBorderBottom", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, выполнившего работы, подлежащие освидетельствованию (в случае " +
                "выполнения работ по договорам о строительстве, реконструкции, капитальном ремонте объектов капитального " +
                "строительства, заключенным с иными лицами)", "leftCenterNoBorder", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "centerBottomBorderBottom", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);
    }

    private void addSubscripts(PdfPTable table) {
        table.addCell(creator.createCell("", "leftCenterNoBorder", f5, 12, 1, 0.0F));
        table.addCell(creator.createCell("", "centerBottomBorderBottom", fontToFillIn, 9, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, инициалы)", "centerTopNoBorder", subscript, 15, 1, 0.0F));
        table.addCell(creator.createCell("", "centerTopNoBorder", subscript, 12, 1, 0.0F));
        table.addCell(creator.createCell("(подпись)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
    }

    // utils
// --------------------------------------------------------------------------------------------------------------------------------


    public String[] actDate(String date) {
        String[] endDateList = date.split("-");
        String day = endDateList[0];
        String month = getMonth(endDateList[1]);
        String year = endDateList[2];

        return new String[]{day, month, year};
    }

    private String getMonth(String month) {
        List<String> cyphers = List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        List<String> months = List.of("января", "февраля", "марта", "апреля", "мая", "июня", "июля",
                "августа", "сентября", "октября", "ноября", "декабря");

        Map<String, String> monthsMap = IntStream.range(0, cyphers.size())
                .boxed()
                .collect(Collectors.toMap(cyphers::get, months::get));
        return monthsMap.get(month);
    }

    private void addLongString(String works, PdfPTable table, Font font, int numberOfColumns) {
        int currentLength = 118;

        while (works.length() >= currentLength) {
            String worksRow = works.substring(0, currentLength - 1);
            int lastSpace = worksRow.lastIndexOf(" ");
            worksRow = worksRow.substring(0, lastSpace);
            table.addCell(creator.createCell(worksRow, "centerBorderBottom", font, numberOfColumns, 1, 0.0F));
            works = works.replace(worksRow, "");
        }
        table.addCell(creator.createCell(works, "centerBorderBottom", font, numberOfColumns, 1, 0.0F));
    }

    private void addMaterials(String materials, PdfPTable table) {

        if (materials == null || materials.isEmpty()) {
            materials = "н/п";
        }

        if (materials.length() < 60) {
            table.addCell(creator.createCell(materials, "centerBorderBottom", fontToFillIn, 21, 1, 0.0F));
            table.addCell(creator.createCell("", "leftCenterNoBorder", f5, 15, 1, 0.0F));
            table.addCell(creator.createCell("(наименования строительных  материалов (изделий),", "centerTopNoBorder",
                    subscript, 21, 1, 0.0F));
            table.addCell(creator.createCell("н/п", "centerBorderBottom", fontToFillIn, 36, 1, 0.0F));
        } else {
            String materialsRow = materials.substring(0, 60);
            int lastSpace = materialsRow.lastIndexOf(" ");
            materialsRow = materialsRow.substring(0, lastSpace);
            table.addCell(creator.createCell(materialsRow, "centerBorderBottom", fontToFillIn, 21, 1, 0.0F));

            table.addCell(creator.createCell("", "leftCenterNoBorder", f5, 15, 1, 0.0F));
            table.addCell(creator.createCell("(наименования строительных  материалов (изделий),", "centerTopNoBorder",
                    subscript, 21, 1, 0.0F));
            materials = materials.replace(materialsRow, "");
            addLongString(materials, table, fontToFillIn, 36);
        }
        table.addCell(creator.createCell("реквизиты сертификатов и (или) других документов, подтверждающих их качество и " +
                        "безопасность, в случае если необходимо указывать более 5 документов, указывается ссылка на " +
                        "их реестр, который является неотъемлемой частью акта)", "centerTopNoBorder",
                subscript, 36, 1, 0.0F));
    }
}

