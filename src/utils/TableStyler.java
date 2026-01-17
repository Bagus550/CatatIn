package utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class TableStyler {

    public static void styleTable(JTable table) {
        // --- HEADER ---
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(58, 123, 213)); // biru keren
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        // Alignment header ke tengah
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

        // --- ROW HEIGHT & FONT ---
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        // --- CELL RENDERER ---
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Warna baris selang-seling
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(230, 245, 255)); // biru muda
                    } else {
                        c.setBackground(new Color(255, 255, 255)); // putih
                    }
                }

                // Text color jika selected
                if (isSelected) {
                    c.setBackground(new Color(0, 123, 255)); // biru gelap
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(Color.BLACK);
                }

                // Alignment center untuk semua cell
                setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

                return c;
            }
        });

        // Opsional: sesuaikan lebar kolom otomatis
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(150);
        }
    }
}
