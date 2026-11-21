//YASHIKA GUPTA
package database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection()) {

            if (con != null) {
                System.out.println("Connected to MySQL successfully!");
                System.out.println("Database: " + con.getCatalog());
            }

        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
    }
}