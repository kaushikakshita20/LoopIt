// RAJNI
// YASHIKA GUPTA
package service;

import model.*;
import database.DBConnection;
import threading.BookingTask;
import threading.ProducerConsumer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService {

    private final ResourceService resourceService;
    private final WaitlistService waitlistService;
    private final ProducerConsumer bookingQueue;

    public BookingService(ResourceService resourceService, WaitlistService waitlistService) {
        this.resourceService = resourceService;
        this.waitlistService = waitlistService;

        bookingQueue = new ProducerConsumer(10, 3);
        bookingQueue.start();
    }

    // ================= REQUEST BORROW =================

    public void requestBorrow(User borrower, int resourceId) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        try {
            BookingTask task = new BookingTask(this, borrower, resourceId);
            bookingQueue.produce(task);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Booking request interrupted.");

        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void processBorrowRequest(User borrower, int resourceId) {
        borrowResource(borrower, resourceId);
    }

    // ================= BORROW RESOURCE =================

    public Booking borrowResource(User borrower, int resourceId) {

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

        System.out.println(Thread.currentThread().getName() + " received booking request for Resource ID: " + resourceId);

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String sqlCheck = "SELECT available FROM resources WHERE resource_id = ? FOR UPDATE";
        String sqlUpdate = "UPDATE resources SET available = 0 WHERE resource_id = ? AND available = 1";
        String sqlInsert = "INSERT INTO bookings (user_id, resource_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Lock the resource row until the transaction finishes.
                boolean available;

                try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {

                    ps.setInt(1, resourceId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            System.out.println("Resource not found!");
                            return null;
                        }

                        available = rs.getBoolean("available");
                    }
                }

                if (!available) {
                    con.rollback();
                    System.out.println("Resource is unavailable.");
                    waitlistService.addToWaitlist(resourceId, borrower);
                    return null;
                }

                // Update availability only if the resource is still available.
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {

                    ps.setInt(1, resourceId);

                    int updated = ps.executeUpdate();

                    if (updated == 0) {
                        con.rollback();
                        System.out.println("Resource is unavailable.");
                        waitlistService.addToWaitlist(resourceId, borrower);
                        return null;
                    }
                }

                // Insert the booking inside the same transaction.
                int bookingId;

                try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, borrower.getUserId());
                    ps.setInt(2, resourceId);
                    ps.setDate(3, Date.valueOf(today));
                    ps.setDate(4, Date.valueOf(dueDate));
                    ps.setString(5, BookingStatus.BORROWED.name());

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {

                        if (keys.next()) {
                            bookingId = keys.getInt(1);
                        } else {
                            throw new SQLException("Booking ID was not generated.");
                        }
                    }
                }

                // Commit availability update and booking insertion together.
                con.commit();

                resource.setAvailable(false);

                Booking booking = new Booking(bookingId, borrower, resource, today, dueDate, null, BookingStatus.BORROWED);

                System.out.println("\nBooking successful!");
                System.out.println("Booking ID: " + bookingId);
                System.out.println("Resource: " + resource.getName());
                System.out.println("Borrow Date: " + today);
                System.out.println("Due Date: " + dueDate);
                System.out.println(Thread.currentThread().getName() + " completed booking transaction.");

                return booking;

            } catch (SQLException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error while borrowing resource!");
            e.printStackTrace();
            return null;
        }
    }

    // ================= RETURN RESOURCE =================


    public void returnResource(int bookingId, User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        String sqlFindBooking = "SELECT resource_id FROM bookings WHERE booking_id = ? AND user_id = ? AND status = 'BORROWED'";
        String sqlLockResource = "SELECT resource_id FROM resources WHERE resource_id = ? FOR UPDATE";
        String sqlReturnBooking = "UPDATE bookings SET status = 'RETURNED', return_date = ? WHERE booking_id = ? AND user_id = ? AND status = 'BORROWED'";
        String sqlFindNext = "SELECT w.waitlist_id, u.user_id, u.name, u.email, u.password, u.role FROM waitlist w JOIN users u ON w.user_id = u.user_id WHERE w.resource_id = ? ORDER BY w.waitlist_id ASC LIMIT 1 FOR UPDATE";
        String sqlDeleteWaitlist = "DELETE FROM waitlist WHERE waitlist_id = ?";
        String sqlInsertBooking = "INSERT INTO bookings (user_id, resource_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateAvailability = "UPDATE resources SET available = ? WHERE resource_id = ?";

        int resourceId;
        int nextBookingId = -1;
        User nextUser = null;

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // 1. Find the active booking.
                try (PreparedStatement ps = con.prepareStatement(sqlFindBooking)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, borrower.getUserId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            System.out.println("Active booking not found or you cannot return this booking!");
                            return;
                        }
                        resourceId = rs.getInt("resource_id");
                    }
                }

                // 2. Lock the resource row.
                try (PreparedStatement ps = con.prepareStatement(sqlLockResource)) {
                    ps.setInt(1, resourceId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            System.out.println("Resource not found!");
                            return;
                        }
                    }
                }

                // 3. Mark the existing booking as returned.
                try (PreparedStatement ps = con.prepareStatement(sqlReturnBooking)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, bookingId);
                    ps.setInt(3, borrower.getUserId());

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        System.out.println("Booking could not be returned.");
                        return;
                    }
                }

                // 4. Fetch the first user in FIFO order and lock the waitlist row.
                int waitlistId = -1;

                try (PreparedStatement ps = con.prepareStatement(sqlFindNext)) {
                    ps.setInt(1, resourceId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            waitlistId = rs.getInt("waitlist_id");
                            nextUser = new User(
                                    rs.getInt("user_id"),
                                    rs.getString("name"),
                                    rs.getString("email"),
                                    rs.getString("password"),
                                    Role.valueOf(rs.getString("role"))
                            );
                        }
                    }
                }

                // 5. If someone is waiting, create their booking.
                if (nextUser != null) {

                    LocalDate today = LocalDate.now();
                    LocalDate dueDate = today.plusDays(14);

                    try (PreparedStatement ps = con.prepareStatement(sqlInsertBooking, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, nextUser.getUserId());
                        ps.setInt(2, resourceId);
                        ps.setDate(3, Date.valueOf(today));
                        ps.setDate(4, Date.valueOf(dueDate));
                        ps.setString(5, BookingStatus.BORROWED.name());

                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                nextBookingId = keys.getInt(1);
                            } else {
                                throw new SQLException("Next booking ID was not generated.");
                            }
                        }
                    }

                    // 6. Remove the promoted user from the waitlist.
                    try (PreparedStatement ps = con.prepareStatement(sqlDeleteWaitlist)) {
                        ps.setInt(1, waitlistId);

                        if (ps.executeUpdate() == 0) {
                            throw new SQLException("Waitlist entry could not be removed.");
                        }
                    }
                }

                // 7. Keep unavailable if allocated; otherwise make available.
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateAvailability)) {
                    ps.setBoolean(1, nextUser == null);
                    ps.setInt(2, resourceId);
                    ps.executeUpdate();
                }

                // 8. Commit all changes together.
                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

            // Update the in-memory resource after successful commit.
            Resource resource = resourceService.getResourceById(resourceId);

            if (resource != null) {
                resource.setAvailable(nextUser == null);
            }

            System.out.println("Resource returned successfully!");

            if (nextUser != null) {
                System.out.println("Resource allocated to next waitlisted user: " + nextUser.getName());
                System.out.println("New Booking ID: " + nextBookingId);
            } else {
                System.out.println("No users are waiting. Resource is now available.");
            }

        } catch (SQLException e) {
            System.out.println("Error while returning resource!");
            e.printStackTrace();
        }
    }


    // ================= UPDATE RESOURCE AVAILABILITY =================

    private void setResourceAvailability(int resourceId, boolean available) {

        String sql = "UPDATE resources SET available = ? WHERE resource_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, resourceId);
            ps.executeUpdate();

            Resource resource = resourceService.getResourceById(resourceId);

            if (resource != null) {
                resource.setAvailable(available);
            }

        } catch (SQLException e) {
            System.out.println("Error updating resource availability!");
            e.printStackTrace();
        }
    }

    // ================= GET BOOKING BY ID =================

    private Booking getBookingById(int bookingId) {

        String sql = "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int userId = rs.getInt("user_id");
                    int resourceId = rs.getInt("resource_id");

                    User borrower = getUserById(userId);
                    Resource resource = resourceService.getResourceById(resourceId);

                    return new Booking(rs.getInt("booking_id"), borrower, resource, rs.getDate("borrow_date").toLocalDate(), rs.getDate("due_date").toLocalDate(), rs.getDate("return_date") == null ? null : rs.getDate("return_date").toLocalDate(), BookingStatus.valueOf(rs.getString("status")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= GET USER BY ID =================

    private User getUserById(int userId) {

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("password"), Role.valueOf(rs.getString("role")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= VIEW ALL BOOKINGS =================

    public synchronized void viewBookings() {

        List<Booking> bookings = getAllBookings();

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {
            System.out.println("Booking ID: " + booking.getBookingId() + " | Borrower: " + booking.getBorrower().getName() + " | Resource: " + booking.getResource().getName() + " | Status: " + booking.getStatus() + " | Due Date: " + booking.getDueDate());
        }
    }

    // ================= OVERDUE BOOKINGS =================

    public synchronized void viewOverdueBookings() {

        List<Booking> bookings = getAllBookings();
        boolean found = false;

        for (Booking booking : bookings) {

            if (booking.isOverdue()) {
                found = true;
                System.out.println("Overdue Booking ID: " + booking.getBookingId() + " | Borrower: " + booking.getBorrower().getName() + " | Resource: " + booking.getResource().getName());
            }
        }

        if (!found) {
            System.out.println("No overdue bookings.");
        }
    }

    // ================= ACTIVE BOOKINGS =================

    public synchronized void viewActiveBookings(User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        String sql = "SELECT * FROM bookings WHERE user_id = ? AND status = 'BORROWED'";
        boolean found = false;

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, borrower.getUserId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    found = true;

                    Resource resource = resourceService.getResourceById(rs.getInt("resource_id"));

                    System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Resource: " + (resource == null ? "Unknown" : resource.getName()) + " | Due Date: " + rs.getDate("due_date"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!found) {
            System.out.println("You have no active bookings.");
        }
    }

    // ================= BORROWING HISTORY =================

    public synchronized void viewBorrowingHistory(User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        System.out.println("\n--- MY BORROWING HISTORY ---");

        String sql = "SELECT * FROM bookings WHERE user_id = ?";
        boolean found = false;

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, borrower.getUserId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    found = true;

                    Resource resource = resourceService.getResourceById(rs.getInt("resource_id"));

                    System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Resource: " + (resource == null ? "Unknown" : resource.getName()) + " | Borrow Date: " + rs.getDate("borrow_date") + " | Due Date: " + rs.getDate("due_date") + " | Return Date: " + rs.getDate("return_date") + " | Status: " + rs.getString("status"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!found) {
            System.out.println("You have no borrowing history.");
        }
    }

    // ================= BOOKING COUNTS =================

    public synchronized int getTotalBookingCount() {
        return getBookingCount("SELECT COUNT(*) FROM bookings");
    }

    public synchronized int getActiveBookingCount() {
        return getBookingCount("SELECT COUNT(*) FROM bookings WHERE status = 'BORROWED'");
    }

    public synchronized int getReturnedBookingCount() {
        return getBookingCount("SELECT COUNT(*) FROM bookings WHERE status = 'RETURNED'");
    }

    public synchronized int getOverdueBookingCount() {
        return getBookingCount("SELECT COUNT(*) FROM bookings WHERE status = 'BORROWED' AND due_date < CURDATE()");
    }

    private int getBookingCount(String sql) {

        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ================= GET ALL BOOKINGS =================

    public synchronized List<Booking> getAllBookings() {

        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY booking_id";

        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                User borrower = getUserById(rs.getInt("user_id"));
                Resource resource = resourceService.getResourceById(rs.getInt("resource_id"));

                if (borrower != null && resource != null) {

                    bookings.add(new Booking(rs.getInt("booking_id"), borrower, resource, rs.getDate("borrow_date").toLocalDate(), rs.getDate("due_date").toLocalDate(), rs.getDate("return_date") == null ? null : rs.getDate("return_date").toLocalDate(), BookingStatus.valueOf(rs.getString("status"))));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bookings!");
            e.printStackTrace();
        }

        return bookings;
    }

    // ================= SHUTDOWN QUEUE =================

    public void shutdownBookingQueue() {

        bookingQueue.shutdown();

        try {
            bookingQueue.awaitTermination();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
