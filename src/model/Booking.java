// 19.11.2026 Rajni

package model;

import java.time.LocalDate;

public class Booking {

    // Variables
    private int bookingId;
    private User borrower;
    private Resource resource;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BookingStatus status;


    // Constructor
    public Booking(int bookingId, User borrower, Resource resource,
                   LocalDate borrowDate, LocalDate dueDate,
                   LocalDate returnDate, BookingStatus status) {
    }

    public int getBookingId() {
        return 0;
    }

    public User getBorrower() {
        return null;
    }

    public Resource getResource() {
        return null;
    }

    public LocalDate getBorrowDate() {
        return null;
    }

    public LocalDate getDueDate() {
        return null;
    }

    public LocalDate getReturnDate() {
        return null;
    }

    public BookingStatus getStatus() {
        return null;
    }

    public void setReturnDate(LocalDate returnDate) {
    }

    public void setStatus(BookingStatus status) {
    }

    public void markAsReturned() {
    }

    public boolean isOverdue() {
        return false;
    }
}
