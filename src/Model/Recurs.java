package Model;

import Helpers.CriticalSection;
import Helpers.StockStrategy;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;
public class Recurs {
    //Variables para View
    public static final int MAX_CAPACITY = 100;
    public static final int DEFAULT_MINIMUM = 1;
    public static final int DEFAULT_MAXIMUM = 50;
    private static final long STOCK_POLL_INTERVAL_MILLIS = 25; //usado en changeStock como tiempo ms.

    //Cosas de stock
    private AtomicLongArray stock = new AtomicLongArray(1);
    private final AtomicLong totalProduced = new AtomicLong();
    private final AtomicLong totalConsumed = new AtomicLong();
    private int maximumStock = DEFAULT_MAXIMUM;
    private boolean stockControlEnabled = true;
    private boolean synchronizationEnabled = true;
    private StockStrategy stockStrategy = StockStrategy.WAIT_NOTIFY_ALL;
    private volatile Runnable onCountChanged = () -> {};

    //Cosas del semáforo
    private Consumer<CriticalSection> onCriticalSectionChanged = status -> {};
    private CriticalSection criticalSection = CriticalSection.IDLE;

    //===================(Métodos)==========================//

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

    // Estos 2 se encargan de seleccionar el tipo de recurso y cambiar el estado de producción
    public void produceOne(int resourceType) throws InterruptedException {
        changeStock(true, resourceType);
    }

    public void consumeOne(int resourceType) throws InterruptedException {
        changeStock(false, resourceType);
    }

    /** Comprueba si la producción está bloqueada o no. */
    private boolean isBlocked(boolean producing, int resourceType) {
        long current = stock.get(resourceType);
        return producing ? current >= maximumStock : current == 0;
    }

    /** Método principal pensado para el control de stock con las validaciones importantes */
    private void changeStock(boolean producing, int resourceType) throws InterruptedException {
        if (resourceType < 0 || resourceType >= stock.length()) {
            throw new IllegalArgumentException("El tipo de recurso seleccionado no existe.");
        }
        boolean useWaitNotify = synchronizationEnabled
                && stockStrategy == StockStrategy.WAIT_NOTIFY_ALL;
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

    // Métodos para notificar cambios para el semáforo

    /** Método para controlar el semáforo mediante 
     * enum CriticalSection, también para añadir cambios dentro de la lista stock e incrementar 
     * el total de producido y consumido */
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

    public synchronized void setOnCriticalSectionChanged(Consumer<CriticalSection> listener) {
        onCriticalSectionChanged = listener == null ? status -> {} : listener;
        onCriticalSectionChanged.accept(criticalSection);
    }

    // Getters y setters

    public long getContador() {
        return getTotalInStock();
    }

    public void setOnCountChanged(Runnable r) {
        onCountChanged = r == null ? () -> {} : r;
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
