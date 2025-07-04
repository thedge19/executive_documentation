package com.executive_documentation.acts.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PdfCellCreator {
    private final ActPdfCellStyler cellStyler;

    public PdfPCell createCell(String text, String position, Font font, int numberOfColumns, int numberOfRows,
                                float cellHeight) {
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
            case "CB":
                cellStyler.createCellStyleHorizontalCenterBorder(cell);
                break;
            case "CBB":
                cellStyler.createCellStyleHorizontalCenterAndVerticalCenterBottomBorder(cell);
                break;
            case "cBBB":
                cellStyler.createCellStyleHorizontalCenterAndVerticalBottomBottomBorder(cell);
                break;
            case "CNB":
                cellStyler.createCellStyleHorizontalCenterAndVerticalCenter(cell);
                break;
            case "cBNB":
                cellStyler.createCellStyleHorizontalCenterAndVerticalBottomNoBorder(cell);
                break;
            case "cTNB":
                cellStyler.createCellStyleHorizontalCenterAndVerticalTopNoBorder(cell);
                break;
            case "lTNB":
                cellStyler.createCellStyleHorizontalLeftAndVerticalTopNoBorder(cell);
                break;
            case "lCNB":
                cellStyler.createCellStyleHorizontalLeftAndVerticalCenterNoBorder(cell);
                break;
            case "lBNB":
                cellStyler.createCellStyleHorizontalLeftAndVerticalBottomNoBorder(cell);
                break;
            case "lBBB":
                cellStyler.createCellStyleHorizontalLeftAndVerticalBottomBottomBorder(cell);
                break;
            case "rBNB":
                cellStyler.createCellStyleHorizontalRightAndVerticalBottomNoBorder(cell);
                break;
            case "rCNB":
                cellStyler.createCellStyleHorizontalRightAndVerticalCenterNoBorder(cell);
                break;
            case "rTNB":
                cellStyler.createCellStyleHorizontalRightAndVerticalTopNoBorder(cell);
                break;
        }

        return cell;
    }
}
