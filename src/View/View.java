package View;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import Model.Model;
import Helpers.CriticalSection;
import javax.swing.*;

public class View {
    static final Color BACKGROUND = new Color(245, 247, 250);
    static final Color SURFACE = Color.WHITE;
    static final Color BORDER = new Color(222, 228, 236);
    static final Color TEXT = new Color(31, 41, 55);
    static final Color MUTED = new Color(107, 114, 128);
    static final Color PRIMARY = new Color(37, 99, 235);
    static final Color SUCCESS = new Color(22, 163, 74);
    static final Color DANGER = new Color(220, 38, 38);

    private final JFrame frame = new JFrame();
    private final ControlPanel controlPanel;
    private final DataPanel dataPanel;
    private final ViewPanel viewPanel;

    public View() {
        frame.setTitle("Simulador de producción y consumo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1360, 860);
        frame.setMinimumSize(new Dimension(1120, 760));

        controlPanel = new ControlPanel(BORDER, SURFACE);
        dataPanel = new DataPanel(BORDER, SURFACE);
        viewPanel = new ViewPanel();

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.add(createHeader(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);
        JScrollPane controlsScrollPane = new JScrollPane(controlPanel);
        controlsScrollPane.setBorder(null);
        controlsScrollPane.setPreferredSize(new Dimension(450, 600));
        controlsScrollPane.getViewport().setBackground(BACKGROUND);
        main.add(controlsScrollPane, BorderLayout.WEST);

        JPanel information = new JPanel();
        information.setOpaque(false);
        information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
        viewPanel.setPreferredSize(new Dimension(700, 250));
        dataPanel.setPreferredSize(new Dimension(700, 390));
        information.add(viewPanel);
        information.add(Box.createVerticalStrut(18));
        information.add(dataPanel);
        main.add(information, BorderLayout.CENTER);
        content.add(main, BorderLayout.CENTER);
        frame.setContentPane(content);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Producción y consumo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Configura los equipos y observa cómo evoluciona la reserva de comida.");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));
        subtitle.setForeground(MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        return header;
    }

    public void playBttnListener(ActionListener actionListener) {
        controlPanel.addPlayButtonListener(actionListener);
    }

    public void stopBttnListener(ActionListener actionListener) {
        controlPanel.addStopButtonListener(actionListener);
    }

    public void resetBttnListener(ActionListener actionListener) {
        controlPanel.addResetButtonListener(actionListener);
    }

    public void resetControlValues() {
        controlPanel.resetValues();
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFoodStatistics(long produced, long consumed, long inStock, long[] stockByType, int maximumStock) {
        dataPanel.setFoodValues(produced, consumed, inStock, maximumStock);
        dataPanel.setStockByResourceType(stockByType);
    }

    public void setCriticalSectionStatus(CriticalSection status) {
        viewPanel.setCriticalSectionStatus(status);
    }

    public Model.SimulationConfig getSimulationConfig() {
        return controlPanel.getSimulationConfig();
    }

    public void setComptador(long comptador) {
        viewPanel.setComptador(comptador);
    }

    public void setSleepTimeProduct(int milliseconds) {
        viewPanel.setSleepTimeProduct(milliseconds);
    }

    public void setSleepTimeConsumed(int milliseconds) {
        viewPanel.setSleepTimeConsumed(milliseconds);
    }

    static void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
    }

    static void styleInput(JTextField field) {
        field.setFont(field.getFont().deriveFont(14f));
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        field.setPreferredSize(new Dimension(90, 36));
    }

}
