// RAJNI
//YASHIKA GUPTA
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

    public BookingService(ResourceService resourceService,
                          WaitlistService waitlistService) {

        this.resourceService = resourceService;
        this.waitlistService = waitlistService;

        bookingQueue = new ProducerConsumer(10);
        bookingQueue.start();
    }

    // ================= REQUEST BORROW =================

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
            System.out.println("Booking request interrupted.");

        } catch (IllegalStateException e) {

            System.out.println(e.getMessage());
        }
    }

    public void processBorrowRequest(User borrower, int resourceId) {
        borrowResource(borrower, resourceId);
    }

    // ================= BORROW RESOURCE =================

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

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String sqlCheck =
                "SELECT available FROM resources WHERE resource_id = ?";

        String sqlUpdate =
                "UPDATE resources SET available = 0 " +
                        "WHERE resource_id = ? AND available = 1";

        String sqlInsert =
                "INSERT INTO bookings " +
                        "(user_id, resource_id, borrow_date, due_date, status) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Check availability directly in the database
                try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {

                    ps.setInt(1, resourceId);

                    ResultSet rs = ps.executeQuery();

                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("Resource not found!");
                        return null;
                    }

                    if (rs.getInt("available") == 0) {
                        con.rollback();

                        System.out.println("Resource is unavailable.");

                        waitlistService.addToWaitlist(
                                resourceId,
                                borrower
                        );

                        return null;
                    }
                }

                // Reserve resource in database
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {

                    ps.setInt(1, resourceId);

                    int updated = ps.executeUpdate();

                    if (updated == 0) {
                        con.rollback();

                        System.out.println("Resource is unavailable.");
                        waitlistService.addToWaitlist(
                                resourceId,
                                borrower
                        );

                        return null;
                    }
                }

                // Insert booking
                int bookingId;

                try (PreparedStatement ps = con.prepareStatement(
                        sqlInsert,
                        Statement.RETURN_GENERATED_KEYS
                )) {

                    ps.setInt(1, borrower.getUserId());
                    ps.setInt(2, resourceId);
                    ps.setDate(3, Date.valueOf(today));
                    ps.setDate(4, Date.valueOf(dueDate));
                    ps.setString(5, BookingStatus.BORROWED.name());

                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();

                    if (keys.next()) {
                        bookingId = keys.getInt(1);
                    } else {
                        throw new SQLException(
                                "Booking ID was not generated."
                        );
                    }
                }

                con.commit();

                resource.setAvailable(false);

                Booking booking = new Booking(
                        bookingId,
                        borrower,
                        resource,
                        today,
                        dueDate,
                        null,
                        BookingStatus.BORROWED
                );

                System.out.println("\nBooking successful!");
                System.out.println("Booking ID: " + bookingId);
                System.out.println("Resource: " + resource.getName());
                System.out.println("Borrow Date: " + today);
                System.out.println("Due Date: " + dueDate);

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

    public synchronized void returnResource(int bookingId,
                                            User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        String sql =
                "UPDATE bookings " +
                        "SET status = 'RETURNED', return_date = ? " +
                        "WHERE booking_id = ? " +
                        "AND user_id = ? " +
                        "AND status = 'BORROWED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, bookingId);
            ps.setInt(3, borrower.getUserId());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                System.out.println(
                        "Active booking not found or you cannot return this booking!"
                );
                return;
            }

            Booking booking = getBookingById(bookingId);

            if (booking == null) {
                System.out.println("Booking returned successfully!");
                return;
            }

            Resource resource = booking.getResource();
            int resourceId = resource.getResourceId();

            setResourceAvailability(resourceId, true);

            System.out.println("Resource returned successfully!");

            // Check waitlist
            User nextUser = waitlistService.getNextUser(resourceId);

            if (nextUser != null) {

                Booking nextBooking = borrowResource(
                        nextUser,
                        resourceId
                );

                if (nextBooking != null) {

                    System.out.println(
                            "Resource allocated to next waitlisted user: "
                                    + nextUser.getName()
                    );

                    System.out.println(
                            "New Booking ID: "
                                    + nextBooking.getBookingId()
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println("Error while returning resource!");
            e.printStackTrace();
        }
    }

    // ================= UPDATE RESOURCE AVAILABILITY =================

    private void setResourceAvailability(int resourceId,
                                         boolean available) {

        String sql =
                "UPDATE resources SET available = ? " +
                        "WHERE resource_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, resourceId);

            ps.executeUpdate();

            Resource resource =
                    resourceService.getResourceById(resourceId);

            if (resource != null) {
                resource.setAvailable(available);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error updating resource availability!"
            );

            e.printStackTrace();
        }
    }

    // ================= GET BOOKING BY ID =================

    private Booking getBookingById(int bookingId) {

        String sql =
                "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int userId = rs.getInt("user_id");
                int resourceId = rs.getInt("resource_id");

                User borrower = getUserById(userId);

                Resource resource =
                        resourceService.getResourceById(resourceId);

                return new Booking(
                        rs.getInt("booking_id"),
                        borrower,
                        resource,
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getDate("return_date") == null
                                ? null
                                : rs.getDate("return_date").toLocalDate(),
                        BookingStatus.valueOf(rs.getString("status"))
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }

    // ================= GET USER BY ID =================

    private User getUserById(int userId) {

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                );
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

            System.out.println(
                    "Booking ID: " + booking.getBookingId()
                            + " | Borrower: "
                            + booking.getBorrower().getName()
                            + " | Resource: "
                            + booking.getResource().getName()
                            + " | Status: "
                            + booking.getStatus()
                            + " | Due Date: "
                            + booking.getDueDate()
            );
        }
    }

    // ================= OVERDUE BOOKINGS =================

    public synchronized void viewOverdueBookings() {

        List<Booking> bookings = getAllBookings();

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

    // ================= ACTIVE BOOKINGS =================

    public synchronized void viewActiveBookings(User borrower) {

        if (borrower == null) {
            System.out.println("Invalid user!");
            return;
        }

        String sql =
                "SELECT * FROM bookings " +
                        "WHERE user_id = ? AND status = 'BORROWED'";

        boolean found = false;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, borrower.getUserId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                found = true;

                Resource resource =
                        resourceService.getResourceById(
                                rs.getInt("resource_id")
                        );

                System.out.println(
                        "Booking ID: " + rs.getInt("booking_id")
                                + " | Resource: "
                                + (resource == null
                                ? "Unknown"
                                : resource.getName())
                                + " | Due Date: "
                                + rs.getDate("due_date")
                );
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

        String sql =
                "SELECT * FROM bookings WHERE user_id = ?";

        boolean found = false;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, borrower.getUserId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                found = true;

                Resource resource =
                        resourceService.getResourceById(
                                rs.getInt("resource_id")
                        );

                System.out.println(
                        "Booking ID: " + rs.getInt("booking_id")
                                + " | Resource: "
                                + (resource == null
                                ? "Unknown"
                                : resource.getName())
                                + " | Borrow Date: "
                                + rs.getDate("borrow_date")
                                + " | Due Date: "
                                + rs.getDate("due_date")
                                + " | Return Date: "
                                + rs.getDate("return_date")
                                + " | Status: "
                                + rs.getString("status")
                );
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

        return getBookingCount(
                "SELECT COUNT(*) FROM bookings"
        );
    }

    public synchronized int getActiveBookingCount() {

        return getBookingCount(
                "SELECT COUNT(*) FROM bookings WHERE status = 'BORROWED'"
        );
    }

    public synchronized int getReturnedBookingCount() {

        return getBookingCount(
                "SELECT COUNT(*) FROM bookings WHERE status = 'RETURNED'"
        );
    }

    public synchronized int getOverdueBookingCount() {

        return getBookingCount(
                "SELECT COUNT(*) FROM bookings " +
                        "WHERE status = 'BORROWED' AND due_date < CURDATE()"
        );
    }

    private int getBookingCount(String sql) {

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

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

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                User borrower =
                        getUserById(rs.getInt("user_id"));

                Resource resource =
                        resourceService.getResourceById(
                                rs.getInt("resource_id")
                        );

                if (borrower != null && resource != null) {

                    bookings.add(
                            new Booking(
                                    rs.getInt("booking_id"),
                                    borrower,
                                    resource,
                                    rs.getDate("borrow_date").toLocalDate(),
                                    rs.getDate("due_date").toLocalDate(),
                                    rs.getDate("return_date") == null
                                            ? null
                                            : rs.getDate("return_date").toLocalDate(),
                                    BookingStatus.valueOf(
                                            rs.getString("status")
                                    )
                            )
                    );
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
