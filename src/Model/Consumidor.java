package Model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

public class Consumidor implements Runnable {
    private final Recurs recurs;
    private boolean unlimited;
    private int minimumAmount;
    private int maximumAmount;
    private boolean variableResourceType = true;
    private int fixedResourceType;
    private int minimumSleepTimeMillis;
    private int maximumSleepTimeMillis;
    private IntConsumer onSleepTimeChanged = milliseconds -> {};

    public Consumidor(Recurs recurs) {
        this.recurs = recurs;
    }

    public void setConsumptionAmount(boolean unlimited, int minimumAmount, int maximumAmount) {
        this.unlimited = unlimited;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
    }

    public void setResourceSelection(boolean variableResourceType, int fixedResourceType) {
        this.variableResourceType = variableResourceType;
        this.fixedResourceType = fixedResourceType;
    }

    public void setSleepTimeRange(int minimumSleepTimeMillis, int maximumSleepTimeMillis) {
        this.minimumSleepTimeMillis = minimumSleepTimeMillis;
        this.maximumSleepTimeMillis = maximumSleepTimeMillis;
    }

    public void setOnSleepTimeChanged(IntConsumer listener) {
        onSleepTimeChanged = listener;
    }

    @Override
    public void run() {
        boolean firstOperation = true;
        while ((unlimited || firstOperation) && !Thread.currentThread().isInterrupted()) {
            try {
                int amount = randomBetween(minimumAmount, maximumAmount);
                for (int i = 0; i < amount && !Thread.currentThread().isInterrupted(); i++) {
                    int resourceType = variableResourceType
                            ? ThreadLocalRandom.current().nextInt(recurs.getResourceTypeCount())
                            : fixedResourceType;
                    recurs.consumeOne(resourceType);
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                int sleepTime = randomBetween(minimumSleepTimeMillis, maximumSleepTimeMillis);
                onSleepTimeChanged.accept(sleepTime);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                firstOperation = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private int randomBetween(int minimum, int maximum) {
        return (int) ThreadLocalRandom.current().nextLong((long) minimum, (long) maximum + 1);
    }
}
