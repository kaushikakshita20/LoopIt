
package service;

import model.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class BookingService {

    private final ArrayList<Booking> bookings = new ArrayList<>();

    private final ResourceService resourceService;
    private final WaitlistService waitlistService;

    private int nextBookingId = 1;

    public BookingService(ResourceService resourceService,
                          WaitlistService waitlistService) {

        this.resourceService = resourceService;
        this.waitlistService = waitlistService;
    }

    // TASK 4: Borrow a resource
    public synchronized Booking borrowResource(User borrower, int resourceId) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return null;
        }

        Resource resource = resourceService.getResourceById(resourceId);

        if (resource == null) {
            System.out.println("Resource not found!");
            return null;
        }

        // If resource is unavailable, add user to waiting list
        if (!resource.isAvailable()) {
            System.out.println("Resource is unavailable.");

            waitlistService.addToWaitlist(resourceId, borrower);
            return null;
        }

        // Make resource unavailable
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

    // TASK 4: Return a borrowed resource
    public synchronized void returnResource(int bookingId, User borrower) {

        for (Booking booking : bookings) {

            if (booking.getBookingId() == bookingId
                    && booking.getStatus() == BookingStatus.BORROWED) {

                // Only the actual borrower can return this booking
                if (borrower == null
                        || booking.getBorrower().getUserId()
                        != borrower.getUserId()) {

                    System.out.println("You cannot return this booking!");
                    return;
                }

                // Update booking status and return date
                booking.markAsReturned();

                Resource resource = booking.getResource();
                int resourceId = resource.getResourceId();

                resource.setAvailable(true);

                System.out.println("Resource returned successfully!");

                // Check whether someone is waiting
                User nextUser = waitlistService.getNextUser(resourceId);

                if (nextUser != null) {

                    // Allocate resource to the next waiting user
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

                    System.out.println(
                            "Resource allocated to next waitlisted user: "
                                    + nextUser.getName()
                    );

                    System.out.println(
                            "New Booking ID: " + nextBooking.getBookingId()
                    );
                }

                return;
            }
        }

        System.out.println("Active booking not found!");
    }

    // Display all bookings
    public void viewBookings() {

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {

            System.out.println(
                    "Booking ID: " + booking.getBookingId()
                            + " | Borrower: " + booking.getBorrower().getName()
                            + " | Resource: " + booking.getResource().getName()
                            + " | Status: " + booking.getStatus()
                            + " | Due Date: " + booking.getDueDate()
            );
        }
    }

    // Display overdue bookings
    public void viewOverdueBookings() {

        boolean found = false;

        for (Booking booking : bookings) {

            if (booking.isOverdue()) {

                found = true;

                System.out.println(
                        "Overdue Booking ID: " + booking.getBookingId()
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

    public void viewActiveBookings(User borrower) {

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

}
