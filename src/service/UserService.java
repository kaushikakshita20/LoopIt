// 20.11.2026 - Akshita Kaushik
// 22.11.2026 - Yashika Gupta


package service;

import model.User;
import model.Role;
import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class UserService {

    public UserService() {
        createAdminIfNotExists();
    }

    // ================= CREATE ADMIN =================

    private void createAdminIfNotExists() {

        String checkQuery =
                "SELECT user_id FROM users WHERE email = ?";

        String insertQuery =
                "INSERT INTO users (name, email, password, role) " +
                        "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkPs = con.prepareStatement(checkQuery)) {

            checkPs.setString(1, "admin@loopit.com");

            try (ResultSet rs = checkPs.executeQuery()) {

                if (!rs.next()) {

                    try (PreparedStatement insertPs =
                                 con.prepareStatement(insertQuery)) {

                        insertPs.setString(1, "Loop It Admin");
                        insertPs.setString(2, "admin@loopit.com");
                        insertPs.setString(3, "Admin@123");
                        insertPs.setString(4, Role.ADMIN.name());

                        insertPs.executeUpdate();

                        System.out.println(
                                "Admin account created in database!"
                        );
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println("Admin initialization failed!");
            e.printStackTrace();
        }
    }

    // ================= REGISTER =================

    public boolean register(String name, String email,
                            String password, String confirmPassword) {

        if (name == null || email == null ||
                password == null || confirmPassword == null) {

            System.out.println("All fields are required!");
            return false;
        }

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        if (name.isBlank() || email.isBlank() ||
                password.isBlank() || confirmPassword.isBlank()) {

            System.out.println("All fields are required!");
            return false;
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            System.out.println("Invalid email!");
            return false;
        }

        if (!password.equals(confirmPassword)) {

            System.out.println("Passwords do not match!");
            return false;
        }

        String checkQuery =
                "SELECT user_id FROM users WHERE email = ?";

        String insertQuery =
                "INSERT INTO users (name, email, password, role) " +
                        "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkPs =
                     con.prepareStatement(checkQuery)) {

            checkPs.setString(1, email);

            try (ResultSet rs = checkPs.executeQuery()) {

                if (rs.next()) {

                    System.out.println("Email already registered!");
                    return false;
                }
            }

            try (PreparedStatement insertPs =
                         con.prepareStatement(
                                 insertQuery,
                                 Statement.RETURN_GENERATED_KEYS
                         )) {

                insertPs.setString(1, name);
                insertPs.setString(2, email);
                insertPs.setString(3, password);
                insertPs.setString(4, Role.MEMBER.name());

                insertPs.executeUpdate();

                try (ResultSet keys = insertPs.getGeneratedKeys()) {

                    if (keys.next()) {

                        int userId = keys.getInt(1);

                        System.out.println(
                                "Account created successfully!"
                        );

                        System.out.println(
                                "Your User ID is: " + userId
                        );
                    }
                }

                return true;
            }

        } catch (SQLException e) {

            System.out.println("Registration failed!");
            e.printStackTrace();
            return false;
        }
    }

    // ================= LOGIN =================

    public User login(String email, String password) {

        if (email == null || password == null ||
                email.isBlank() || password.isBlank()) {

            System.out.println("Email and password are required!");
            return null;
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        String query =
                "SELECT * FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    System.out.println("Account not found!");
                    return null;
                }

                String storedPassword = rs.getString("password");

                if (!storedPassword.equals(password)) {

                    System.out.println("Incorrect password!");
                    return null;
                }

                // CHECK USER STATUS
                String status = rs.getString("status");

                if ("BLOCKED".equalsIgnoreCase(status)) {

                    System.out.println(
                            "Your account has been blocked by the admin."
                    );

                    System.out.println(
                            "You cannot login. Please contact the admin."
                    );

                    return null;
                }

                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                );

                System.out.println("Login successful!");

                return user;
            }

        } catch (SQLException e) {

            System.out.println("Login failed!");
            e.printStackTrace();
            return null;
        }
    }

    // ================= VIEW ALL USERS =================

    public void viewAllUsers() {

        String query =
                "SELECT user_id, name, email, role, status FROM users";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== REGISTERED USERS ==========");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println(
                        "User ID: " + rs.getInt("user_id") +
                                " | Name: " + rs.getString("name") +
                                " | Email: " + rs.getString("email") +
                                " | Role: " + rs.getString("role") +
                                " | Status: " + rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No users found.");
            }

        } catch (SQLException e) {

            System.out.println("Unable to retrieve users!");
            e.printStackTrace();
        }
    }

    // ================= BLOCK USER =================

    public void blockUser(int userId) {

        // Admin cannot block another admin
        String checkQuery =
                "SELECT role, status FROM users WHERE user_id = ?";

        String updateQuery =
                "UPDATE users SET status = 'BLOCKED' " +
                        "WHERE user_id = ? AND role = 'MEMBER'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkPs =
                     con.prepareStatement(checkQuery)) {

            checkPs.setInt(1, userId);

            try (ResultSet rs = checkPs.executeQuery()) {

                if (!rs.next()) {

                    System.out.println("User not found!");
                    return;
                }

                if ("ADMIN".equalsIgnoreCase(rs.getString("role"))) {

                    System.out.println("Admin accounts cannot be blocked!");
                    return;
                }

                if ("BLOCKED".equalsIgnoreCase(rs.getString("status"))) {

                    System.out.println("User is already blocked!");
                    return;
                }
            }

            try (PreparedStatement updatePs =
                         con.prepareStatement(updateQuery)) {

                updatePs.setInt(1, userId);

                int rows = updatePs.executeUpdate();

                if (rows > 0) {

                    System.out.println(
                            "User blocked successfully!"
                    );
                } else {

                    System.out.println("Unable to block user!");
                }
            }

        } catch (SQLException e) {

            System.out.println("Error while blocking user!");
            e.printStackTrace();
        }
    }

    // ================= UNBLOCK USER =================

    public void unblockUser(int userId) {

        String checkQuery =
                "SELECT role, status FROM users WHERE user_id = ?";

        String updateQuery =
                "UPDATE users SET status = 'ACTIVE' " +
                        "WHERE user_id = ? AND role = 'MEMBER'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkPs =
                     con.prepareStatement(checkQuery)) {

            checkPs.setInt(1, userId);

            try (ResultSet rs = checkPs.executeQuery()) {

                if (!rs.next()) {

                    System.out.println("User not found!");
                    return;
                }

                if ("ADMIN".equalsIgnoreCase(rs.getString("role"))) {

                    System.out.println(
                            "Admin accounts cannot be unblocked here!"
                    );

                    return;
                }

                if ("ACTIVE".equalsIgnoreCase(rs.getString("status"))) {

                    System.out.println("User is already active!");
                    return;
                }
            }

            try (PreparedStatement updatePs =
                         con.prepareStatement(updateQuery)) {

                updatePs.setInt(1, userId);

                int rows = updatePs.executeUpdate();

                if (rows > 0) {

                    System.out.println(
                            "User unblocked successfully!"
                    );
                } else {

                    System.out.println("Unable to unblock user!");
                }
            }

        } catch (SQLException e) {

            System.out.println("Error while unblocking user!");
            e.printStackTrace();
        }
    }
}