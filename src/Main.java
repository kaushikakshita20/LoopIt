// 17.11.2026- AKSHITA KAUSHIK
import java.util.Scanner;
import model.User;
import model.Role;
import service.UserService;
import service.MemberDashboard;
import service.AdminDashboard;
import service.ResourceService;
import service.BookingService;
import service.WaitlistService;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService userService = new UserService();
        ResourceService resourceService = new ResourceService();
        WaitlistService waitlistService = new WaitlistService();
        BookingService bookingService = new BookingService(
                resourceService,
                waitlistService
        );

        while (true) {
            System.out.println("\n================================");
            System.out.println("            LOOP IT");
            System.out.println("       OWN LESS. SHARE MORE.");
            System.out.println("================================");
            System.out.println("1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Exit");
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
                    System.out.println("\n------ SIGN UP ------");
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter email: ");
                    String email = sc.nextLine();
                    System.out.print("Create password: ");
                    String password = sc.nextLine();
                    System.out.print("Confirm password: ");
                    String confirmPassword = sc.nextLine();
                    userService.register(
                            name, email, password, confirmPassword);
                    break;
                case 2:
                    System.out.println("\n------ LOGIN ------");
                    System.out.print("Enter email: ");
                    String loginEmail = sc.nextLine();
                    System.out.print("Enter password: ");
                    String loginPassword = sc.nextLine();
                    User loggedInUser = userService.login(
                            loginEmail, loginPassword);
                    if (loggedInUser != null) {

                        if (loggedInUser.getRole() == Role.ADMIN) {
                            AdminDashboard dashboard = new AdminDashboard(
                                    resourceService,
                                    bookingService,
                                    userService
                            );
                            dashboard.showDashboard(loggedInUser, sc);
                        } else {
                            MemberDashboard dashboard = new MemberDashboard(resourceService,bookingService);
                            dashboard.showDashboard(loggedInUser, sc);
                        }
                    }
                    break;
                case 3:
                    System.out.println("Thank you for using Loop It!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
