package com.executive_documentation.acts.pdf;

import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.acts.service.ActService;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.executive_documentation.materials.model.Certificate;
import com.executive_documentation.materials.repository.CertificateRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActPdfService {
    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final ActService actService;
    private final ControlPdfService controlPdfService;
    private final FileStorageService fileStorageService;
    private final EntranceControlRepository entranceControlRepository;
    private final ActRepository actRepository;
    private final PdfCellCreator creator;
    private final CertificateRepository certificateRepository;

    private Font f5;
    private Font fontToFillIn;
    private Font subscript;
    private Font f13;
    private Font f14;

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
                            fileStorageService.getFilePublicUrl(actDto.getExecutiveSchemaUrl()));
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
                    List<Certificate> certificates = certificateRepository.findAllByMaterial(control.getMaterial());
                    for (Certificate certificate : certificates) {

                        if (certificate.getPath() != null) {

                            try {
                                addRemoteDocumentToMerge(copy, fileStorageService.getFilePublicUrl(
                                        certificate.getPath()));
                            } catch (Exception e) {
                                log.warn("Не удалось добавить сертификат: {}", e.getMessage());
                            }
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
        try {
            PdfReader reader = new PdfReader(new URI(url).toURL());
            try {
                copy.addDocument(reader);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            } finally {
                reader.close();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL format", e);
        }
    }

    // АОСР
// --------------------------------------------------------------------------------------------------------------------------------
    private void addAOSRTableData(PdfPTable table, ActResponseDto act) {
        addFirstStaticBlock(table);

        table.addCell(creator.createCell(act.getProjectName(), "CBB", fontToFillIn, 36, 1, 0.0F));

        addSecondStaticBlock(table);

        table.addCell(creator.createCell("№", "rBNB", f14, 2, 1, 20F));
        table.addCell(creator.createCell(act.getActNumber(), "cBBB", fontToFillIn, 5, 1, 20F));
        table.addCell(creator.createCell("", "rBNB", f14, 16, 1, 20F));
        table.addCell(creator.createCell("«", "rBNB", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[0], "cBBB", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("»", "lBNB", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[1], "cBBB", fontToFillIn, 5, 1, 20F));
        table.addCell(creator.createCell("", "rBNB", f14, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[2], "cBBB", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("г.", "lBNB", f5, 1, 1, 20F));

        addThirdStaticBlock(table);

        PdfUtils.longString(act.getWorks(), 118, table, creator, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименование скрытых работ)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("2. Работы выполнены по проектной документации", "lCNB", f5, 36, 1, 20F));
        table.addCell(creator.createCell(act.getProjectName(), "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(номер, другие реквизиты чертежа, наименование проектной и/или рабочей документации, " +
                        "сведения о лицах, осуществляющих подготовку раздела проектной и/или рабочей документации)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("3. При выполнении работ применены", "lCNB", f5, 15, 1, 0.0F));
        log.info(act.getMaterials());
        addMaterials(act.getMaterials(), table);

        table.addCell(creator.createCell("4. Предъявлены документы, подтверждающие соответствие работ предъявляемым к ним  требованиям",
                "lCNB", f5, 36, 1, 0.0F));
        PdfUtils.longString(act.getSubmittedDocuments(), 118, table, creator, fontToFillIn, 36);
        table.addCell(creator.createCell("(исполнительные схемы и чертежи, результаты экспертиз, обследований, лабораторных " +
                        "и иных испытаний выполненных работ, проведенных в процессе строительного контроля)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("5. Даты:", "lBNB", f5, 3, 1, 20F));
        table.addCell(creator.createCell("начала работ", "lBNB", f5, 8, 1, 20F));
        table.addCell(creator.createCell("«", "rBNB", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[0], "cBBB", fontToFillIn, 2, 1, 20F));
        table.addCell(creator.createCell("»", "lBNB", f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[1], "cBBB", fontToFillIn, 4, 1, 20F));
        table.addCell(creator.createCell("", "rBNB",
                f5, 1, 1, 20F));
        table.addCell(creator.createCell(actDate(act.getStartDate())[2], "cBBB",
                fontToFillIn, 3, 1, 20F));
        table.addCell(creator.createCell("г.", "lBNB", f5, 13, 1, 20F));

        table.addCell(creator.createCell("", "lBNB", f5, 3, 1, 0.0F));
        table.addCell(creator.createCell("окончания работ", "lBNB", f5, 8, 1, 0.0F));
        table.addCell(creator.createCell("«", "rBNB", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[0], "cBBB", fontToFillIn, 2, 1, 0.0F));
        table.addCell(creator.createCell("»", "lBNB", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[1], "cBBB", fontToFillIn, 4, 1, 0.0F));
        table.addCell(creator.createCell("", "rBNB", f5, 1, 1, 0.0F));
        table.addCell(creator.createCell(actDate(act.getEndDate())[2], "cBBB",
                fontToFillIn, 3, 1, 0.0F));
        table.addCell(creator.createCell("г.", "lBNB", f5, 13, 1, 0.0F));

        table.addCell(creator.createCell("6. Работы выполнены в соответствии с", "lCNB", f5, 36, 1, 0.0F));
        PdfUtils.longString(act.getInAccordWith(), 118, table, creator, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименования и структурные единицы технических регламентов, иных нормативных " +
                        "правовых актов, разделы проектной и (или) рабочей документации)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("7. Разрешается  производство   последующих  работ", "lCNB", f5, 36, 1, 0.0F));
        PdfUtils.longString(act.getNextWorks(), 118, table, creator, fontToFillIn, 36);
        table.addCell(creator.createCell("(наименование работ, строительных конструкций, участков сетей инженерно-технического обеспечения)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("Дополнительные сведения", "lCNB", f5, 11, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "cBBB", fontToFillIn, 25, 1, 0.0F));
        table.addCell(creator.createCell("Акт составлен в  ", "lCNB", f5, 7, 1, 0.0F));
        table.addCell(creator.createCell("3", "cBBB", fontToFillIn, 2, 1, 0.0F));
        table.addCell(creator.createCell("экземплярах (в случае заполнения акта на бумажном носителе).", "lCNB", f5, 27, 1, 0.0F));

        table.addCell(creator.createCell("Приложения:", "lCNB", f5, 36, 1, 0.0F));
        PdfUtils.longString(act.getSubmittedDocuments(), 118, table, creator, fontToFillIn, 36);

        addForthStaticBlock(table);
    }

    private void addFirstStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("", "CNB", f5, 23, 1, 0.0F));
        table.addCell(creator.createCell("Приказ Минстроя  №344/пр от 16.05.2023", "rCNB", f5, 13, 1, 0.0F));
        table.addCell(creator.createCell("", "CNB", f5, 31, 1, 0.0F));
        table.addCell(creator.createCell("приложение №3", "rCNB", f5, 5, 1, 0.0F));
        table.addCell(creator.createCell("Объект капитального строительства", "lCNB", f5, 36, 1, 0.0F));
    }

    private void addSecondStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("РФ, Краснодарский кр., г. Новороссийск, ш. Сухумское, д. 85, к. 1", "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(наименование объекта капитального строительства в соответствии с проектной документацией, почтовый или строительный адрес объекта капитального строительства)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Застройщик, технический заказчик, лицо, ответственное за  эксплуатацию здания, сооружения, или региональный оператор",
                "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("АО «Черномортранснефть», ОГРН 1022302384136, ИНН 2315072242, 353911, Россия, Краснодарский край,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее -  при наличии), адрес места жительства, ОРГНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,  ", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г. Новороссийск, Сухумское шоссе, д.85, к.1, (8617) 60-34-51, 60-92-61, 60-92-80, Факс: (8617) 64-55-81",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Саморегулируемая организация «Союз Професиональных Строителей Южного Региона» ОГРН 1092300003400,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("ИНН2310141990, 350015, Краснодарский Край, г. Краснодар, ул. Коммунаров, д. 258, тел. (факс) +7(861)2981178",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального " +
                "строительства не требуется);", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Лицо, осуществляющее строительство, реконструкцию, капитальный ремонт",
                "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» ОГРН 1157456011899, ИНН7456028407,455025,РФ,Челябинская область,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее - при наличии), адрес места жительства, ОГРНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г.о. Магнитогорский, г. Магнитогорск, ул. Лесопарковая, д. 93, к.3, пом. 6, тел +7 (951)244-35-65, +7(3519)33-01-04",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("«Союз строительных компаний Урала и Сибири» ОГРН 1087400001897 ИНН 7453198672, 454092, Челябинская область,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("г. Челябинск, ул. Елькина, д. 84, тел. (факс) +7 351 280-41-14",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального" +
                " строительства не требуется)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Лицо, осуществляющее подготовку проектной документации",
                "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Проектно-сметное бюро, АО «Черномортранснефть», ОГРН 1022302384136 ИНН 2315072242, РФ,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, имя, отчество (последнее - при наличии), адрес места жительства, ОГРНИП, " +
                "ИНН индивидуального предпринимателя, полное и (или) сокращенное наименование,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Краснодарский край, г. Новороссийск, Сухумское шоссе, д.85, к.1, (8617) 60-34-51, 60-92-61, Факс: (8617) 64-55-81",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("ОГРН, ИНН, адрес юридического лица в пределах его места нахождения, телефон или факс, " +
                "полное и (или) сокращенное наименование, ОГРН, ИНН саморегулируемой организации,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Саморегулируемая организация «Союз Професиональных Строителей Южного Региона» ОГРН 1092300003400,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("членом которой является указанное юридическое лицо или индивидуальный предприниматель " +
                "(за исключением случаев, когда членство в саморегулируемых организациях", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("ИНН2310141990, 350015, Краснодарский Край, г. Краснодар, ул. Коммунаров, д. 258, тел. (факс) +7(861)2981178",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" в области строительства, реконструкции, капитального ремонта объектов капитального" +
                " строительства не требуется)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("АКТ", "cBNB", f13, 36, 1, 30F));
        table.addCell(creator.createCell("освидетельствования скрытых работ", "CNB", f13, 36, 1, 0.0F));
    }

    private void addThirdStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("", "CNB", f5, 23, 1, 20F));
        table.addCell(creator.createCell("(дата составления акта)", "cTNB", subscript, 13, 1, 20F));

        table.addCell(creator.createCell("Представитель застройщика, технического заказчика, лица, ответственного за " +
                        "эксплуатацию здания, сооружения, или регионального оператора по вопросам строительного контроля",
                "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Ведущий инженер ОКС ПК «Шесхарис» А.А. Челебиев, приказ № 155 от 19.02.2024 г.",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, идентификационный номер в национальном " +
                "реестре специалистов в области строительства (за исключением случаев,", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("АО «Черномортранснефть», ОГРН 1022302384136, ИНН 2315072242, почтовый адрес: 353911, Россия,",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" когда членство в саморегулируемых организациях в области строительства, реконструкции, " +
                "капитального ремонта объектов капитального строительства не требуется),", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Краснодарский край, г. Новороссийск, Шесхарис-11, тел. +7 (8617) 645740, факс +7 (8617) 645581",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell(" реквизиты распорядительного документа, подтверждающего полномочия, с указанием полного " +
                "и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица в пределах его места" +
                " нахождения (в случае осуществления строительного контроля на основании договора с застройщиком или " +
                "техническим заказчиком), фамилии, имени, отчества (последнее - при наличии), адреса места " +
                "жительства, ОГРНИП, ИНН индивидуального предпринимателя (в случае осуществления строительного " +
                "контроля на основании договора с застройщиком или техническим заказчиком)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт",
                "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Руководитель работ ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов, приказ №696/16 от 16.07.2024 г.",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт, " +
                "по вопросам строительного контроля", "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Начальник СКК ООО «Энергомонтаж» Л.С. Попова, приказ №176/14.295.24-ЧТН-2024 от 21.06.2024 г.",
                "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, идентификационный номер" +
                        " в национальном реестре специалистов в области строительства (за исключением случаев, когда членство в " +
                        "саморегулируемых организациях в области строительства, реконструкции, капитального ремонта объектов капитального " +
                        "строительства не требуется), реквизиты распорядительного документа, подтверждающего полномочия)",
                "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, осуществляющего подготовку проектной документации (в случае привлечения " +
                        "застройщиком лица, осуществляющего подготовку проектной документации, для проверки соответствия выполняемых работ " +
                        "проектной документации согласно части 2 статьи 53 Градостроительного кодекса Российской Федерации)",
                "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия, с указанием полного и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица " +
                "в пределах его места нахождения, фамилии, имени, отчества (последнее - при наличии), адреса места жительства, ОГРНИП, " +
                "ИНН индивиуального предпринимателя)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("Представитель лица, выполнившего работы, подлежащие освидетельствованию (в случае " +
                "выполнения работ по договорам о строительстве, реконструкции, капитальном ремонте объектов капитального строительства, " +
                "заключенным с иными лицами)", "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(должность (при наличии), фамилия, инициалы, реквизиты распорядительного документа, " +
                "подтверждающего полномочия, с указанием полного и (или) сокращенного наименования, ОГРН, ИНН, адреса юридического лица " +
                "в пределах его места нахождения, фамилии, имени, отчества (последнее - при наличии), адреса места жительства, ОГРНИП, " +
                "ИНН индивиуального предпринимателя)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("произвели осмотр работ, выполненных", "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ»", "CBB", fontToFillIn, 36, 1, 0.0F));
        table.addCell(creator.createCell("(полное и (или) сокращенное наименование или фамилия, имя, отчество (последнее - при наличии) " +
                "лица,  выполнившего работы, подлежащие освидетельствованию)", "cTNB", subscript, 36, 1, 0.0F));
        table.addCell(creator.createCell("и составили настоящий акт о нижеследующем:", "lTNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("1. К освидетельствованию предъявлены следующие работы:", "lTNB", f5, 36, 1, 0.0F));
    }

    private void addForthStaticBlock(PdfPTable table) {
        table.addCell(creator.createCell("(исполнительные схемы и чертежи, результаты экспертиз, обследований, лабораторных и иных испытаний)",
                "cTNB", subscript, 36, 1, 0.0F));

        table.addCell(creator.createCell("Представитель застройщика, технического заказчика, лица, ответственного за " +
                        "эксплуатацию здания, сооружения, или регионального оператора по вопросам строительного контроля", "lCNB",
                f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Челебиев А.А.", "cBBB", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт", "lCNB",
                f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Трифонов А.Е.", "cBBB", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего строительство, реконструкцию, капитальный ремонт, " +
                "по вопросам строительного контроля", "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("Попова Л.С.", "cBBB", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, осуществляющего подготовку проектной документации (в случае привлечения " +
                        "застройщиком лица, осуществляющего подготовку проектной документации, для проверки соответствия " +
                        "выполняемых работ проектной документации  согласно части 2 статьи 53 Градостроительного кодекса Российской Федерации)",
                "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "cBBB", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);

        table.addCell(creator.createCell("Представитель лица, выполнившего работы, подлежащие освидетельствованию (в случае " +
                "выполнения работ по договорам о строительстве, реконструкции, капитальном ремонте объектов капитального " +
                "строительства, заключенным с иными лицами)", "lCNB", f5, 36, 1, 0.0F));
        table.addCell(creator.createCell("н/п", "cBBB", fontToFillIn, 15, 1, 0.0F));
        addSubscripts(table);
    }

    private void addSubscripts(PdfPTable table) {
        table.addCell(creator.createCell("", "lCNB", f5, 12, 1, 0.0F));
        table.addCell(creator.createCell("", "cBBB", fontToFillIn, 9, 1, 0.0F));
        table.addCell(creator.createCell("(фамилия, инициалы)", "cTNB", subscript, 15, 1, 0.0F));
        table.addCell(creator.createCell("", "cTNB", subscript, 12, 1, 0.0F));
        table.addCell(creator.createCell("(подпись)", "cTNB", subscript, 9, 1, 0.0F));
    }

    private void addMaterials(String materials, PdfPTable table) {

        if (materials == null || materials.isEmpty()) {
            materials = "н/п";
        }

        if (materials.length() < 60) {
            table.addCell(creator.createCell(materials, "CBB", fontToFillIn, 21, 1, 0.0F));
            table.addCell(creator.createCell("", "lCNB", f5, 15, 1, 0.0F));
            table.addCell(creator.createCell("(наименования строительных  материалов (изделий),", "cTNB",
                    subscript, 21, 1, 0.0F));
            table.addCell(creator.createCell("н/п", "CBB", fontToFillIn, 36, 1, 0.0F));
        } else {
            String materialsRow = materials.substring(0, 60);
            int lastSpace = materialsRow.lastIndexOf(" ");
            materialsRow = materialsRow.substring(0, lastSpace);
            table.addCell(creator.createCell(materialsRow, "CBB", fontToFillIn, 21, 1, 0.0F));

            table.addCell(creator.createCell("", "lCNB", f5, 15, 1, 0.0F));
            table.addCell(creator.createCell("(наименования строительных  материалов (изделий),", "cTNB",
                    subscript, 21, 1, 0.0F));
            materials = materials.replace(materialsRow, "");
            PdfUtils.longString(materials, 118, table, creator, fontToFillIn, 36);
        }
        table.addCell(creator.createCell("реквизиты сертификатов и (или) других документов, подтверждающих их качество и " +
                        "безопасность, в случае если необходимо указывать более 5 документов, указывается ссылка на " +
                        "их реестр, который является неотъемлемой частью акта)", "cTNB",
                subscript, 36, 1, 0.0F));
    }

    // utils
// --------------------------------------------------------------------------------------------------------------------------------
    public String[] actDate(String date) {
        String[] endDateList = date.split("-");
        String day = endDateList[0];
        String month = PdfUtils.getMonth(endDateList[1]);
        String year = endDateList[2];

        return new String[]{day, month, year};
    }
}

