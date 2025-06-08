package com.executive_documentation.acts.pdf;

import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.executive_documentation.fileStorage.service.FileStorageService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ControlPdfService {

    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final ActPdfCellStyler cellStyler;
    private final EntranceControlRepository entranceControlRepository;
    private final FileStorageService fileStorageService;

    private Font f1;
    private Font fontToFillInControl;
    private Font subscript;

    public ControlPdfService(EntranceControlRepository entranceControlRepository, PdfUtils pdfUtils, FileStorageService fileStorageService) {
        this.entranceControlRepository = entranceControlRepository;
        this.fileStorageService = fileStorageService;
        this.cellStyler = new ActPdfCellStyler();
    }

    @PostConstruct
    public void initFonts() throws IOException, DocumentException {
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
            this.f1 = new Font(baseFont, 9);
            this.fontToFillInControl = new Font(baseFont, 9, Font.BOLDITALIC);
            this.subscript = new Font(baseFont, 6);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public void exportControlToPdf(long id, HttpServletResponse response) throws IOException, DocumentException {

        EntranceControl control = entranceControlRepository.findById(id).orElse(null);

        assert control != null;
        String fileName = "АктВК_" + control.getControlNumber().replace("/", "_") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        ByteArrayOutputStream controlPdfStream = new ByteArrayOutputStream();
        Document controlDocument = new Document();

        try {
            PdfWriter.getInstance(controlDocument, controlPdfStream);
            controlDocument.open();

            PdfPTable controlTable = new PdfPTable(9);
            controlTable.setWidthPercentage(105);
            float[] controlWidths = new float[]{48.76f, 89.25f, 48.76f, 60.33f, 57.03f, 48.76f, 42.17f, 71.90f, 33.03f};
            controlTable.setTotalWidth(500f);
            controlTable.setWidths(controlWidths);

            addControlTableData(controlTable, control);
            controlDocument.add(controlTable);
        } finally {
            if (controlDocument.isOpen()) {
                controlDocument.close();
            }
        }

        // 3. Объединяем PDF через HTTP
        ByteArrayOutputStream mergedPdf = new ByteArrayOutputStream();
        Document mergedDoc = new Document();
        PdfCopy copy = null;
        PdfReader controlReader = null;
        PdfReader certificateReader = null;

        try {
            copy = new PdfCopy(mergedDoc, mergedPdf);
            mergedDoc.open();

            // Добавляем акт
            controlReader = new PdfReader(new ByteArrayInputStream(controlPdfStream.toByteArray()));
            copy.addDocument(controlReader);

            // Добавляем сертификат через HTTP
            URL certificateUrl = new URL(fileStorageService.getStorageBaseUrl(control.getMaterial().getCertificate().getPath()));
//            URL certificateUrl = new URL(fileStorageService.getFilePublicUrl(control.getMaterial().getCertificate().getPath()));
            certificateReader = new PdfReader(certificateUrl);
            copy.addDocument(certificateReader);
            mergedDoc.close();

            response.getOutputStream().write(mergedPdf.toByteArray());
            log.info("Объединенный PDF акта входного контроля {} и сертификата сгенерирован", control.getControlNumber());
            return;
        } catch (Exception e) {
            log.error("Ошибка при объединении с сертификатом: {}", e.getMessage());
            response.getOutputStream().write(controlPdfStream.toByteArray());
        } finally {
            // Закрываем все ресурсы в правильном порядке
            if (certificateReader != null) certificateReader.close();
            if (controlReader != null) controlReader.close();
            if (copy != null) copy.close();
            if (mergedDoc.isOpen()) mergedDoc.close();
        }
        log.info("Акт входного контроля {} создан", control.getControlNumber());
    }

    public ByteArrayOutputStream generateControlPdf(EntranceControl control) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(105);
            float[] widths = new float[]{48.76f, 89.25f, 48.76f, 60.33f, 57.03f, 48.76f, 42.17f, 71.90f, 33.03f};
            table.setTotalWidth(500f);
            table.setWidths(widths);

            addControlTableData(table, control);
            document.add(table);

        } finally {
            if (document.isOpen()) document.close();
        }

        return outputStream;
    }

