package View;

import Helpers.CriticalSection;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViewPanel extends JPanel {
    private final JLabel counterLabel;
    private final JLabel sleepTimeProduct;
    private final JLabel sleepTimeConsumed;
    private final JLabel semaphoreStatus;
    private final SemaphoreLight freeLight;
    private final SemaphoreLight producerLight;
    private final SemaphoreLight consumerLight;

    public ViewPanel() {
        setBackground(View.SURFACE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(View.BORDER),
                BorderFactory.createEmptyBorder(22, 24, 20, 24)
        ));

        JLabel title = new JLabel("Estado de los recursos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(View.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        counterLabel = new JLabel("Recursos en reserva: 0");
        counterLabel.setFont(counterLabel.getFont().deriveFont(Font.BOLD, 38f));
        counterLabel.setForeground(View.PRIMARY);
        counterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sleepTimeProduct = new JLabel("Último intervalo de producción: —");
        sleepTimeProduct.setFont(sleepTimeProduct.getFont().deriveFont(14f));
        sleepTimeProduct.setForeground(View.MUTED);
        sleepTimeProduct.setAlignmentX(Component.LEFT_ALIGNMENT);

        sleepTimeConsumed = new JLabel("Último intervalo de consumo: —");
        sleepTimeConsumed.setFont(sleepTimeConsumed.getFont().deriveFont(14f));
        sleepTimeConsumed.setForeground(View.MUTED);
        sleepTimeConsumed.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel semaphoreTitle = new JLabel("Región crítica del recurso");
        semaphoreTitle.setFont(semaphoreTitle.getFont().deriveFont(Font.BOLD, 14f));
        semaphoreTitle.setForeground(View.TEXT);
        semaphoreTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        freeLight = new SemaphoreLight(View.SUCCESS);
        freeLight.setActive(true);
        producerLight = new SemaphoreLight(View.PRIMARY);
        consumerLight = new SemaphoreLight(new Color(217, 119, 6));
        semaphoreStatus = new JLabel("Recurso libre");
        semaphoreStatus.setFont(semaphoreStatus.getFont().deriveFont(Font.BOLD, 13f));
        semaphoreStatus.setForeground(View.SUCCESS);

        JPanel semaphorePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        semaphorePanel.setOpaque(false);
        semaphorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        semaphorePanel.add(freeLight);
        semaphorePanel.add(new JLabel("Libre"));
        semaphorePanel.add(producerLight);
        semaphorePanel.add(new JLabel("Produciendo"));
        semaphorePanel.add(consumerLight);
        semaphorePanel.add(new JLabel("Consumiendo"));

        add(title);
        add(Box.createVerticalStrut(18));
        add(counterLabel);
        add(Box.createVerticalStrut(18));
        add(sleepTimeProduct);
        add(Box.createVerticalStrut(8));
        add(sleepTimeConsumed);
        add(Box.createVerticalStrut(16));
        add(semaphoreTitle);
        add(Box.createVerticalStrut(6));
        add(semaphorePanel);
        add(Box.createVerticalStrut(4));
        add(semaphoreStatus);
    }

    // Setters
    public void setComptador(long comptador) {
        counterLabel.setText("Recursos en reserva: " + comptador);
    }

    public void setSleepTimeProduct(int milliseconds) {
        sleepTimeProduct.setText("Último intervalo de producción: " + milliseconds + " ms");
    }

    public void setSleepTimeConsumed(int milliseconds) {
        sleepTimeConsumed.setText("Último intervalo de consumo: " + milliseconds + " ms");
    }

    public void setCriticalSectionStatus(CriticalSection status) {
        freeLight.setActive(status == CriticalSection.IDLE);
        producerLight.setActive(status == CriticalSection.PRODUCING);
        consumerLight.setActive(status == CriticalSection.CONSUMING);
        switch (status) {
            case IDLE -> {
                semaphoreStatus.setText("Recurso libre");
                semaphoreStatus.setForeground(View.SUCCESS);
                System.out.println(status.toString());
            }
            case PRODUCING -> {
                semaphoreStatus.setText("Chef dentro de la región crítica");
                semaphoreStatus.setForeground(View.PRIMARY);
                System.out.println(status.toString());
            }
            case CONSUMING -> {
                semaphoreStatus.setText("Cliente dentro de la región crítica");
                semaphoreStatus.setForeground(new Color(217, 119, 6));
                System.out.println(status.toString());
            }
        }
    }

    private static final class SemaphoreLight extends JPanel {
        private final Color lightColor;
        private boolean active;

        private SemaphoreLight(Color lightColor) {
            this.lightColor = lightColor;
            setOpaque(false);
            setPreferredSize(new java.awt.Dimension(16, 16));
        }

        private void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(active ? lightColor : new Color(209, 213, 219));
            g.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
            if (active) {
                g.setColor(new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 55));
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(lightColor);
                g.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
            }
            g.dispose();
        }
    }
}
