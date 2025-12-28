package com.executive_documentation.acts.pdf.utils;

import com.executive_documentation.acts.dto.font.Fonts;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class PdfUtils {

    private static final String FONT_PATH = "/fonts/times.ttf";

    public static String getMonth(String month) {
        List<String> cyphers = List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        List<String> months = List.of("января", "февраля", "марта", "апреля", "мая", "июня", "июля",
                "августа", "сентября", "октября", "ноября", "декабря");

        Map<String, String> monthsMap = IntStream.range(0, cyphers.size())
                .boxed()
                .collect(Collectors.toMap(cyphers::get, months::get));
        return monthsMap.get(month);
    }

    public static void longString(String works, int currentLength, PdfPTable table, PdfCellCreator creator, Font font, int numberOfColumns) {
        while (works.length() >= currentLength) {
            String worksRow = works.substring(0, currentLength - 1);
            int lastSpace = worksRow.lastIndexOf(" ");
            worksRow = worksRow.substring(0, lastSpace);
            table.addCell(creator.createCell(worksRow, "CBB", font, numberOfColumns, 1, 0.0F));
            works = works.replace(worksRow, "");
        }
        table.addCell(creator.createCell(works, "CBB", font, numberOfColumns, 1, 0.0F));
    }

    public static Fonts initFonts() {
        try {
            // Загрузка шрифта из ресурсов
            InputStream fontStream = PdfUtils.class.getResourceAsStream(FONT_PATH);
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

            // Создаем и возвращаем объект с инициализированными шрифтами
            return new Fonts(
                    new Font(baseFont, 9, Font.BOLDITALIC), // fontToFillIn
                    new Font(baseFont, 6), // subscript
                    new Font(baseFont, 9, Font.BOLDITALIC), // fontToFillInControl
                    new Font(baseFont, 9, Font.BOLDITALIC), // fontForPageNumbers
                    new Font(baseFont, 11),       // f1
                    new Font(baseFont, 12, Font.BOLD), // f2
                    new Font(baseFont, 9, Font.ITALIC),  // f3
                    new Font(baseFont, 14, Font.BOLD),  // f4 (из WorkLogPdfService)
                    new Font(baseFont, 9),               // f5 (из WorkLogPdfService)
                    new Font(baseFont, 10, Font.ITALIC),  // f6 (из WorkLogPdfService)
                    new Font(baseFont, 30, Font.BOLD),   // f7
                    new Font(baseFont, 16, Font.BOLD),   // f8
                    new Font(baseFont, 14),              // f9
                    new Font(baseFont, 11, Font.BOLD), // f13
                    new Font(baseFont, 10, Font.BOLD) // f14
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }
}
