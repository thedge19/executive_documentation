package com.executive_documentation.acts.pdf.service;

import com.executive_documentation.acts.dto.entrance.EntranceControlExportDto;
import com.executive_documentation.acts.dto.entrance.EntranceControlMapper;
import com.executive_documentation.acts.dto.font.Fonts;
import com.executive_documentation.acts.pdf.utils.PdfCellCreator;
import com.executive_documentation.acts.pdf.utils.PdfUtils;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControlLogPdfService {
    private final EntranceControlRepository entranceControlRepository;
    private final PdfCellCreator creator;
    private final EntranceControlMapper entranceControlMapper;

    private Font f1;
    private Font f3;
    private Font f7;
    private Font f8;
    private Font f9;

    @PostConstruct
    public void initFonts() {
        Fonts fonts = PdfUtils.initFonts();
        this.f1 = fonts.f1();
        this.f3 = fonts.f3();
        this.f7 = fonts.f7();
        this.f8 = fonts.f8();
        this.f9 = fonts.f9();
    }

    public void exportEntranceControlLogToPdf(HttpServletResponse response) throws IOException, DocumentException {
        String fileName = "Журнал_входного_контроля.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        response.getOutputStream().write(
                generateControlLogPdf(false).toByteArray());

        log.info("PDF ЖВК сгенерирован");
    }

    public ByteArrayOutputStream generateControlLogPdf(boolean addPage) throws DocumentException {
        List<EntranceControlExportDto> controls = entranceControlRepository
                .findAllByOrderByDateAsc()
                .stream()
                .map(entranceControlMapper::toExportDto).toList();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(105);
            table.setTotalWidth(500f);
            float[] widths = new float[]{19.93f, 38.71f, 98.51f, 47.84f, 138.38f, 41.58f, 33.61f, 34.17f, 47.28f};
            table.setWidths(widths);

            addEntranceControlLogTableData(table, controls);
            document.add(table);

            if (addPage) {
                int currentPages = writer.getPageNumber();
                if (currentPages % 2 != 0) {
                    // Добавляем пустую страницу
                    document.newPage();
                    document.add(new Paragraph(" ")); // Пустой контент
                    log.info("Добавлена пустая страница для журнала (раздел 3). Было {} страниц", currentPages);
                }
            }
        } finally {
            document.close();
        }
        return output;
    }

    private void addEntranceControlLogTableData(PdfPTable table, List<EntranceControlExportDto> controls) {
        addEntranceControlLogTitle(table);

        addEntranceControlLogTableHeader(table);

        int counter = 1;

        for (EntranceControlExportDto control : controls) {
            table.addCell(creator.createCell(counter + "", "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(control.getDate()), "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getMaterials().split(" - ")[0], "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getMaterials().split(" - ")[1], "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getDocuments(), "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("Скл. хран.", "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("", "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("", "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("Годен", "CB", f3, 1, 1, 0.0F));
        }
    }

    private void addEntranceControlLogTitle(PdfPTable table) {
        table.addCell(creator.createCell("Журнал", "cBNB", f7, 9, 1, 200F));
        table.addCell(creator.createCell("входного учета и контроля качества получаемых деталей,", "cBNB", f8, 9, 1, 0.0F));
        table.addCell(creator.createCell("материалов, конструкций и оборудования", "cBNB", f8, 9, 1, 0.0F));
        table.addCell(creator.createCell("на объекте:", "CNB", f9, 9, 1, 80F));
        table.addCell(creator.createCell("14.295.24 «Текущий ремонт зданий и сооружений ПК «Шесхарис»", "cTNB", f9, 9, 1, 200F));
        table.addCell(creator.createCell("", "cTNB", f9, 9, 1, 520F));
        table.addCell(creator.createCell("ООО «Энергомонтаж»", "lBNB", f1, 8, 1, 20F));
        table.addCell(creator.createCell("Форма 12", "rBNB", f1, 1, 1, 20F));
        table.addCell(creator.createCell("(Наименование предприятия)", "lCNB", f1, 6, 1, 0.0F));
        table.addCell(creator.createCell("РД 39-00147105-015-98", "rCNB", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Строительство: Текущий ремонт", "rCNB", f1, 9, 1, 0.0F));
        table.addCell(creator.createCell("Объект: 14.295.24 «Текущий ремонт зданий и сооружений ПК «Шесхарис»", "rTNB", f1, 9, 1, 110F));
        table.addCell(creator.createCell("Журнал", "cTNB", f9, 9, 1, 0.0F));
        table.addCell(creator.createCell("входного контроля качества", "cTNB", f9, 9, 1, 0.0F));
        table.addCell(creator.createCell("", "cBNB", f1, 5, 1, 120F));
        table.addCell(creator.createCell("Начат:", "lBNB", f1, 1, 1, 120F));
        table.addCell(creator.createCell("« 02 » сентября 2024г.", "lBNB", f1, 3, 1, 120F));
        table.addCell(creator.createCell("", "cBNB", f1, 5, 1, 20F));
        table.addCell(creator.createCell("Окончен:", "lBNB", f1, 1, 1, 20F));
        table.addCell(creator.createCell("«      »                 20__г.", "lBNB", f1, 3, 1, 20F));
        table.addCell(creator.createCell("Руководитель подрядной организации", "lBNB", f1, 4, 1, 110F));
        table.addCell(creator.createCell("__________________________", "cBNB", f1, 1, 1, 110F));
        table.addCell(creator.createCell("_________________", "cBNB", f1, 2, 1, 110F));
        table.addCell(creator.createCell("_________________", "cBNB", f1, 2, 1, 110F));
        table.addCell(creator.createCell("М.П.", "lTNB", f1, 4, 1, 20F));
        table.addCell(creator.createCell("Фамилия, инициалы", "cTNB", f1, 1, 1, 70F));
        table.addCell(creator.createCell("Подпись", "cTNB", f1, 2, 1, 70F));
        table.addCell(creator.createCell("Дата", "cTNB", f1, 2, 1, 70F));


    }

    private void addEntranceControlLogTableHeader(PdfPTable table) {
        table.addCell(creator.createCell("№ п/п", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Дата поставки", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Объект контроля", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Количество ед. измерения", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Номер партии, сертификат, тех.паспорт", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Условия хранения", "CB", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Подпись принявших продукцию по качеству", "CB", f3, 2, 1, 0.0F));
        table.addCell(creator.createCell("Определение степени годности", "CB", f3, 1, 2, 0.0F));

        table.addCell(creator.createCell("Исполни-тель работ", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("Контроллёр", "CB", f3, 1, 1, 0.0F));
    }
}
