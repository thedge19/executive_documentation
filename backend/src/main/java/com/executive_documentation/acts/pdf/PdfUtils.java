package com.executive_documentation.acts.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PdfUtils {
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
}
