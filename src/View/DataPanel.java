package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DataPanel extends JPanel {
    private final DefaultTableModel tableModel;

    public DataPanel(Color borderColor, Color backgroundColor) {
        setBackground(backgroundColor);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(View.BORDER),
                BorderFactory.createEmptyBorder(18, 20, 16, 20)
        ));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Resumen de la simulación");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(View.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[][]{
                {"Recursos producidos", 0},
                {"Recursos consumidos", 0},
                {"Stock total", 0},
                {"Stock por tipo", "Tipo 1: 0"},
                {"Límite máximo aleatorio", Model.Recurs.DEFAULT_MAXIMUM}
        }, new Object[]{"Indicador", "Valor"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(table.getFont().deriveFont(14f));
        table.setForeground(View.TEXT);
        table.setBackground(View.SURFACE);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(View.TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setForeground(View.MUTED);
        table.getTableHeader().setBackground(View.BACKGROUND);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(1).setCellRenderer((source, value, isSelected, hasFocus, row, column) -> {
            JLabel cell = new JLabel(value == null ? "" : value.toString(), SwingConstants.RIGHT);
            cell.setOpaque(true);
            cell.setFont(source.getFont());
            cell.setForeground(View.TEXT);
            cell.setBackground(isSelected ? source.getSelectionBackground() : View.SURFACE);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return cell;
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(View.BORDER));
        scrollPane.getViewport().setBackground(View.SURFACE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setFoodValues(long produced, long consumed, long inStock, int maximumStock) {
        tableModel.setValueAt(produced, 0, 1);
        tableModel.setValueAt(consumed, 1, 1);
        tableModel.setValueAt(inStock, 2, 1);
        tableModel.setValueAt(maximumStock, 4, 1);
    }

    public void setStockByResourceType(long[] stockByType) {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < stockByType.length; i++) {
            if (i > 0) {
                values.append(" | ");
            }
            values.append("Tipo ").append(i + 1).append(": ").append(stockByType[i]);
        }
        tableModel.setValueAt(values.toString(), 3, 1);
    }
}
