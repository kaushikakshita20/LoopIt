// 17.11.2026- AKSHITA KAUSHIK
package service;
import model.User;
import java.util.Scanner;
public class MemberDashboard {
    public void showDashboard(User user, Scanner sc) {
        while (true) {
            System.out.println("\n================================");
            System.out.println("             LOOP IT");
            System.out.println("        MEMBER DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + user.getName() + "!");
            System.out.println("--------------------------------");
            System.out.println("1. Browse Resources");
            System.out.println("2. Search Resources");
            System.out.println("3. Lend a Resource");
            System.out.println("4. Borrow a Resource");
            System.out.println("5. Return a Resource");
            System.out.println("6. My Borrowing History");
            System.out.println("7. Logout");
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
                    System.out.println("Browse Resources - Coming Soon!");
                    break;
                case 2:
                    System.out.println("Search Resources - Coming Soon!");
                    break;
                case 3:
                    System.out.println("Lend Resource - Coming Soon!");
                    break;
                case 4:
                    System.out.println("Borrow Resource - Coming Soon!");
                    break;
                case 5:
                    System.out.println("Return Resource - Coming Soon!");
                    break;
                case 6:
                    System.out.println("Borrowing History - Coming Soon!");
                    break;
                case 7:
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
