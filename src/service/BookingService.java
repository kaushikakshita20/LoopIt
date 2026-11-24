// RAJNI
package service;
import model.*;
import threading.BookingTask;
import threading.ProducerConsumer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BookingService {
    private final ArrayList<Booking> bookings = new ArrayList<>();
    private final ResourceService resourceService;
    private final WaitlistService waitlistService;
    private final ProducerConsumer bookingQueue;
    private int nextBookingId = 1;
    public BookingService(ResourceService resourceService,
                          WaitlistService waitlistService) {
        this.resourceService = resourceService;
        this.waitlistService = waitlistService;
        bookingQueue = new ProducerConsumer(10);
        bookingQueue.start();
    }
    public void requestBorrow(User borrower, int resourceId) {
        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        try {
            BookingTask task = new BookingTask(
                    this,
                    borrower,
                    resourceId
            );
            bookingQueue.produce(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(
                    "Booking request interrupted."
            );
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
    public void processBorrowRequest(User borrower, int resourceId) {
        borrowResource(borrower, resourceId);
    }
    public synchronized Booking borrowResource(User borrower,
                                               int resourceId) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return null;
        }
        Resource resource = resourceService.getResourceById(resourceId);
        if (resource == null) {
            System.out.println("Resource not found!");
            return null;
        }

        if (resource.getOwnerId() == borrower.getUserId()) {
            System.out.println("You cannot borrow your own resource!");
            return null;
        }
        if (!resource.isAvailable()) {
            System.out.println("Resource is unavailable.");
            waitlistService.addToWaitlist(
                    resourceId,
                    borrower
            );
            return null;
        }
        resource.setAvailable(false);
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);
        Booking booking = new Booking(
                nextBookingId++,
                borrower,
                resource,
                today,
                dueDate,
                null,
                BookingStatus.BORROWED
        );
        bookings.add(booking);
        System.out.println("\nBooking successful!");
        System.out.println("Booking ID: " + booking.getBookingId());
        System.out.println("Resource: " + resource.getName());
        System.out.println("Borrow Date: " + today);
        System.out.println("Due Date: " + dueDate);
        return booking;
    }
    public synchronized void returnResource(int bookingId,
                                            User borrower) {
        for (Booking booking : bookings) {
            if (booking.getBookingId() == bookingId
                    && booking.getStatus() == BookingStatus.BORROWED) {
                if (borrower == null || booking.getBorrower().getUserId() != borrower.getUserId()) {
                    System.out.println("You cannot return this booking!");
                    return;
                }

                booking.markAsReturned();
                Resource resource = booking.getResource();
                int resourceId = resource.getResourceId();
                resource.setAvailable(true);
                System.out.println("Resource returned successfully!");
                User nextUser = waitlistService.getNextUser(resourceId);
                if (nextUser != null) {
                    resource.setAvailable(false);
                    LocalDate today = LocalDate.now();
                    Booking nextBooking = new Booking(
                            nextBookingId++,
                            nextUser,
                            resource,
                            today,
                            today.plusDays(14),
                            null,
                            BookingStatus.BORROWED
                    );
                    bookings.add(nextBooking);
                    System.out.println("Resource allocated to next waitlisted user: " + nextUser.getName());
                    System.out.println("New Booking ID: " + nextBooking.getBookingId());
                }
                return;
            }
        }
        System.out.println("Active booking not found!");
    }
    public synchronized void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println(
                    "Booking ID: " + booking.getBookingId()
                            + " | Borrower: "
                            + booking.getBorrower().getName()
                            + " | Resource: "
                            + booking.getResource().getName()
                            + " | Status: " + booking.getStatus()
                            + " | Due Date: " + booking.getDueDate()
            );
        }
    }
    public synchronized void viewOverdueBookings() {
        boolean found = false;
        for (Booking booking : bookings) {
            if (booking.isOverdue()) {
                found = true;
                System.out.println(
                        "Overdue Booking ID: "
                                + booking.getBookingId()
                                + " | Borrower: "
                                + booking.getBorrower().getName()
                                + " | Resource: "
                                + booking.getResource().getName()
                );
            }
        }
        if (!found) {
            System.out.println("No overdue bookings.");
        }
    }
    public synchronized void viewActiveBookings(User borrower) {
        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }
        boolean found = false;
        for (Booking booking : bookings) {
            if (booking.getBorrower().getUserId()
                    == borrower.getUserId()
                    && booking.getStatus() == BookingStatus.BORROWED) {
                found = true;
                System.out.println(
                        "Booking ID: " + booking.getBookingId()
                                + " | Resource: "
                                + booking.getResource().getName()
                                + " | Due Date: "
                                + booking.getDueDate()
                );
            }
        }
        if (!found) {
            System.out.println("You have no active bookings.");
        }
    }

    // Display borrowing history
    public synchronized void viewBorrowingHistory(User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        boolean found = false;

        System.out.println("\n--- MY BORROWING HISTORY ---");

        for (Booking booking : bookings) {

            if (booking.getBorrower().getUserId()
                    == borrower.getUserId()) {

                found = true;

                System.out.println(
                        "Booking ID: " + booking.getBookingId()
                                + " | Resource: "
                                + booking.getResource().getName()
                                + " | Borrow Date: "
                                + booking.getBorrowDate()
                                + " | Due Date: "
                                + booking.getDueDate()
                                + " | Return Date: "
                                + booking.getReturnDate()
                                + " | Status: "
                                + booking.getStatus()
                );
            }
        }

        if (!found) {
            System.out.println("You have no borrowing history.");
        }
    }

    public synchronized int getTotalBookingCount() {
        return bookings.size();
    }

    public synchronized int getActiveBookingCount() {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.BORROWED) {
                count++;
            }
        }

        return count;
    }
    public synchronized int getReturnedBookingCount() {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.RETURNED) {
                count++;
            }
        }
        return count;
    }
    public synchronized int getOverdueBookingCount() {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.isOverdue()) {
                count++;
            }
        }
        return count;
    }
    public void shutdownBookingQueue() {
        bookingQueue.shutdown();
        try {
            bookingQueue.awaitTermination();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public synchronized List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }
}