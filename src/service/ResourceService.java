//YASHIKA GUPTA-ADD OF CRUD
//ISHITA

package service;

import model.Resource;
import model.User;
import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class ResourceService {

    // MySQL AUTO_INCREMENT generates IDs
    // This method is retained for compatibility with existing code
    public int generateResourceId() {
        return 0;
    }


    // ADD RESOURCE
    public boolean addResource(Resource resource, User owner) {

        if (resource == null || owner == null) {
            System.out.println("Invalid resource or owner!");
            return false;
        }

        if (resource.getOwnerId() != owner.getUserId()) {
            System.out.println("Invalid resource owner!");
            return false;
        }

        if (resource.getName() == null ||
                resource.getName().isBlank()) {
            System.out.println("Invalid resource name!");
            return false;
        }

        if (resource.getCategory() == null ||
                resource.getCategory().isBlank()) {
            System.out.println("Invalid category!");
            return false;
        }

        if (!resource.isValidPricing()) {
            System.out.println("Invalid borrowing type or pricing details!");
            return false;
        }

        String query = "INSERT INTO resources " +
                "(name, category, description, owner_id, available, " +
                "borrowing_type, price_per_day, security_deposit) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     query, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("Connected database: " + con.getCatalog());

            ps.setString(1, resource.getName());
            ps.setString(2, resource.getCategory());
            ps.setString(3, resource.getDescription());
            ps.setInt(4, resource.getOwnerId());
            ps.setBoolean(5, resource.isAvailable());
            ps.setString(6, resource.getBorrowingType());
            ps.setDouble(7, resource.getPricePerDay());
            ps.setDouble(8, resource.getSecurityDeposit());

            int rows = ps.executeUpdate();

            System.out.println("Rows inserted: " + rows);

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {

                    if (keys.next()) {
                        resource.setResourceId(keys.getInt(1));

                        System.out.println(
                                "Generated Resource ID: "
                                        + resource.getResourceId()
                        );
                    }
                }

                System.out.println("Resource uploaded successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error adding resource!");
            e.printStackTrace();
        }

        return false;
    }


    // VIEW ALL RESOURCES
    public void viewResources() {

        String query = "SELECT * FROM resources";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                Resource resource = extractResource(rs);

                displayResource(resource);
            }

            if (!found) {
                System.out.println("No resources available.");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing resources!");
            e.printStackTrace();
        }
    }


    // GET RESOURCE BY ID
    public Resource getResourceById(int id) {

        String query =
                "SELECT * FROM resources WHERE resource_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return extractResource(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching resource!");
            e.printStackTrace();
        }

        return null;
    }


    // GET RESOURCES BY OWNER
    public List<Resource> getResourcesByOwner(int ownerId) {

        List<Resource> result = new ArrayList<>();

        String query =
                "SELECT * FROM resources WHERE owner_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.add(extractResource(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching owner resources!");
            e.printStackTrace();
        }

        return result;
    }


    // SEARCH RESOURCE
    public List<Resource> searchResource(String keyword) {

        List<Resource> result = new ArrayList<>();

        if (keyword == null || keyword.isBlank()) {
            return result;
        }

        String query =
                "SELECT * FROM resources " +
                        "WHERE name LIKE ? OR category LIKE ? OR description LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String search = "%" + keyword.trim() + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.add(extractResource(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching resources!");
            e.printStackTrace();
        }

        return result;
    }


    
    public boolean updateResource(int id, String name,
                                  String category, String description) {

        if (name == null || name.isBlank()) {
            System.out.println("Resource name cannot be empty!");
            return false;
        }

        if (category == null || category.isBlank()) {
            System.out.println("Resource category cannot be empty!");
            return false;
        }

        if (description == null || description.isBlank()) {
            System.out.println("Resource description cannot be empty!");
            return false;
        }

        String query =
                "UPDATE resources SET name = ?, category = ?, " +
                        "description = ? WHERE resource_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Resource updated successfully!");
                return true;
            } else {
                System.out.println("Resource not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error updating resource!");
            e.printStackTrace();
        }

        return false;
    }


    
    public boolean removeResource(int id) {

        String query =
                "DELETE FROM resources WHERE resource_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Resource removed successfully!");
                return true;
            } else {
                System.out.println("Resource not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error deleting resource!");
            e.printStackTrace();
        }

        return false;
    }


    
    public void viewCategories() {

        String query =
                "SELECT DISTINCT category FROM resources";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            System.out.print("Categories: ");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.print(rs.getString("category") + " ");
            }

            if (!found) {
                System.out.print("No categories found.");
            }

            System.out.println();

        } catch (SQLException e) {
            System.out.println("Error viewing categories!");
            e.printStackTrace();
        }
    }


    
    public List<Resource> sortResourcesByName() {

        return getSortedResources("name ASC");
    }


    
    public List<Resource> sortResourcesByCategory() {

        return getSortedResources("category ASC");
    }


    
    public List<Resource> sortResourcesByNameDescending() {

        return getSortedResources("name DESC");
    }


   
    public List<Resource> sortResourcesById() {

        return getSortedResources("resource_id ASC");
    }


    private List<Resource> getSortedResources(String order) {

        List<Resource> result = new ArrayList<>();

        String query = "SELECT * FROM resources ORDER BY " + order;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(extractResource(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error sorting resources!");
            e.printStackTrace();
        }

        return result;
    }


     public List<Resource> getAvailableResources() {

        List<Resource> result = new ArrayList<>();

        String query =
                "SELECT * FROM resources WHERE available = TRUE";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(extractResource(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching available resources!");
            e.printStackTrace();
        }

        return result;
    }


    
    public Collection<Resource> getAllResources() {

        List<Resource> result = new ArrayList<>();

        String query = "SELECT * FROM resources";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(extractResource(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching all resources!");
            e.printStackTrace();
        }

        return result;
    }


    
    private Resource extractResource(ResultSet rs)
            throws SQLException {

        return new Resource(
                rs.getInt("resource_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getInt("owner_id"),
                rs.getBoolean("available"),
                rs.getString("borrowing_type"),
                rs.getDouble("price_per_day"),
                rs.getDouble("security_deposit")
        );
    }


    
    private void displayResource(Resource resource) {

        System.out.println(
                "ID: " + resource.getResourceId()
                        + " | Name: " + resource.getName()
                        + " | Category: " + resource.getCategory()
                        + " | Owner ID: " + resource.getOwnerId()
                        + " | Available: " + resource.isAvailable()
                        + " | Type: " + resource.getBorrowingType()
                        + " | Price/Day: Rs. " + resource.getPricePerDay()
                        + " | Deposit: Rs. " + resource.getSecurityDeposit()
        );
    }
}
