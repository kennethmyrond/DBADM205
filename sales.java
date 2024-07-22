import java.sql.*;
import java.util.Scanner;

public class sales {

    // Database URL
    public String url = "jdbc:mysql://127.0.0.1:3306/db2.6test";
    // Database credentials
    public String username = "root";
    public String password = "root1234";

    public int addOrder() {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("Enter Order Number (0 if new):");
        int v_orderNumber = sc.nextInt();
        sc.nextLine(); // Consume newline

        System.out.println("Enter Required Date (YYYY-MM-DD):");
        String v_requiredDate = sc.nextLine() + " 00:00:00"; // Convert to datetime format

        System.out.println("Enter Customer Number:");
        int v_customerNumber = sc.nextInt();
        sc.nextLine(); // Consume newline

        System.out.println("Enter Product Code:");
        String v_productCode = sc.nextLine();

        System.out.println("Enter Quantity Ordered:");
        int v_quantityOrdered = sc.nextInt();
        sc.nextLine(); // Consume newline

        System.out.println("Enter Price Each:");
        double v_priceEach = sc.nextDouble();
        sc.nextLine(); // Consume newline

        Connection conn = null;
        PreparedStatement pstmt = null;
        int nestedCall = 0;

        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            int newOrderNumber_var = v_orderNumber;
            int orderLineNumber_var = 0;

            // Check for nested calls
            String checkNestedCallSQL = "SELECT COUNT(1) FROM information_schema.innodb_trx WHERE trx_mysql_thread_id = CONNECTION_ID()";
            pstmt = conn.prepareStatement(checkNestedCallSQL);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nestedCall = rs.getInt(1);
            }
            rs.close();
            pstmt.close();

            if (nestedCall == 0) {
                conn.setAutoCommit(false);
            }

            // Generate new OrderNumber if needed
            if (v_orderNumber == 0) {
                String lockOrdersSQL = "SELECT * FROM orders FOR UPDATE";
                pstmt = conn.prepareStatement(lockOrdersSQL);
                pstmt.executeQuery();
                pstmt.close();

                String getMaxOrderNumberSQL = "SELECT MAX(orderNumber) + 1 FROM orders";
                pstmt = conn.prepareStatement(getMaxOrderNumberSQL);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    newOrderNumber_var = rs.getInt(1);
                }
                rs.close();
                pstmt.close();

                if (newOrderNumber_var == 0) {
                    newOrderNumber_var = 9001;
                }

