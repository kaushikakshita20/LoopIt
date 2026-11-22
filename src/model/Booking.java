package model;

import java.time.LocalDate;

public class Booking {

    private int bookingId;
    private User borrower;
    private Resource resource;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BookingStatus status;

    public Booking(int bookingId, User borrower, Resource resource,
                   LocalDate borrowDate, LocalDate dueDate,
                   LocalDate returnDate, BookingStatus status) {

        this.bookingId = bookingId;
        this.borrower = borrower;
        this.resource = resource;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public User getBorrower() {
        return borrower;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void markAsReturned() {
        this.returnDate = LocalDate.now();
        this.status = BookingStatus.RETURNED;
    }

    public boolean isOverdue() {
        return status == BookingStatus.BORROWED
                && LocalDate.now().isAfter(dueDate);
    }
}