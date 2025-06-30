package com.executive_documentation.acts.pdf;

import com.executive_documentation.acts.model.EntranceControl;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControlPdfService {

    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final PdfCellCreator creator;

    private Font f1;
    private Font fontToFillInControl;
    private Font subscript;

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
            this.f1 = new Font(baseFont, 9);
            this.fontToFillInControl = new Font(baseFont, 9, Font.BOLDITALIC);
            this.subscript = new Font(baseFont, 6);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
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

        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ»", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(наименование строительной организации)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell(clearProjectNameForControls(control.getAct().getProject().getName(), 1), "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(наименование объекта)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("АКТ №", "rightBottomNoBorder", f1, 4, 1, 30F));
        controlTable.addCell(creator.createCell(control.getAct().getActNumber(), "centerBottomBorderBottom", fontToFillInControl, 2, 1, 30F));
        controlTable.addCell(creator.createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 30F));
        controlTable.addCell(creator.createCell("результатов входного контроля МТР и оборудования", "centerBottomNoBorder", f1, 9, 1, 30F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("((наименование МТР)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("от", "rightBottomNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell(controlDate, "centerBottomBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("Составлен представителями:", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("субподрядной организации", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("Руководитель работ ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "centerBorderBottom", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerNoBorder", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля подрядчика", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("Начальник отдела контроля качества", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerNoBorder", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» Попова Л.С.", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля застройщика или технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("н/п", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("застройщика ", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("или технического заказчика ", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("Ведущий инженер ОКС ПК «Шесхарис» А.А. Челебиев", "centerBorderBottom", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "centerTopNoBorder", subscript, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("в том, что произведен", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("выборочный", "centerBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("осмотр МТР и оборудования", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(сплошной, выборочный)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("(наименование)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("предназначенных проектной документацией", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell(clearProjectNameForControls(control.getAct().getProject().getName(), 2), "centerBorderBottom", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("(шифр, раздел, номер изменения проектной документации)", "centerTopNoBorder", subscript, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("для строительства на участке", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getSubObjectName(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("(участок линейной части (км/ПК), подобъект НПС/ЛПДС)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("1. Осмотром геометрических размеров, маркировки МТР и оборудования", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getMaterials(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("(наименование, заводской номер)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("сопроводительной документации", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getDocuments(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("(паспорта, сертификаты)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("установлено, что данный МТР и оборудование по своим техническим параметрам", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("Внешний вид, количество", "centerBorderBottom", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(контролируемые параметры)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("номеру технических условий", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell(control.getStandard(), "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(контролируемые параметры)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("техническим характеристикам", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("по данным сопроводительной документации", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(по данным сопроводительной документации, результатам испытаний)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("соответствует", "centerBorderBottom", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("проектной документации.", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(соответствует/не соответствует)", "centerTopNoBorder", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("2. Сопроводительная документация на МТР и оборудование", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        addLongString(control.getDocuments(), controlTable, fontToFillInControl);
        controlTable.addCell(creator.createCell("(паспорта, сертификаты)", "centerTopNoBorder", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("имеется в полном комплекте.", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("3. МТР и оборудование", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("не находится", "centerBorderBottom", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("в Перечне основных видов МТР и оборудования.", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(находится/не находится)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("4. Техническая документация на МТР и оборудование ", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("отсутствует в Реестре", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("(номер учетной записи в Реестре/отсутствует в Реестре)", "centerTopNoBorder", subscript, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("5. Дополнительно отмечено следующее", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("н/п", "centerBorderBottom", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("(заполняется при необходимости)", "centerTopNoBorder", subscript, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("Представитель субподрядной", "leftBottomNoBorder", f1, 9, 1, 30F));
        controlTable.addCell(creator.createCell("строительной организации", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель службы", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("подрядчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» Л.С. Попова", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель службы", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("застройщика или", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель застройщика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("или технического заказчика", "leftCenterNoBorder", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ПК «Шесхарис» А.А. Челебиев", "centerBorderBottom", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
    }

    void addControlSigns(PdfPTable controlTable) {
        controlTable.addCell(creator.createCell("", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerBorderBottom", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("М.П.", "leftCenterNoBorder", f1, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("(организация, ФИО)", "centerTopNoBorder", subscript, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerTopNoBorder", subscript, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("(подпись)", "centerTopNoBorder", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(дата)", "centerTopNoBorder", subscript, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "centerTopNoBorder", subscript, 1, 1, 0.0F));
    }

    // utils
// --------------------------------------------------------------------------------------------------------------------------------

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
            table.addCell(creator.createCell(worksRow, "centerBorderBottom", font, 9, 1, 0.0F));
            works = works.replace(worksRow, "");
        }
        table.addCell(creator.createCell(works, "centerBorderBottom", font, 9, 1, 0.0F));
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
