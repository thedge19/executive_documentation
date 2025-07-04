package com.executive_documentation.worklogs.pdf;

import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.acts.dto.act.ActMapper;
import com.executive_documentation.acts.pdf.PdfCellCreator;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.worklogs.dto.WorkLogDto;
import com.executive_documentation.worklogs.service.WorkLogService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
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

@RequiredArgsConstructor
@Service
@Slf4j
public class WorkLogPdfService {
    private static final String FONT_PATH = "/fonts/times.ttf"; // Путь в ресурсах

    private final WorkLogService workLogService;
    private final ActRepository actRepository;
    private final PdfCellCreator creator;

    private Font f3;
    private Font f4;
    private Font f5;
    private Font f6;

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
            this.f3 = new Font(baseFont, 9, Font.ITALIC);
            this.f4 = new Font(baseFont, 14, Font.BOLD);
            this.f5 = new Font(baseFont, 9);
            this.f6 = new Font(baseFont, 10, Font.ITALIC);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public void exportWorkLog3toPdf(HttpServletResponse response) throws IOException, DocumentException {
        String fileName = "Общий_Журнал_работ_третий_раздел.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        response.getOutputStream().write(
                generateWorkLogPdf(false, 3)
                        .toByteArray());

        log.info("PDF третьего раздела сгенерирован");
    }

    public void exportWorkLog6ToPdf(HttpServletResponse response) throws IOException, DocumentException {
        String fileName = "Общий_Журнал_работ_третий_раздел.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        response.getOutputStream().write(
                generateWorkLogPdf(false, 6)
                        .toByteArray());

        log.info("PDF шестого раздела сгенерирован");
    }

    // workLog
    // --------------------------------------------------------------------------------------------------------------------------------
    private void addWorkLog3TableData(PdfPTable table, List<WorkLogDto> dtos3) {
        addPdfWorkLog3Header(table);
        addPdfWorkLog3TableHeader(table);

        for (WorkLogDto dto : dtos3) {
            table.addCell(creator.createCell(String.valueOf(dto.getWorkLogNumber()), "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(dto.getWorkDate()), "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell(String.valueOf(dto.getName()), "CB", f3, 1, 1, 0.0F));
            table.addCell(creator.createCell("Руководитель работ Трифонов А.Е.", "CB", f3, 1, 1, 0.0F));
        }

    }

    private void addPdfWorkLog3Header(PdfPTable table) {
        table.addCell(creator.createCell("РАЗДЕЛ 3", "CNB", f4, 4, 1, 0.0F));
        table.addCell(creator.createCell("Сведения о выполнении работ в процессе строительства, \n" +
                "реконструкции, капитального ремонта объекта капитального строительства", "CNB", f5, 4, 1, 50F));
    }

    private void addPdfWorkLog3TableHeader(PdfPTable table) {
        table.addCell(creator.createCell("№№/пп", "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("Дата выполнения работ", "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("Наименование работ, выполняемых  в процессе строительства, " +
                "реконструкции, капитального ремонта объекта капитального строительства", "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("Должность, фамилия, инициалы, подпись уполномоченного представителя лица, " +
                "осуществляющего строительство", "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("1", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("2", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("3", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("4", "CB", f3, 1, 1, 0.0F));
    }

    private void addWorkLog6TableData(PdfPTable table, List<ActLogResponseDto> dtos6) {
        addPdfWorkLog6Header(table);
        addPdfWorkLog6TableHeader(table);

        int rowNumber = 1;

        for (ActLogResponseDto dto : dtos6) {

            table.addCell(creator.createCell(String.valueOf(rowNumber), "CB", f3, 1, 1, 0.0F));

            String rowAct = "Акт освидетельствования скрытых работ №"
                    + dto.getActNumber() + " "
                    + dto.getWorks();
            table.addCell(creator.createCell(rowAct, "CB", f3, 1, 1, 0.0F));

            String rowDateAndSigns = String.valueOf(dto.getEndDate());

            String signs = rowNumber == 1 ? "г., Ведущий инженер ОКС ПК «Шесхарис» Челебиев А.А., " +
                    "Руководитель работ ООО «Энергомонтаж» А.Е. Трифонов, " +
                    "Начальник СКК ООО «Энергомонтаж» Попова Л.С." : "г., Те же лица, что и в п.1";

            rowDateAndSigns += signs;

            table.addCell(creator.createCell(rowDateAndSigns, "CB", f3, 1, 1, 0.0F));

            rowNumber++;
        }
    }

    private void addPdfWorkLog6Header(PdfPTable table) {
        table.addCell(creator.createCell("РАЗДЕЛ 6", "CNB", f4, 4, 1, 0.0F));
        table.addCell(creator.createCell("Перечень исполнительной документации при строительстве, \n" +
                "реконструкции, капитальном ремонте объекта капитального строительства", "CNB", f5, 4, 1, 50F));
    }

    private void addPdfWorkLog6TableHeader(PdfPTable table) {
        table.addCell(creator.createCell("№№/пп", "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("Наименование исполнительной документации (с указанием вида работ, места " +
                        "расположения конструкций, участков сетей инженерно – технического обеспечения и т.д.)",
                "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("Дата подписания акта, должности, фамилии, инициалы лиц, подписавших акты",
                "CB", f6, 1, 1, 0.0F));
        table.addCell(creator.createCell("1", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("2", "CB", f3, 1, 1, 0.0F));
        table.addCell(creator.createCell("3", "CB", f3, 1, 1, 0.0F));
    }

    public ByteArrayOutputStream generateWorkLogPdf(boolean addPage, int section) throws DocumentException {
        // Определяем данные и структуру таблицы в зависимости от раздела
        PdfPTable table;
        if (section == 3) {
            table = new PdfPTable(4);
            float[] widths = new float[]{31.11f, 79.89f, 267.07f, 121.93f};
            table.setWidths(widths);
        } else if (section == 6) {
            table = new PdfPTable(3);
            float[] widths = new float[]{19.92f, 335.92f, 144.16f};
            table.setWidths(widths);
        } else {
            throw new IllegalArgumentException("Unsupported section: " + section);
        }

        // Общая часть для обоих разделов
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();

            table.setWidthPercentage(105);
            table.setTotalWidth(500f);

            // Заполняем таблицу данными
            if (section == 3) {
                addWorkLog3TableData(table, workLogService.getWorkLog3());
            } else {
                addWorkLog6TableData(table, actRepository
                        .findAllByOrderByEndDateAscActNumberAsc()
                        .stream()
                        .map(ActMapper::actToActLogResponseDto)
                        .toList());
            }

            document.add(table);

            // Добавляем пустую страницу при необходимости
            if (addPage) {
                int currentPages = writer.getPageNumber();
                if (currentPages % 2 != 0) {
                    document.newPage();
                    document.add(new Paragraph(" "));
                    log.info("Добавлена пустая страница для журнала (раздел {}). Было {} страниц", section, currentPages);
                }
            }
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return output;
    }

    public void exportWorkLogToPdf(HttpServletResponse response, int section) throws IOException, DocumentException {
        String fileName = "Общий_Журнал_работ_третий_раздел.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

        response.getOutputStream().write(
                generateWorkLogPdf(false, section)
                        .toByteArray());

        log.info("PDF {} раздела сгенерирован", section);
    }
}
