package com.executive_documentation.acts.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PdfUtils {

    private ActPdfCellStyler cellStyler;

    public PdfUtils(ActPdfCellStyler cellStyler) {
        this.cellStyler = cellStyler;
    }

    public PdfPCell createCell(String text, String position, Font font, int numberOfColumns, int numberOfRows, float cellHeight) {
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
}
