package com.executive_documentation.acts.pdf;

import com.executive_documentation.acts.dto.EntranceControlExportDto;
import com.executive_documentation.acts.dto.EntranceControlMapper;
import com.executive_documentation.acts.repository.EntranceControlRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControlLogPdfService {
    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

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
            this.f3 = new Font(baseFont, 9, Font.ITALIC);
            this.f7 = new Font(baseFont, 30, Font.BOLD);
            this.f8 = new Font(baseFont, 16, Font.BOLD);
            this.f9 = new Font(baseFont, 14);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public void exportEntranceControlLogToPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<EntranceControlExportDto> controls = entranceControlRepository
                .findAllByOrderByDateAsc()
                .stream()
                .map(entranceControlMapper::toExportDto).toList();

        String fileName = "Журнал_входного_контроля.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        // 1. Создаем PDF ЖВК
        ByteArrayOutputStream controlLogPdfStream = new ByteArrayOutputStream();
        Document controlLogDocument = new Document();
        controlLogDocument.setPageSize(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(controlLogDocument, controlLogPdfStream);
            controlLogDocument.open();

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(105);
            table.setTotalWidth(500f);
            float[] widths = new float[]{19.93f, 38.71f, 98.51f, 47.84f, 138.38f, 41.58f, 33.61f, 34.17f, 47.28f};
            table.setWidths(widths);

            addEntranceControlLogTableData(table, controls);
            controlLogDocument.add(table);
        } finally {
            if (controlLogDocument.isOpen()) {
                controlLogDocument.close();
            }
        }

        response.getOutputStream().write(controlLogPdfStream.toByteArray());

        log.info("PDF ЖВК сгенерирован");
    }

    public ByteArrayOutputStream generateControlLogPdf() throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        List<EntranceControlExportDto> controls = entranceControlRepository
                .findAllByOrderByDateAsc()
                .stream()
                .map(entranceControlMapper::toExportDto).toList();

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

            int currentPages = writer.getPageNumber();
            if (currentPages % 2 != 0) {
                // Добавляем пустую страницу
                document.newPage();
                document.add(new Paragraph(" ")); // Пустой контент
                log.info("Добавлена пустая страница для журнала (раздел 3). Было {} страниц", currentPages);
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
            table.addCell(creator.createCell(counter + "", "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(control.getDate()), "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getMaterials().split(" - ")[0], "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getMaterials().split(" - ")[1], "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(control.getDocuments(), "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("Скл. хран.", "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("", "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("", "centerBorder", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("Годен", "centerBorder", f3, 1, 1, 0.0F));
        }
    }

    private void addEntranceControlLogTitle(PdfPTable table) {
        table.addCell(creator.createCell("Журнал", "centerBottomNoBorder", f7, 9, 1, 200F));
        table.addCell(creator.createCell("входного учета и контроля качества получаемых деталей,", "centerBottomNoBorder", f8, 9, 1, 0.0F));
        table.addCell(creator.createCell("материалов, конструкций и оборудования", "centerBottomNoBorder", f8, 9, 1, 0.0F));
        table.addCell(creator.createCell("на объекте:", "centerNoBorder", f9, 9, 1, 80F));
        table.addCell(creator.createCell("14.295.24 «Текущий ремонт зданий и сооружений ПК «Шесхарис»", "centerTopNoBorder", f9, 9, 1, 200F));
        table.addCell(creator.createCell("", "centerTopNoBorder", f9, 9, 1, 520F));
        table.addCell(creator.createCell("ООО «Энергомонтаж»", "leftBottomNoBorder", f1, 8, 1, 20F));
        table.addCell(creator.createCell("Форма 12", "rightBottomNoBorder", f1, 1, 1, 20F));
        table.addCell(creator.createCell("(Наименование предприятия)", "leftCenterNoBorder", f1, 6, 1, 0.0F));
        table.addCell(creator.createCell("РД 39-00147105-015-98", "rightCenterNoBorder", f1, 3, 1, 0.0F));
        table.addCell(creator.createCell("Строительство: Текущий ремонт", "rightCenterNoBorder", f1, 9, 1, 0.0F));
        table.addCell(creator.createCell("Объект: 14.295.24 «Текущий ремонт зданий и сооружений ПК «Шесхарис»", "rightTopNoBorder", f1, 9, 1, 110F));
        table.addCell(creator.createCell("Журнал", "centerTopNoBorder", f9, 9, 1, 0.0F));
        table.addCell(creator.createCell("входного контроля качества", "centerTopNoBorder", f9, 9, 1, 0.0F));
        table.addCell(creator.createCell("", "centerBottomNoBorder", f1, 5, 1, 120F));
        table.addCell(creator.createCell("Начат:", "leftBottomNoBorder", f1, 1, 1, 120F));
        table.addCell(creator.createCell("« 02 » сентября 2024г.", "leftBottomNoBorder", f1, 3, 1, 120F));
        table.addCell(creator.createCell("", "centerBottomNoBorder", f1, 5, 1, 20F));
        table.addCell(creator.createCell("Окончен:", "leftBottomNoBorder", f1, 1, 1, 20F));
        table.addCell(creator.createCell("«      »                 20__г.", "leftBottomNoBorder", f1, 3, 1, 20F));
        table.addCell(creator.createCell("Руководитель подрядной организации", "leftBottomNoBorder", f1, 4, 1, 110F));
        table.addCell(creator.createCell("__________________________", "centerBottomNoBorder", f1, 1, 1, 110F));
        table.addCell(creator.createCell("_________________", "centerBottomNoBorder", f1, 2, 1, 110F));
        table.addCell(creator.createCell("_________________", "centerBottomNoBorder", f1, 2, 1, 110F));
        table.addCell(creator.createCell("М.П.", "leftTopNoBorder", f1, 4, 1, 20F));
        table.addCell(creator.createCell("Фамилия, инициалы", "centerTopNoBorder", f1, 1, 1, 70F));
        table.addCell(creator.createCell("Подпись", "centerTopNoBorder", f1, 2, 1, 70F));
        table.addCell(creator.createCell("Дата", "centerTopNoBorder", f1, 2, 1, 70F));


    }

    private void addEntranceControlLogTableHeader(PdfPTable table) {
        table.addCell(creator.createCell("№ п/п", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Дата поставки", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Объект контроля", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Количество ед. измерения", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Номер партии, сертификат, тех.паспорт", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Условия хранения", "centerBorder", f3, 1, 2, 0.0F));
        table.addCell(creator.createCell("Подпись принявших продукцию по качеству", "centerBorder", f3, 2, 1, 0.0F));
        table.addCell(creator.createCell("Определение степени годности", "centerBorder", f3, 1, 2, 0.0F));

        table.addCell(creator.createCell("Исполни-тель работ", "centerBorder", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("Контроллёр", "centerBorder", f3, 1, 1, 0.0F));
    }
}
