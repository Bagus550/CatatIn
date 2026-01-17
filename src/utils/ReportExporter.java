/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportExporter {
     // =========================
    // EXPORT PDF
    // =========================
    public static void exportToPDF(JTable table) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Laporan PDF");
            chooser.setSelectedFile(new File("Laporan_Tugas.pdf"));

            if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(chooser.getSelectedFile()));
            document.open();

            // ===== FONT (HELVETICA) =====
            com.itextpdf.text.Font titleFont =
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            16,
                            com.itextpdf.text.Font.BOLD
                    );

            com.itextpdf.text.Font normalFont =
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            10
                    );

            // ===== JUDUL =====
            Paragraph title = new Paragraph("LAPORAN DAFTAR TUGAS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph tanggal = new Paragraph(
                    "Tanggal Cetak : " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                    normalFont
            );
            tanggal.setAlignment(Element.ALIGN_CENTER);
            document.add(tanggal);

            document.add(new Paragraph(" "));

            // ===== TABLE =====
            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            // Header
            for (int i = 0; i < table.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(
                        new Phrase(table.getColumnName(i), normalFont)
                );
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(cell);
            }

            // Data
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object value = table.getValueAt(row, col);
                    pdfTable.addCell(
                            new Phrase(value == null ? "" : value.toString(), normalFont)
                    );
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(null, "PDF berhasil dibuat!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal export PDF:\n" + e.getMessage());
        }
    }

    // =========================
    // EXPORT EXCEL
    // =========================
    public static void exportToExcel(JTable table) {
         try {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Excel");
        chooser.setSelectedFile(new File("Laporan_Tugas.xlsx"));

        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        // Paksa ekstensi .xlsx
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Laporan Tugas");

        // Header
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            headerRow.createCell(i)
                    .setCellValue(table.getColumnName(i));
        }

        // Data
        for (int r = 0; r < table.getRowCount(); r++) {
            Row row = sheet.createRow(r + 1);
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object value = table.getValueAt(r, c);
                row.createCell(c)
                        .setCellValue(value == null ? "" : value.toString());
            }
        }

        // Auto size kolom
        for (int i = 0; i < table.getColumnCount(); i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        workbook.close();

        JOptionPane.showMessageDialog(
                null,
                "Excel berhasil disimpan di:\n" + file.getAbsolutePath()
        );

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
                null,
                "Gagal export Excel:\n" + e.getMessage()
        );
    }
}
}
