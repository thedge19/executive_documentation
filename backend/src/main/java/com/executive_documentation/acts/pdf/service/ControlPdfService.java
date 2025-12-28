package com.executive_documentation.acts.pdf.service;

import com.executive_documentation.acts.dto.font.Fonts;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import com.executive_documentation.acts.pdf.utils.PdfCellCreator;
import com.executive_documentation.acts.pdf.utils.PdfUtils;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.materials.model.Material;
import com.executive_documentation.materials.repository.MaterialRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControlPdfService {
    private final MaterialRepository materialRepository;

    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final PdfCellCreator creator;
    private final ActRepository actRepository;

    private Font f1;
    private Font fontToFillInControl;
    private Font subscript;

    @PostConstruct
    public void initFonts() {
        Fonts fonts = PdfUtils.initFonts();
        this.f1 = fonts.f1();
        this.fontToFillInControl = fonts.fontToFillInControl();
        this.subscript = fonts.subscript();
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
        Act act = actRepository.findById(control.getAct().getId()).orElseThrow();
        String controlDate = control.getDate().toString();
        String[] controlDateList = controlDate.split("-");
        controlDate = controlDateList[2] + " " + PdfUtils.getMonth(controlDateList[1]) + " " + controlDateList[0] + " г.";
        Material material = materialRepository.findById(control.getMaterial().getId()).orElseThrow();
        String controlMaterial = material.getName() + " - " + decimalFormat(control.getQuantity()) + " " + control.getMaterial().getUnits();
        String materialDocuments = material.getCertificateName();
        String standard = material.getStandard();
        String projectName = act.getProject().getName();
        String actNumber = act.getActNumber();
        String subObjectName = act.getSubObject().getName();

        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ»", "CBB", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(наименование строительной организации)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell(clearProjectNameForControls(projectName, 1), "CBB", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(наименование объекта)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("АКТ №", "rBNB", f1, 4, 1, 30F));
        controlTable.addCell(creator.createCell(actNumber, "lBBB", fontToFillInControl, 2, 1, 30F));
        controlTable.addCell(creator.createCell("", "CNB", fontToFillInControl, 3, 1, 30F));
        controlTable.addCell(creator.createCell("результатов входного контроля МТР и оборудования", "cBNB", f1, 9, 1, 30F));
        PdfUtils.longString(controlMaterial, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("((наименование МТР)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("от", "rBNB", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell(controlDate, "cBBB", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "CNB", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("Составлен представителями:", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("субподрядной организации", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("Руководитель работ ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "CBB", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "CNB", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "cTNB", subscript, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля подрядчика", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("Начальник отдела контроля качества", "CBB", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "CNB", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "cTNB", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» Попова Л.С.", "CBB", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля застройщика или технического заказчика", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("н/п", "CBB", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("застройщика ", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("или технического заказчика ", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("Ведущий инженер ОКС ПК «Шесхарис» А.А. Челебиев", "CBB", fontToFillInControl, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(должность, организация, ФИО)", "cTNB", subscript, 7, 1, 0.0F));
        controlTable.addCell(creator.createCell("в том, что произведен", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("выборочный", "CBB", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("осмотр МТР и оборудования", "lCNB", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(сплошной, выборочный)", "cTNB", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 5, 1, 0.0F));
        PdfUtils.longString(controlMaterial, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("(наименование)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("предназначенных проектной документацией", "lCNB", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell(clearProjectNameForControls(projectName, 2), "CBB", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("(шифр, раздел, номер изменения проектной документации)", "cTNB", subscript, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("для строительства на участке", "lCNB", f1, 9, 1, 0.0F));
        PdfUtils.longString(subObjectName, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("(участок линейной части (км/ПК), подобъект НПС/ЛПДС)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("1. Осмотром геометрических размеров, маркировки МТР и оборудования", "lCNB", f1, 9, 1, 0.0F));
        PdfUtils.longString(controlMaterial, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("(наименование, заводской номер)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("сопроводительной документации", "lCNB", f1, 9, 1, 0.0F));
        PdfUtils.longString(materialDocuments, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("(паспорта, сертификаты)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("установлено, что данный МТР и оборудование по своим техническим параметрам", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("Внешний вид, количество", "CBB", fontToFillInControl, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("(контролируемые параметры)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("номеру технических условий", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell(standard, "CBB", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(контролируемые параметры)", "cTNB", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("техническим характеристикам", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("по данным сопроводительной документации", "CBB", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(по данным сопроводительной документации, результатам испытаний)", "cTNB", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("соответствует", "CBB", fontToFillInControl, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("проектной документации.", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("(соответствует/не соответствует)", "cTNB", subscript, 6, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("2. Сопроводительная документация на МТР и оборудование", "lCNB", f1, 9, 1, 0.0F));
        PdfUtils.longString(materialDocuments, 98, controlTable, creator, fontToFillInControl, 9);
        controlTable.addCell(creator.createCell("(паспорта, сертификаты)", "cTNB", subscript, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("имеется в полном комплекте.", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("3. МТР и оборудование", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("не находится", "CBB", fontToFillInControl, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("в Перечне основных видов МТР и оборудования.", "lCNB", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(находится/не находится)", "cTNB", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("4. Техническая документация на МТР и оборудование ", "lCNB", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("отсутствует в Реестре", "CBB", fontToFillInControl, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("(номер учетной записи в Реестре/отсутствует в Реестре)", "cTNB", subscript, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("5. Дополнительно отмечено следующее", "lCNB", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("н/п", "CBB", fontToFillInControl, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "lCNB", f1, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("(заполняется при необходимости)", "cTNB", subscript, 5, 1, 0.0F));
        controlTable.addCell(creator.createCell("Представитель субподрядной", "lBNB", f1, 9, 1, 30F));
        controlTable.addCell(creator.createCell("строительной организации", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» А.Е. Трифонов", "CBB", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель службы", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("подрядчика", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ООО «ЭНЕРГОМОНТАЖ» Л.С. Попова", "CBB", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель службы", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("строительного контроля", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("застройщика или", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("технического заказчика", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "CBB", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
        controlTable.addCell(creator.createCell("Представитель застройщика", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("или технического заказчика", "lCNB", f1, 9, 1, 0.0F));
        controlTable.addCell(creator.createCell("ПК «Шесхарис» А.А. Челебиев", "CBB", fontToFillInControl, 4, 1, 0.0F));
        addControlSigns(controlTable);
    }

    void addControlSigns(PdfPTable controlTable) {
        controlTable.addCell(creator.createCell("", "lCNB", f1, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "CBB", fontToFillInControl, 3, 1, 0.0F));
        controlTable.addCell(creator.createCell("М.П.", "lCNB", f1, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("(организация, ФИО)", "cTNB", subscript, 4, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "cTNB", subscript, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("(подпись)", "cTNB", subscript, 2, 1, 0.0F));
        controlTable.addCell(creator.createCell("(дата)", "cTNB", subscript, 1, 1, 0.0F));
        controlTable.addCell(creator.createCell("", "cTNB", subscript, 1, 1, 0.0F));
    }

    // utils
// --------------------------------------------------------------------------------------------------------------------------------
    private String clearProjectNameForControls(String projectName, int choice) {
        String[] split = projectName.split("\\.");
        if (choice == 1) {
            projectName = split[0] + "." + split[1];
        } else {
            projectName = split[2];
        }

        return projectName;
    }

    private String decimalFormat(BigDecimal quantity) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        return df.format(quantity);
    }
}
