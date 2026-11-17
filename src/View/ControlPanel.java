package View;

import Model.Recurs;
import Model.Model;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ControlPanel extends JPanel {
    private final JTextField producerCountField = input("2");
    private final JTextField consumerCountField = input("2");
    private final JTextField resourceTypeCountField = input("1");
    private final JTextField minimumProductionDelayField = input("500");
    private final JTextField maximumProductionDelayField = input("1500");
    private final JTextField minimumConsumptionDelayField = input("500");
    private final JTextField maximumConsumptionDelayField = input("1500");
    private final JTextField minimumProductionAmountField = input("1");
    private final JTextField maximumProductionAmountField = input("10");
    private final JTextField minimumConsumptionAmountField = input("1");
    private final JTextField maximumConsumptionAmountField = input("10");
    private final JTextField minimumStockField = input(Integer.toString(Recurs.DEFAULT_MINIMUM));
    private final JTextField maximumStockField = input(Integer.toString(Recurs.DEFAULT_MAXIMUM));
    private final JComboBox<ResourceTypeMode> productionTypeMode = new JComboBox<>(new ResourceTypeMode[]{
            new ResourceTypeMode("Fijo", false),
            new ResourceTypeMode("Variable (aleatorio)", true)
    });
    private final JComboBox<ResourceTypeMode> consumptionTypeMode = new JComboBox<>(new ResourceTypeMode[]{
            new ResourceTypeMode("Fijo", false),
            new ResourceTypeMode("Variable (aleatorio)", true)
    });
    private final JComboBox<Integer> fixedProductionType = new JComboBox<>(new Integer[]{1});
    private final JComboBox<Integer> fixedConsumptionType = new JComboBox<>(new Integer[]{1});
    private final JCheckBox unlimitedProduction = new JCheckBox("Ilimitats");
    private final JCheckBox unlimitedConsumption = new JCheckBox("Ilimitats");
    private final JCheckBox stockControl = new JCheckBox("Control stock recursos: SI", true);
    private final JCheckBox synchronization =
            new JCheckBox("Sincronització per produir/consumir: SI", true);
    private final JComboBox<StockStrategyOption> stockStrategy = new JComboBox<>(new StockStrategyOption[]{
            new StockStrategyOption("sleep (consultar periódicamente)", Model.SimulationConfig.StockStrategy.SLEEP),
            new StockStrategyOption("wait / notifyAll", Model.SimulationConfig.StockStrategy.WAIT_NOTIFY_ALL)
    });
    private final JLabel strategyHint = new JLabel(" ");
    private final JButton playButton = new JButton("Iniciar simulación");
    private final JButton stopButton = new JButton("Detener");
    private final JButton resetButton = new JButton("Restablecer");

    public ControlPanel(Color borderColor, Color backgroundColor) {
        setBackground(View.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(View.BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(420, 900));

        styleInputFields();
        styleOption(unlimitedProduction);
        styleOption(unlimitedConsumption);
        styleOption(stockControl);
        styleOption(synchronization);
        minimumProductionAmountField.setToolTipText("Sorteja una quantitat per cada operació de producció.");
        maximumProductionAmountField.setToolTipText("Sorteja una quantitat per cada operació de producció.");
        minimumConsumptionAmountField.setToolTipText("Sorteja una quantitat per cada operació de consum.");
        maximumConsumptionAmountField.setToolTipText("Sorteja una quantitat per cada operació de consum.");
        minimumStockField.setToolTipText("La capacitat màxima aleatòria se sorteja una vegada en iniciar.");
        maximumStockField.setToolTipText("La capacitat màxima aleatòria se sorteja una vegada en iniciar.");
        View.styleButton(playButton, View.SUCCESS, Color.WHITE);
        View.styleButton(stopButton, new Color(254, 226, 226), View.DANGER);
        View.styleButton(resetButton, new Color(243, 244, 246), View.TEXT);
        playButton.setAlignmentX(LEFT_ALIGNMENT);
        playButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        stockStrategy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        strategyHint.setForeground(View.MUTED);
        strategyHint.setFont(strategyHint.getFont().deriveFont(11f));
        strategyHint.setAlignmentX(LEFT_ALIGNMENT);

        add(heading("Controles", 22, View.TEXT));
        add(Box.createVerticalStrut(12));
        add(playButton);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(stopButton);
        actions.add(resetButton);
        add(actions);

        add(sectionTitle("PARTICIPANTES Y RECURSOS"));
        add(inputRow("Nº Productors", producerCountField));
        add(inputRow("Nº Consumidors", consumerCountField));
        add(inputRow("Nº Tipus de recursos", resourceTypeCountField));

        add(sectionTitle("INTERVALO ENTRE OPERACIONES (ms)"));
        add(rangeRow("Temps produir", minimumProductionDelayField, maximumProductionDelayField));
        add(rangeRow("Temps consumir", minimumConsumptionDelayField, maximumConsumptionDelayField));

        add(sectionTitle("RECURSOS POR PARTICIPANTE"));
        add(rangeRow("Recursos a produir (per operació)", minimumProductionAmountField, maximumProductionAmountField));
        add(optionRow(unlimitedProduction));
        add(rangeRow("Recursos a consumir (per operació)", minimumConsumptionAmountField, maximumConsumptionAmountField));
        add(optionRow(unlimitedConsumption));

        add(sectionTitle("TIPO DE RECURSO"));
        add(inputRow("Tipo producción", productionTypeMode));
        add(inputRow("Tipo fijo producción", fixedProductionType));
        add(inputRow("Tipo consumo", consumptionTypeMode));
        add(inputRow("Tipo fijo consumo", fixedConsumptionType));

        add(sectionTitle("CONTROL DE STOCK"));
        add(optionRow(stockControl));
        add(inputRow("Tipus control stock", stockStrategy));
        add(strategyHint);
        add(rangeRow("Límit aleatori per simulació", minimumStockField, maximumStockField));

        add(sectionTitle("SINCRONIZACIÓN"));
        add(optionRow(synchronization));
        productionTypeMode.setSelectedIndex(1);
        consumptionTypeMode.setSelectedIndex(1);
        synchronization.addActionListener(event -> {
            synchronization.setText("Sincronització per produir/consumir: "
                    + (synchronization.isSelected() ? "SI" : "NO"));
            updateStrategyHint();
        });
        stockControl.addActionListener(event -> {
            stockControl.setText("Control stock recursos: " + (stockControl.isSelected() ? "SI" : "NO"));
            updateStockControls();
        });
        unlimitedProduction.addActionListener(event -> updateAmountFields());
        unlimitedConsumption.addActionListener(event -> updateAmountFields());
        productionTypeMode.addActionListener(event -> updateResourceTypeControls());
        consumptionTypeMode.addActionListener(event -> updateResourceTypeControls());
        resourceTypeCountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshResourceTypeChoices();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshResourceTypeChoices();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshResourceTypeChoices();
            }
        });
        updateStockControls();
        updateAmountFields();
        refreshResourceTypeChoices();
        updateResourceTypeControls();
    }

    private void styleInputFields() {
        for (JTextField field : new JTextField[]{
                producerCountField, consumerCountField, resourceTypeCountField,
                minimumProductionDelayField, maximumProductionDelayField,
                minimumConsumptionDelayField, maximumConsumptionDelayField,
                minimumProductionAmountField, maximumProductionAmountField,
                minimumConsumptionAmountField, maximumConsumptionAmountField,
                minimumStockField, maximumStockField
        }) {
            View.styleInput(field);
            field.setColumns(5);
        }
    }

    private void styleOption(JCheckBox option) {
        option.setOpaque(false);
        option.setForeground(View.TEXT);
        option.setAlignmentX(LEFT_ALIGNMENT);
    }

    private static JTextField input(String value) {
        return new JTextField(value, 6);
    }

    private JLabel heading(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, (float) size));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = heading(text, 12, View.MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(12, 0, 3, 0));
        return label;
    }

    private JPanel inputRow(String labelText, java.awt.Component input) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel label = new JLabel(labelText);
        label.setForeground(View.TEXT);
        row.add(label, BorderLayout.CENTER);
        row.add(input, BorderLayout.EAST);
        return row;
    }

    private JPanel rangeRow(String labelText, JTextField minimum, JTextField maximum) {
        JPanel range = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        range.setOpaque(false);
        range.add(new JLabel("min"));
        range.add(minimum);
        range.add(new JLabel("-"));
        range.add(new JLabel("max"));
        range.add(maximum);
        return inputRow(labelText, range);
    }

    private JPanel optionRow(JCheckBox option) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(option);
        return row;
    }

    public Model.SimulationConfig getSimulationConfig() {
        int producerCount = parseInteger(producerCountField, "Nº Productors");
        int consumerCount = parseInteger(consumerCountField, "Nº Consumidors");
        int resourceTypes = parseInteger(resourceTypeCountField, "Nº Tipus de recursos");
        int minProductionDelay = parseInteger(minimumProductionDelayField, "Temps mínim produir");
        int maxProductionDelay = parseInteger(maximumProductionDelayField, "Temps màxim produir");
        int minConsumptionDelay = parseInteger(minimumConsumptionDelayField, "Temps mínim consumir");
        int maxConsumptionDelay = parseInteger(maximumConsumptionDelayField, "Temps màxim consumir");
        int minProductionAmount = unlimitedProduction.isSelected()
                ? 0 : parseInteger(minimumProductionAmountField, "Mínim de recursos a produir");
        int maxProductionAmount = unlimitedProduction.isSelected()
                ? 0 : parseInteger(maximumProductionAmountField, "Màxim de recursos a produir");
        int minConsumptionAmount = unlimitedConsumption.isSelected()
                ? 0 : parseInteger(minimumConsumptionAmountField, "Mínim de recursos a consumir");
        int maxConsumptionAmount = unlimitedConsumption.isSelected()
                ? 0 : parseInteger(maximumConsumptionAmountField, "Màxim de recursos a consumir");
        int minStock = stockControl.isSelected()
                ? parseInteger(minimumStockField, "Límit mínim de recursos") : Recurs.DEFAULT_MINIMUM;
        int maxStock = stockControl.isSelected()
                ? parseInteger(maximumStockField, "Límit màxim de recursos") : Recurs.DEFAULT_MAXIMUM;
        StockStrategyOption selectedStrategy = (StockStrategyOption) stockStrategy.getSelectedItem();
        if (selectedStrategy == null) {
            throw new IllegalArgumentException("Selecciona un tipus de control de stock.");
        }
        ResourceTypeMode selectedProductionMode = (ResourceTypeMode) productionTypeMode.getSelectedItem();
        ResourceTypeMode selectedConsumptionMode = (ResourceTypeMode) consumptionTypeMode.getSelectedItem();
        Integer selectedProductionType = (Integer) fixedProductionType.getSelectedItem();
        Integer selectedConsumptionType = (Integer) fixedConsumptionType.getSelectedItem();
        if (selectedProductionMode == null || selectedConsumptionMode == null
                || selectedProductionType == null || selectedConsumptionType == null) {
            throw new IllegalArgumentException("Selecciona el mode i el tipus de recurs per produir i consumir.");
        }

        return new Model.SimulationConfig(producerCount, consumerCount, resourceTypes,
                minProductionDelay, maxProductionDelay, minConsumptionDelay, maxConsumptionDelay,
                unlimitedProduction.isSelected(), minProductionAmount, maxProductionAmount,
                unlimitedConsumption.isSelected(), minConsumptionAmount, maxConsumptionAmount,
                stockControl.isSelected(), selectedStrategy.strategy, minStock, maxStock,
                synchronization.isSelected(),
                selectedProductionMode.variable(),
                selectedProductionType,
                selectedConsumptionMode.variable(),
                selectedConsumptionType);
    }

    private int parseInteger(JTextField field, String description) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(description + " debe ser un entero no negativo.");
        }
    }

    private void updateAmountFields() {
        setEnabled(!unlimitedProduction.isSelected(),
                minimumProductionAmountField, maximumProductionAmountField);
        setEnabled(!unlimitedConsumption.isSelected(),
                minimumConsumptionAmountField, maximumConsumptionAmountField);
    }

    private void updateStockControls() {
        boolean enabled = stockControl.isSelected();
        stockStrategy.setEnabled(enabled);
        minimumStockField.setEnabled(enabled);
        maximumStockField.setEnabled(enabled);
        updateStrategyHint();
    }

    private void refreshResourceTypeChoices() {
        int resourceTypes;
        try {
            resourceTypes = Integer.parseInt(resourceTypeCountField.getText().trim());
        } catch (NumberFormatException exception) {
            return;
        }
        if (resourceTypes < 1 || resourceTypes > 100) {
            return;
        }
        updateTypeChoices(fixedProductionType, resourceTypes);
        updateTypeChoices(fixedConsumptionType, resourceTypes);
    }

    private void updateTypeChoices(JComboBox<Integer> selector, int resourceTypes) {
        Integer selectedType = (Integer) selector.getSelectedItem();
        selector.removeAllItems();
        for (int type = 1; type <= resourceTypes; type++) {
            selector.addItem(type);
        }
        if (selectedType != null && selectedType <= resourceTypes) {
            selector.setSelectedItem(selectedType);
        }
    }

    private void updateResourceTypeControls() {
        ResourceTypeMode productionMode = (ResourceTypeMode) productionTypeMode.getSelectedItem();
        ResourceTypeMode consumptionMode = (ResourceTypeMode) consumptionTypeMode.getSelectedItem();
        fixedProductionType.setEnabled(productionMode != null && !productionMode.variable());
        fixedConsumptionType.setEnabled(consumptionMode != null && !consumptionMode.variable());
    }

    private void updateStrategyHint() {
        if (!stockControl.isSelected()) {
            strategyHint.setText("Sense control de stock, aquest mode no s'aplica.");
        } else if (!synchronization.isSelected()) {
            strategyHint.setText("Amb sincronització desactivada, s'utilitza sleep per consultar el stock.");
        } else {
            strategyHint.setText("El stock es comprova abans de cada operació.");
        }
    }

    private void setEnabled(boolean enabled, JTextField... fields) {
        for (JTextField field : fields) {
            field.setEnabled(enabled);
        }
    }

    public void addPlayButtonListener(ActionListener listener) {
        playButton.addActionListener(listener);
    }

    public void addStopButtonListener(ActionListener listener) {
        stopButton.addActionListener(listener);
    }

    public void addResetButtonListener(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

    public void resetValues() {
        producerCountField.setText("2");
        consumerCountField.setText("2");
        resourceTypeCountField.setText("1");
        minimumProductionDelayField.setText("500");
        maximumProductionDelayField.setText("1500");
        minimumConsumptionDelayField.setText("500");
        maximumConsumptionDelayField.setText("1500");
        minimumProductionAmountField.setText("1");
        maximumProductionAmountField.setText("10");
        minimumConsumptionAmountField.setText("1");
        maximumConsumptionAmountField.setText("10");
        minimumStockField.setText(Integer.toString(Recurs.DEFAULT_MINIMUM));
        maximumStockField.setText(Integer.toString(Recurs.DEFAULT_MAXIMUM));
        unlimitedProduction.setSelected(false);
        unlimitedConsumption.setSelected(false);
        productionTypeMode.setSelectedIndex(1);
        consumptionTypeMode.setSelectedIndex(1);
        stockControl.setSelected(true);
        stockControl.setText("Control stock recursos: SI");
        stockStrategy.setSelectedIndex(1);
        synchronization.setSelected(true);
        synchronization.setText("Sincronització per produir/consumir: SI");
        updateAmountFields();
        updateStockControls();
        refreshResourceTypeChoices();
        updateResourceTypeControls();
    }

    private record ResourceTypeMode(String label, boolean variable) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record StockStrategyOption(String label, Model.SimulationConfig.StockStrategy strategy) {
        @Override
        public String toString() {
            return label;
        }
    }
}
