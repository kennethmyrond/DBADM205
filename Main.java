import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("Main Menu:");
            System.out.println("  [1] - Employees");
            System.out.println("  [2] - Products");
            System.out.println("  [3] - Sales");
            System.out.println("  [0] - Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    employees.main(args);
                    break;
                case 2:
                    products.main(args);
                    break;
                case 3:
                    sales.main(args);
                    break;
                case 0:
                    System.out.println("Exiting the program...");
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }

            if (choice != 0) {
                System.out.println("Press Enter to continue...");
                sc.nextLine();
            }

        } while (choice != 0);

        sc.close();
    }
}
