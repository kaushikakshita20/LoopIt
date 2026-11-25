// 23.11.2026 - AKSHITA KAUSHIK
package threading;
import java.util.LinkedList;
import java.util.Queue;
public class ProducerConsumer {
    private final Queue<BookingTask> queue = new LinkedList<>();
    private final int CAPACITY;
    private final int WORKER_COUNT;
    private boolean accepting = true;
    private boolean started = false;
    private Thread[] workers;
    public ProducerConsumer(int capacity, int workerCount) {
        if (capacity <= 0 || workerCount <= 0) {
            throw new IllegalArgumentException("Capacity and worker count must be greater than zero.");
        }
        this.CAPACITY = capacity;
        this.WORKER_COUNT = workerCount;
    }
    public synchronized void start() {
        if (started) {
            return;
        }
        if (!accepting) {
            throw new IllegalStateException("Booking queue has already been shut down.");
        }
        started = true;
        workers = new Thread[WORKER_COUNT];
        for (int i = 0; i < WORKER_COUNT; i++) {
            workers[i] = new Thread(() -> {
                try {
                    while (true) {
                        BookingTask task = consume();
                        if (task == null) {
                            break;
                        }
                        System.out.println(Thread.currentThread().getName() + " is processing a booking request.");
                        try {
                            task.run();
                        } catch (RuntimeException e) {
                            System.out.println("Booking task failed: " + e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().getName() + " was interrupted.");
                }
            }, "LoopIt-Booking-Worker-" + (i + 1));

            workers[i].start();
        }
    }
    public synchronized void produce(BookingTask task) throws InterruptedException {
        if (task == null) {
            throw new IllegalArgumentException("Booking task cannot be null.");
        }
        if (!started) {
            throw new IllegalStateException("Booking workers have not been started.");
        }
        while (queue.size() >= CAPACITY && accepting) {
            System.out.println("Booking queue full. Waiting for available space...");
            wait();
        }
        if (!accepting) {
            throw new IllegalStateException("Booking queue is closed.");
        }
        queue.offer(task);
        System.out.println("Booking request added to processing queue.");
        notifyAll();
    }
    private synchronized BookingTask consume() throws InterruptedException {
        while (queue.isEmpty() && accepting) {
            wait();
        }
        if (queue.isEmpty() && !accepting) {
            return null;
        }
        BookingTask task = queue.poll();
        notifyAll();
        return task;
    }
    public synchronized void shutdown() {
        accepting = false;
        System.out.println("Booking queue shutdown initiated.");
        notifyAll();
    }
    public void awaitTermination() throws InterruptedException {
        Thread[] currentWorkers;

        synchronized (this) {
            if (workers == null) {
                return;
            }
            currentWorkers = workers.clone();
        }
        for (Thread worker : currentWorkers) {
            if (worker != null) {
                worker.join();
            }
        }
        System.out.println("All booking workers have terminated.");
    }
}
