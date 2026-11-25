// AKSHITA KAUSHIK 24.11.2026
package threading;
import model.Resource;
import model.User;
import service.ResourceService;
import service.BookingService;
import service.WaitlistService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizationTest {

    public static void main(String[] args) throws InterruptedException {

        ResourceService resourceService = new ResourceService();
        WaitlistService waitlistService = new WaitlistService();

        // Creating resource owner
        User owner = new User(
                1, "Owner", "owner@gmail.com", "123", null
        );

        // Creating two borrowers
        User userA = new User(
                2, "User A", "a@gmail.com", "123", null
        );

        User userB = new User(
                3, "User B", "b@gmail.com", "123", null
        );

        // Creating one shared resource
        Resource resource = new Resource(
                101,
                "Scientific Calculator",
                "Electronics",
                "Casio Calculator",
                1,
                true
        );

        resourceService.addResource(resource, owner);

        BookingService bookingService =
                new BookingService(resourceService, waitlistService);

        // Both threads wait for the same starting signal
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successfulBookings = new AtomicInteger(0);

        Runnable taskA = () -> {
            try {
                ready.countDown();
                start.await();

                if (bookingService.borrowResource(userA, 101) != null) {
                    successfulBookings.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable taskB = () -> {
            try {
                ready.countDown();
                start.await();

                if (bookingService.borrowResource(userB, 101) != null) {
                    successfulBookings.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread threadA = new Thread(taskA, "User A Thread");
        Thread threadB = new Thread(taskB, "User B Thread");

        System.out.println("\nStarting concurrent borrowing test...\n");

        threadA.start();
        threadB.start();

        // Wait until both threads are ready
        ready.await();

        // Release both threads together
        start.countDown();

        threadA.join();
        threadB.join();

        System.out.println("\n========== TEST RESULTS ==========");

        System.out.println(
                "Successful bookings: " + successfulBookings.get()
        );

        System.out.println(
                "Total bookings: " + bookingService.getTotalBookingCount()
        );

        System.out.println(
                "Waitlist size: " + waitlistService.getWaitlistSize(101)
        );

        System.out.println(
                "Resource available: " + resource.isAvailable()
        );

        if (successfulBookings.get() == 1
                && bookingService.getTotalBookingCount() == 1
                && waitlistService.getWaitlistSize(101) == 1
                && !resource.isAvailable()) {

            System.out.println("\nSYNCHRONIZATION TEST PASSED!");

        } else {
            System.out.println("\nSYNCHRONIZATION TEST FAILED!");
        }

        bookingService.shutdownBookingQueue();
    }
}