                String insertOrderSQL = "INSERT INTO orders (orderNumber, orderDate, requiredDate, status, customerNumber) VALUES (?, NOW(), ?, 'In Process', ?)";
                pstmt = conn.prepareStatement(insertOrderSQL);
                pstmt.setInt(1, newOrderNumber_var);
                pstmt.setString(2, v_requiredDate);
                pstmt.setInt(3, v_customerNumber);
                pstmt.executeUpdate();
                pstmt.close();
            } else {
                // Check if order exists
                String checkOrderSQL = "SELECT COUNT(1) FROM orders WHERE orderNumber = ?";
                pstmt = conn.prepareStatement(checkOrderSQL);
                pstmt.setInt(1, v_orderNumber);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1) == 0) {
                        System.out.println("Order does not exist!");
                        return 0;
                    }
                }
                rs.close();
                pstmt.close();
            }

            // Get old quantity from items
            String lockItemsSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
            pstmt = conn.prepareStatement(lockItemsSQL);
            pstmt.setString(1, v_productCode);
            pstmt.executeQuery();
            pstmt.close();

            // Generate entry to orderdetails
            String getMaxOrderLineNumberSQL = "SELECT MAX(orderLineNumber) + 1 FROM orderdetails WHERE orderNumber = ?";
            pstmt = conn.prepareStatement(getMaxOrderLineNumberSQL);
            pstmt.setInt(1, newOrderNumber_var);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                orderLineNumber_var = rs.getInt(1);
            }
            rs.close();
            pstmt.close();

            if (orderLineNumber_var == 0) {
                orderLineNumber_var = 1;
            }

            String insertOrderDetailsSQL = "INSERT INTO orderdetails (orderNumber, productCode, quantityOrdered, priceEach, orderLineNumber) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertOrderDetailsSQL);
            pstmt.setInt(1, newOrderNumber_var);
            pstmt.setString(2, v_productCode);
            pstmt.setInt(3, v_quantityOrdered);
            pstmt.setDouble(4, v_priceEach);
            pstmt.setInt(5, orderLineNumber_var);
            pstmt.executeUpdate();
            pstmt.close();

            // Delay to capture possible conflicts
            String delaySQL = "SELECT SLEEP(3)";
            pstmt = conn.prepareStatement(delaySQL);
            pstmt.executeQuery();
            pstmt.close();


            if (nestedCall == 0) {
                conn.commit();
            }

            System.out.println("Order was added!");
            return 1;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    if (nestedCall == 0) {
                        conn.rollback();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
                //if (nestedCall == 0 && conn != null) { // Added check for conn != null to avoid NullPointerException
                  //  conn.rollback();
                //}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


public int updateOrderProduct() {
    Scanner scanner = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter Order Line Number: ");
    int v_orderLineNumber = scanner.nextInt();

    System.out.print("Enter New Quantity: ");
    int v_quantityOrdered = scanner.nextInt();

    System.out.print("Enter New Price: ");
    double v_priceEach = scanner.nextDouble();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter User Name: ");
    String v_endusername = scanner.nextLine();

    System.out.print("Enter Update Reason: ");
    String v_enduserreason = scanner.nextLine();
    scanner.close();

    Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int nestedCall = 0;
        String productCode_var = null;

    // Perform the update operation
    try {
        conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        // Check for nested calls
        String checkNestedCallSQL = "SELECT COUNT(1) FROM information_schema.innodb_trx WHERE trx_mysql_thread_id = CONNECTION_ID()";
        pstmt = conn.prepareStatement(checkNestedCallSQL);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            nestedCall = rs.getInt(1);
        }
        rs.close();
        pstmt.close();

        if (nestedCall == 0) {
            conn.setAutoCommit(false);
        }

        // Get productCode from table
        String getProductCodeSQL = "SELECT productCode FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ?";
        pstmt = conn.prepareStatement(getProductCodeSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            productCode_var = rs.getString("productCode");
        } else {
            System.out.println("No product code found for the given order number and line number.");
            return 0;
        }
        rs.close();
        pstmt.close();




        // Lock the row for update in orderdetails
        String selectOrderForUpdateSQL = "SELECT quantityOrdered, priceEach FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ? FOR UPDATE";
        pstmt = conn.prepareStatement(selectOrderForUpdateSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        pstmt.executeQuery();
        pstmt.close();

        // Lock the row for update in current_products
        String selectForUpdateSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
        pstmt = conn.prepareStatement(selectForUpdateSQL);
        pstmt.setString(1, productCode_var);
        pstmt.executeQuery();
        pstmt.close();

        // Update order details
        String updateOrderDetailsSQL = "UPDATE orderdetails SET quantityOrdered = ?, priceEach = ?, end_username = ?, end_userreason = ? WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ?";
        pstmt = conn.prepareStatement(updateOrderDetailsSQL);
        pstmt.setInt(1, v_quantityOrdered);
        pstmt.setDouble(2, v_priceEach);
        pstmt.setString(3, v_endusername);
        pstmt.setString(4, v_enduserreason);
        pstmt.setInt(5, v_orderNumber);
        pstmt.setString(6, productCode_var);
        pstmt.setInt(7, v_orderLineNumber);
        int rowsUpdated = pstmt.executeUpdate();
        pstmt.close();

        if (rowsUpdated == 0) {
            System.out.println("Order detail not found for update!");
            conn.rollback();
            return 0;
        }

        // Delay to capture possible conflicts
        String delaySQL = "SELECT SLEEP(3)";
        pstmt = conn.prepareStatement(delaySQL);
        pstmt.executeQuery();
        pstmt.close();

        if (nestedCall == 0) {
            conn.commit();
        }

        System.out.println("Order product updated!");
        return 1;

    } catch (SQLException e) {
        e.printStackTrace();
        // Handle rollback with the same connection instance used for the transaction
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}




    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;
        // Letting the user choose between the functions
        System.out.println("Enter Type: \n  [0] - ADD AN ORDER \n  [1] - UPDATE ORDERED PRODUCT \n  [2] - CREATE AN EMPLOYEE \n  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n  [5] - VIEW BASIC EMPLOYEE RECORD ");

        choice = sc.nextInt();
        sales s = new sales();
        if (choice == 0) s.addOrder();
        else if (choice == 1) s.updateOrderProduct();
        else System.out.println("Invalid choice!");

        System.out.println("Press enter key to continue....");
        sc.nextLine();
        sc.close();
    }
}
