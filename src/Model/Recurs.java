package Model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;

public class Recurs {
    public static final int MAX_CAPACITY = 100;
    public static final int DEFAULT_MINIMUM = 1;
    public static final int DEFAULT_MAXIMUM = 50;
    private static final long STOCK_POLL_INTERVAL_MILLIS = 25;

    public enum CriticalSection {
        IDLE,
        PRODUCING,
        CONSUMING
    }

    private AtomicLongArray stock = new AtomicLongArray(1);
    private final AtomicLong totalProduced = new AtomicLong();
    private final AtomicLong totalConsumed = new AtomicLong();
    private int maximumStock = DEFAULT_MAXIMUM;
    private boolean stockControlEnabled = true;
    private boolean synchronizationEnabled = true;
    private Model.SimulationConfig.StockStrategy stockStrategy = Model.SimulationConfig.StockStrategy.WAIT_NOTIFY_ALL;
    private volatile Runnable onCountChanged = () -> {};
    private Consumer<CriticalSection> onCriticalSectionChanged = status -> {};
    private CriticalSection criticalSection = CriticalSection.IDLE;

    public void setOnCountChanged(Runnable r) {
        onCountChanged = r == null ? () -> {} : r;
    }

    public synchronized void setOnCriticalSectionChanged(Consumer<CriticalSection> listener) {
        onCriticalSectionChanged = listener == null ? status -> {} : listener;
        onCriticalSectionChanged.accept(criticalSection);
    }

    public void configure(Model.SimulationConfig config) {
        synchronized (this) {
            if (stock.length() != config.resourceTypeCount()) {
                stock = new AtomicLongArray(config.resourceTypeCount());
                totalProduced.set(0);
                totalConsumed.set(0);
            }
            maximumStock = (int) ThreadLocalRandom.current().nextLong(
                    config.minimumStockCapacity(), (long) config.maximumStockCapacity() + 1);
            stockControlEnabled = config.stockControlEnabled();
            synchronizationEnabled = config.synchronizationEnabled();
            stockStrategy = config.stockStrategy();
            notifyAll();
        }
        onCountChanged.run();
    }

    public void produceOne() throws InterruptedException {
        produceOne(0);
    }

    public void consumeOne() throws InterruptedException {
        consumeOne(0);
    }

    public void produceOne(int resourceType) throws InterruptedException {
        changeStock(true, resourceType);
    }

    public void consumeOne(int resourceType) throws InterruptedException {
        changeStock(false, resourceType);
    }

    private void changeStock(boolean producing, int resourceType) throws InterruptedException {
        if (resourceType < 0 || resourceType >= stock.length()) {
            throw new IllegalArgumentException("El tipo de recurso seleccionado no existe.");
        }
        boolean useWaitNotify = synchronizationEnabled
                && stockStrategy == Model.SimulationConfig.StockStrategy.WAIT_NOTIFY_ALL;
        while (true) {
            synchronized (this) {
                if (!stockControlEnabled || !isBlocked(producing, resourceType)) {
                    applyChange(producing, resourceType);
                    notifyAll();
                    return;
                }
                if (useWaitNotify) {
                    wait();
                    continue;
                }
            }
            TimeUnit.MILLISECONDS.sleep(STOCK_POLL_INTERVAL_MILLIS);
        }
    }

    private boolean isBlocked(boolean producing, int resourceType) {
        long current = stock.get(resourceType);
        return producing ? current >= maximumStock : current == 0;
    }

    private void applyChange(boolean producing, int resourceType) {
        setCriticalSection(producing ? CriticalSection.PRODUCING : CriticalSection.CONSUMING);
        try {
            stock.addAndGet(resourceType, producing ? 1 : -1);
            (producing ? totalProduced : totalConsumed).incrementAndGet();
            onCountChanged.run();
        } finally {
            setCriticalSection(CriticalSection.IDLE);
        }
    }

    private void setCriticalSection(CriticalSection status) {
        criticalSection = status;
        onCriticalSectionChanged.accept(status);
    }

    public long getContador() {
        return getTotalInStock();
    }

    public void setContador(int contador) {
        synchronized (this) {
            if (contador < 0 || contador > maximumStock) {
                throw new IllegalArgumentException("El stock debe estar entre 0 y " + maximumStock + ".");
            }
            for (int i = 0; i < stock.length(); i++) {
                stock.set(i, i == 0 ? contador : 0);
            }
            if (contador == 0) {
                totalProduced.set(0);
                totalConsumed.set(0);
            }
            notifyAll();
        }
        onCountChanged.run();
    }

    public long getTotalInStock() {
        long total = 0;
        for (int i = 0; i < stock.length(); i++) {
            total += stock.get(i);
        }
        return total;
    }

    public synchronized int getMaximumStock() {
        return maximumStock;
    }

    public long[] getStockByResourceType() {
        long[] counts = new long[stock.length()];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = stock.get(i);
        }
        return counts;
    }

    public int getResourceTypeCount() {
        return stock.length();
    }

    public long getTotalProduced() {
        return totalProduced.get();
    }

    public long getTotalConsumed() {
        return totalConsumed.get();
    }

}
