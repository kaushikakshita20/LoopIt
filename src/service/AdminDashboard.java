// 20.11.2026 - AKSHITA KAUSHIK
package service;
import model.User;
import java.util.Scanner;
public class AdminDashboard {
    public void showDashboard(User user, Scanner sc) {
        while (true) {

            System.out.println("\n================================");
            System.out.println("             LOOP IT");
            System.out.println("         ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("--------------------------------");
            System.out.println("1. Add Resource");
            System.out.println("2. View All Resources");
            System.out.println("3. Search Resource");
            System.out.println("4. Update Resource");
            System.out.println("5. Delete Resource");
            System.out.println("6. View All Bookings");
            System.out.println("7. Manage Users");
            System.out.println("8. Generate Reports");
            System.out.println("9. Logout");
            System.out.println("================================");
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());

            } catch (NumberFormatException e) {

                System.out.println("Please enter a valid number!");
                continue;
            }
            switch (choice) {
                case 1:
                    System.out.println("Add Resource - Coming Soon!");
                    break;
                case 2:
                    System.out.println("View All Resources - Coming Soon!");
                    break;
                case 3:
                    System.out.println("Search Resource - Coming Soon!");
                    break;
                case 4:
                    System.out.println("Update Resource - Coming Soon!");
                    break;
                case 5:
                    System.out.println("Delete Resource - Coming Soon!");
                    break;
                case 6:
                    System.out.println("View All Bookings - Coming Soon!");
                    break;
                case 7:
                    System.out.println("Manage Users - Coming Soon!");
                    break;
                case 8:
                    System.out.println("Generate Reports - Coming Soon!");
                    break;
                case 9:
                    System.out.println("Admin logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