// entrance control acts

    private void addControlTableData(PdfPTable controlTable, EntranceControl control) {
        String controlDate = control.getDate().toString();
        String[] controlDateList = controlDate.split("-");
        controlDate = controlDateList[2] + " " + getMonth(controlDateList[1]) + " " + controlDateList[0] + " г.";

        controlTable.addCell(createCell("ООО «ЭНЕРГОМОНТАЖ»", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(createCell("(наименование строительной организации)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell(clearProjectNameForControls(control.getAct().getProject().getName(), 1), "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(createCell("(наименование объекта)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("АКТ №", "rightBottomNoBorder", f1, 4, 1, 30F));
        controlTable.addCell(createCell(control.getAct().getActNumber(), "centerBottomBorderBottom", fontToFillInControl, 2, 1, 30F));
        controlTable.addCell(createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 30F));
        controlTable.addCell(createCell("результатов входного контроля МТР и оборудования", "centerBottomNoBorder", f1, 9, 1, 30F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("((наименование МТР)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("от", "rightBottomNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(createCell(controlDate, "centerBottomBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(createCell("Составлен представителями:", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("субподрядной организации", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("Руководитель работ ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "centerBorderBottom", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(createCell("", "centerNoBorder", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 7, 1, 0.0F));
        controlTable.addCell(createCell("строительного контроля подрядчика", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("Начальник отдела контроля качества", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(createCell("ООО «ЭНЕРГОМОНТАЖ» Попова Л.С.", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(createCell("строительного контроля застройщика или технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("н/п", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("застройщика ", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("или технического заказчика ", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("Ведущий инженер ОКС ПК «Шесхарис» А.А. Челебиев", "centerBorderBottom", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 7, 1, 0.0F));
        controlTable.addCell(createCell("в том, что произведен", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("выборочный", "centerBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(createCell("осмотр МТР и оборудования", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("(сплошной, выборочный)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("(наименование)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("предназначенных проектной документацией", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(createCell(clearProjectNameForControls(control.getAct().getProject().getName(), 2), "centerBorderBottom", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(createCell("(шифр, раздел, номер изменения проектной документации)", "centerTopNoBorder", subscript, 5, 1, 0.0F));
        controlTable.addCell(createCell("для строительства на участке", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getSubObjectName(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("(участок линейной части (км/ПК), подобъект НПС/ЛПДС)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("1. Осмотром геометрических размеров, маркировки МТР и оборудования", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("(наименование, заводской номер)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("сопроводительной документации", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getDocuments(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("(паспорта, сертификаты)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("установлено, что данный МТР и оборудование по своим техническим параметрам", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("Внешний вид, количество", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(createCell("(контролируемые параметры)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("номеру технических условий", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell(control.getStandard(), "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("(контролируемые параметры)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(createCell("техническим характеристикам", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("по данным сопроводительной документации", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("(по данным сопроводительной документации, результатам испытаний)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(createCell("соответствует", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(createCell("проектной документации.", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("(соответствует/не соответствует)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(createCell("2. Сопроводительная документация на МТР и оборудование", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getDocuments(), controlTable, fontToFillInControl);
        controlTable.addCell(createCell("(паспорта, сертификаты)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(createCell("имеется в полном комплекте.", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("3. МТР и оборудование", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("не находится", "centerBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(createCell("в Перечне основных видов МТР и оборудования.", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(createCell("(находится/не находится)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(createCell("4. Техническая документация на МТР и оборудование ", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(createCell("отсутствует в Реестре", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(createCell("(номер учетной записи в Реестре/отсутствует в Реестре)", "centerTopNoBorder", subscript, 4, 1, 0.0F));
        controlTable.addCell(createCell("5. Дополнительно отмечено следующее", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(createCell("н/п", "centerBorderBottom", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(createCell("(заполняется при необходимости)", "centerTopNoBorder", subscript, 5, 1, 0.0F));
        controlTable.addCell(createCell("Представитель субподрядной", "leftBottomNoBorder", f1, 9, 1, 30F));
        controlTable.addCell(createCell("строительной организации", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(createCell("Представитель службы", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("строительного контроля", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("подрядчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("ООО «ЭНЕРГОМОНТАЖ» Л.С. Попова", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(createCell("Представитель службы", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("строительного контроля", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("застройщика или", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(createCell("Представитель застройщика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("или технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(createCell("ПК «Шесхарис» А.А. Челебиев", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
    }

    void addControlSigns(PdfPTable controlTable) {
        controlTable.addCell(createCell("", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        controlTable.addCell(createCell("", "centerBorderBottom", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(createCell("М.П.", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        controlTable.addCell(createCell("(организация, ФИО)", "centerTopNoBorder", subscript, 4, 1, 0.0F));
        controlTable.addCell(createCell("", "centerTopNoBorder", subscript, 1, 1, 0.0F));
        controlTable.addCell(createCell("(подпись)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(createCell("(дата)", "centerTopNoBorder", subscript, 1, 1, 0.0F));
        controlTable.addCell(createCell("", "centerTopNoBorder", subscript, 1, 1, 0.0F));
    }

    // utils
// --------------------------------------------------------------------------------------------------------------------------------
    private PdfPCell createCell(String text, String position, Font font, int numberOfColumns, int numberOfRows, float cellHeight) {
        Paragraph paragraph = new Paragraph(text, font);
        PdfPCell cell = new PdfPCell(paragraph);

        if (numberOfColumns > 1) {
            cell.setColspan(numberOfColumns);
        }

        if (numberOfRows > 1) {
            cell.setRowspan(numberOfRows);
        }

        if (cellHeight > 0.0F) {
            cell.setFixedHeight(cellHeight);
        }


        switch (position) {
            case "centerBorder":
                cellStyler.createCellStyleHorizontalCenterBorder(cell);
                break;
            case "centerNoBorder":
                cellStyler.createCellStyleHorizontalCenterAndVerticalCenter(cell);
                break;
            case "centerBottomNoBorder":
                cellStyler.createCellStyleHorizontalCenterAndVerticalBottomNoBorder(cell);
                break;
            case "leftTopNoBorder":
                cellStyler.createCellStyleHorizontalLeftAndVerticalTopNoBorder(cell);
                break;
            case "leftCenterNoBorder":
                cellStyler.createCellStyleHorizontalLeftAndVerticalCenterNoBorder(cell);
                break;
            case "leftBottomNoBorder":
                cellStyler.createCellStyleHorizontalLeftAndVerticalBottomNoBorder(cell);
                break;
            case "emptyCellBottomBorder":
                cellStyler.createCellStyleBottomBorder(cell);
                break;
            case "centerTopNoBorder":
                cellStyler.createCellStyleHorizontalCenterAndVerticalTopNoBorder(cell);
                break;
            case "rightBottomNoBorder":
                cellStyler.createCellStyleHorizontalRightAndVerticalBottomNoBorder(cell);
                break;
            case "rightCenterNoBorder":
                cellStyler.createCellStyleHorizontalRightAndVerticalCenterNoBorder(cell);
                break;
            case "rightTopNoBorder":
                cellStyler.createCellStyleHorizontalRightAndVerticalTopNoBorder(cell);
                break;
            case "centerBorderBottom":
                cellStyler.createCellStyleHorizontalCenterAndVerticalCenterBottomBorder(cell);
                break;
            case "centerBottomBorderBottom":
                cellStyler.createCellStyleHorizontalCenterAndVerticalBottomBottomBorder(cell);
                break;
            case "leftBottomBorderBottom":
                cellStyler.createCellStyleHorizontalLeftAndVerticalBottomBottomBorder(cell);
                break;
        }

        return cell;
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

    private void addLongString(String works, PdfPTable table, Font font) {
        int currentLength = 118;

        if (font == fontToFillInControl) {
            currentLength = 98;
        }

        while (works.length() >= currentLength) {
            String worksRow = works.substring(0, currentLength - 1);
            int lastSpace = worksRow.lastIndexOf(" ");
            worksRow = worksRow.substring(0, lastSpace);
            table.addCell(createCell(worksRow, "centerBorderBottom", font, 9, 1, 0.0F));
            works = works.replace(worksRow, "");
        }
        table.addCell(createCell(works, "centerBorderBottom", font, 9, 1, 0.0F));
    }


    private String clearProjectNameForControls(String projectName, int choice) {
        String[] split = projectName.split("\\.");
        if (choice == 1) {
            projectName = split[0] + "." + split[1];
        } else {
            projectName = split[2];
        }

        return projectName;
    }
}
