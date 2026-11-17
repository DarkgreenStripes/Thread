package Model;

import Helpers.StockStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class Model {
    public record SimulationConfig(
            int producerCount,
            int consumerCount,
            int resourceTypeCount,
            int minimumProductionDelayMillis,
            int maximumProductionDelayMillis,
            int minimumConsumptionDelayMillis,
            int maximumConsumptionDelayMillis,
            boolean unlimitedProduction,
            int minimumProductionAmount,
            int maximumProductionAmount,
            boolean unlimitedConsumption,
            int minimumConsumptionAmount,
            int maximumConsumptionAmount,
            boolean stockControlEnabled,
            StockStrategy stockStrategy,
            int minimumStockCapacity,
            int maximumStockCapacity,
            boolean synchronizationEnabled,
            boolean variableProductionType,
            int fixedProductionType,
            boolean variableConsumptionType,
            int fixedConsumptionType
    ) {

        public SimulationConfig {
            if (producerCount < 0 || consumerCount < 0) {
                throw new IllegalArgumentException("El número de productores y consumidores no puede ser negativo.");
            }
            if (resourceTypeCount < 1 || resourceTypeCount > 100) {
                throw new IllegalArgumentException("El número de tipos de recursos debe estar entre 1 y 100.");
            }
            if (minimumProductionDelayMillis < 0
                    || maximumProductionDelayMillis < minimumProductionDelayMillis
                    || minimumConsumptionDelayMillis < 0
                    || maximumConsumptionDelayMillis < minimumConsumptionDelayMillis) {
                throw new IllegalArgumentException("Los intervalos de tiempo deben cumplir 0 <= mínimo <= máximo.");
            }
            if (minimumProductionAmount < 0 || maximumProductionAmount < minimumProductionAmount
                    || minimumConsumptionAmount < 0 || maximumConsumptionAmount < minimumConsumptionAmount) {
                throw new IllegalArgumentException("Las cantidades deben cumplir 0 <= mínimo <= máximo.");
            }
            if (minimumStockCapacity < 1 || maximumStockCapacity < 1
                    || maximumStockCapacity > Recurs.MAX_CAPACITY
                    || minimumStockCapacity > maximumStockCapacity) {
                throw new IllegalArgumentException("El rango del límite de stock debe cumplir 1 <= mínimo <= máximo <= "
                        + Recurs.MAX_CAPACITY + ".");
            }
            if (fixedProductionType < 1 || fixedProductionType > resourceTypeCount
                    || fixedConsumptionType < 1 || fixedConsumptionType > resourceTypeCount) {
                throw new IllegalArgumentException("El tipo fijo debe estar entre 1 y el número de tipos de recursos.");
            }
        }
    }

    private final Recurs r;
    private final Object workerLock = new Object();
    private List<Thread> producerThreads = List.of();
    private List<Thread> consumerThreads = List.of();
    private SimulationConfig config;
    private IntConsumer onProductSleepTimeChanged = milliseconds -> {};
    private IntConsumer onConsumerSleepTimeChanged = milliseconds -> {};

    public Model() {
        this.r = new Recurs();
    }

    public Recurs getRecurs() {
        return r;
    }

    public void play() throws InterruptedException {
        if (config == null) {
            throw new IllegalStateException("La configuración de la simulación no está establecida.");
        }
        r.configure(config);
        List<Thread> currentProducerThreads = new ArrayList<>();
        List<Thread> currentConsumerThreads = new ArrayList<>();
        synchronized (workerLock) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            for (int i = 1; i <= config.producerCount(); i++) {
                Productor productor = new Productor(r);
                productor.setProductionAmount(config.unlimitedProduction(),
                        config.minimumProductionAmount(), config.maximumProductionAmount());
                productor.setResourceSelection(config.variableProductionType(), config.fixedProductionType() - 1);
                productor.setSleepTimeRange(config.minimumProductionDelayMillis(),
                        config.maximumProductionDelayMillis());
                productor.setOnSleepTimeChanged(onProductSleepTimeChanged);
                currentProducerThreads.add(new Thread(productor, "Chef-" + i));
            }
            for (int i = 1; i <= config.consumerCount(); i++) {
                Consumidor consumidor = new Consumidor(r);
                consumidor.setConsumptionAmount(config.unlimitedConsumption(),
                        config.minimumConsumptionAmount(), config.maximumConsumptionAmount());
                consumidor.setResourceSelection(config.variableConsumptionType(), config.fixedConsumptionType() - 1);
                consumidor.setSleepTimeRange(config.minimumConsumptionDelayMillis(),
                        config.maximumConsumptionDelayMillis());
                consumidor.setOnSleepTimeChanged(onConsumerSleepTimeChanged);
                currentConsumerThreads.add(new Thread(consumidor, "Client-" + i));
            }
            producerThreads = List.copyOf(currentProducerThreads);
            consumerThreads = List.copyOf(currentConsumerThreads);
            for (Thread producerThread : producerThreads) {
                producerThread.start();
            }
            for (Thread consumerThread : consumerThreads) {
                consumerThread.start();
            }
        }
        try {
            for (Thread producerThread : currentProducerThreads) {
                producerThread.join();
            }
            for (Thread consumerThread : currentConsumerThreads) {
                consumerThread.join();
            }
        } finally {
            synchronized (workerLock) {
                if (producerThreads.equals(currentProducerThreads)) {
                    producerThreads = List.of();
                }
                if (consumerThreads.equals(currentConsumerThreads)) {
                    consumerThreads = List.of();
                }
            }
        }
    }

    public void stop() {
        List<Thread> producerThreadsToStop;
        List<Thread> consumerThreadsToStop;
        synchronized (workerLock) {
            producerThreadsToStop = producerThreads;
            consumerThreadsToStop = consumerThreads;
            for (Thread producerThread : producerThreadsToStop) {
                producerThread.interrupt();
            }
            for (Thread consumerThread : consumerThreadsToStop) {
                consumerThread.interrupt();
            }
        }
        for (Thread producerThread : producerThreadsToStop) {
            joinUninterruptibly(producerThread);
        }
        for (Thread consumerThread : consumerThreadsToStop) {
            joinUninterruptibly(consumerThread);
        }
    }

    public void resetFood() {
        stop();
        r.setContador(0);
    }

    private void joinUninterruptibly(Thread thread) {
        if (thread == null) {
            return;
        }
        boolean interrupted = false;
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    public void setOnProductSleepTimeChanged(IntConsumer listener) {
        onProductSleepTimeChanged = listener;
    }

    public void setOnConsumerSleepTimeChanged(IntConsumer listener) {
        onConsumerSleepTimeChanged = listener;
    }
}
