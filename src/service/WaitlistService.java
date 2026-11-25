// RAJNI
// 25.11.2026 - AKSHITA KAUSHIK (ADDED
package service;
import model.User;
import model.Role;
import database.DBConnection;
import java.sql.*;
public class WaitlistService {
    public void addToWaitlist(int resourceId, User user) {
        if (user == null) {
            System.out.println("Invalid user!");
            return;
        }
        String sql = "INSERT IGNORE INTO waitlist (resource_id, user_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, user.getUserId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Added to waiting list successfully!");
            } else {
                System.out.println("You are already in the waiting list!");
            }
        } catch (SQLException e) {
            System.out.println("Error adding user to waiting list!");
            e.printStackTrace();
        }
    }
    public User peekNextUser(int resourceId) {
        String sql = "SELECT user_id FROM waitlist WHERE resource_id = ? ORDER BY waitlist_id ASC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getUserById(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking waiting list!");
            e.printStackTrace();
        }
        return null;
    }
    public boolean removeNextUser(int resourceId, int expectedUserId) {
        String sqlFind = "SELECT waitlist_id, user_id FROM waitlist WHERE resource_id = ? ORDER BY waitlist_id ASC LIMIT 1";
        String sqlDelete = "DELETE FROM waitlist WHERE waitlist_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int waitlistId;
                try (PreparedStatement ps = con.prepareStatement(sqlFind)) {
                    ps.setInt(1, resourceId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        if (rs.getInt("user_id") != expectedUserId) {
                            con.rollback();
                            return false;
                        }
                        waitlistId = rs.getInt("waitlist_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                    ps.setInt(1, waitlistId);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error removing user from waiting list!");
            e.printStackTrace();
            return false;
        }
    }
    public User getNextUser(int resourceId) {
        User nextUser = peekNextUser(resourceId);
        if (nextUser != null) {
            removeNextUser(resourceId, nextUser.getUserId());
        }
        return nextUser;
    }
    public void viewWaitlist(int resourceId) {
        String sql = "SELECT u.user_id, u.name FROM waitlist w JOIN users u ON w.user_id = u.user_id WHERE w.resource_id = ? ORDER BY w.waitlist_id ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                int position = 1;
                System.out.println("Waiting List for Resource ID: " + resourceId);
                while (rs.next()) {
                    found = true;
                    System.out.println(position++ + ". " + rs.getString("name") + " (User ID: " + rs.getInt("user_id") + ")");
                }
                if (!found) {
                    System.out.println("No users in the waiting list.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error viewing waiting list!");
            e.printStackTrace();
        }
    }
    public int getWaitlistSize(int resourceId) {
        String sql = "SELECT COUNT(*) FROM waitlist WHERE resource_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting waiting list!");
            e.printStackTrace();
        }
        return 0;
    }
    private User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("password"), Role.valueOf(rs.getString("role")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user details!");
            e.printStackTrace();
        }
        return null;
    }
}