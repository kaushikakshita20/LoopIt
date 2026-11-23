// 23.11.2026-AKSHITA KAUSHIK
package threading;
import java.util.LinkedList;
import java.util.Queue;
public class ProducerConsumer {
    private final Queue<BookingTask> queue = new LinkedList<>();
    private final int CAPACITY;
    private boolean accepting = true;
    private boolean started = false;
    private Thread worker;
    public ProducerConsumer(int capacity) {
        if (capacity <= 0) { throw new IllegalArgumentException(
                    "Queue capacity must be greater than zero."
            );
        }
        this.CAPACITY = capacity;
    }
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        worker = new Thread(() -> {
            try {
                while (true) {
                    BookingTask task = consume();
                    if (task == null) {
                        break;
                    }
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "LoopIt-Booking-Worker");
        worker.setDaemon(true);
        worker.start();
    }
    public synchronized void produce(BookingTask task) throws InterruptedException {
        if (task == null) {
            throw new IllegalArgumentException(
                    "Booking task cannot be null."
            );
        }
        while (queue.size() == CAPACITY && accepting) {
            System.out.println("Booking queue full. Waiting...");
            wait();
        }
        if (!accepting) {
            throw new IllegalStateException(
                    "Booking queue is closed."
            );
        }
        queue.offer(task);
        System.out.println("Booking request added to processing queue."
        );
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
        notifyAll();
    }
    public void awaitTermination() throws InterruptedException {
        if (worker != null) {
            worker.join();
        }
    }
}