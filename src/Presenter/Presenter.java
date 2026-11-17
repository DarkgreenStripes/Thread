package Presenter;

import Model.Model;
import View.View;
import javax.swing.*;

public class Presenter {
    private final View view;
    private final Model model;
    private long compt;
    private volatile Thread counterThread;

    public Presenter() {
        this.compt = 0;
        this.view = new View();
        this.model = new Model();
    }

    public void loadProgram() {
        model.setOnProductSleepTimeChanged(milliseconds ->
                SwingUtilities.invokeLater(() -> view.setSleepTimeProduct(milliseconds)));
        model.setOnConsumerSleepTimeChanged(milliseconds ->
                SwingUtilities.invokeLater(() -> view.setSleepTimeConsumed(milliseconds)));

        model.getRecurs().setOnCountChanged(() -> {
            compt = model.getRecurs().getContador(); //Coge el valor actualizado de Recurs
            long currentCount = compt;
            long totalProduced = model.getRecurs().getTotalProduced();
            long totalConsumed = model.getRecurs().getTotalConsumed();
            long[] stockByType = model.getRecurs().getStockByResourceType();
            int maximumStock = model.getRecurs().getMaximumStock();
            SwingUtilities.invokeLater(() ->
                    {
                        view.setComptador(currentCount);
                        view.setFoodStatistics(totalProduced, totalConsumed, currentCount, stockByType, maximumStock);
                    }
            );
        });
        model.getRecurs().setOnCriticalSectionChanged(status ->
                SwingUtilities.invokeLater(() -> view.setCriticalSectionStatus(status)));
        view.playBttnListener(e -> playCounter());
        view.stopBttnListener(e -> stopCounter());
        view.resetBttnListener(e -> resetCounter());
    }

    private  void playCounter() {
        if (counterThread != null && counterThread.isAlive()) {
            return;
        }

        Model.SimulationConfig config;
        try {
            config = view.getSimulationConfig();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(view.getFrame(), exception.getMessage(),
                    "Configuración no válida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setConfig(config);

        counterThread = new Thread(() -> {
            try {
                model.play();
                compt = model.getRecurs().getContador();
                SwingUtilities.invokeLater(() -> view.setComptador(compt));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                counterThread = null;
            }
        });
        counterThread.start();
    }

    private void stopCounter() {
        model.stop();
        Thread activeCounter = counterThread;
        if (activeCounter != null) {
            activeCounter.interrupt();
        }
    }

    private void resetCounter() {
        stopCounter();
        model.resetFood();
        view.resetControlValues();
    }

    public long getCompt() { return compt; }
}
