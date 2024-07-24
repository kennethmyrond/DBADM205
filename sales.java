import java.sql.*;
import java.util.Scanner;

public class sales {

    // javac sales.java
    //java -cp .;./mysql-connector-j-9.0.0.jar sales

    // Database URL
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/DBSALES26_G205"; //jdbc:mysql://127.0.0.1:3306/db2.6test
    //jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/DBSALES26_G205
    // Database credentials
    public String username = "DBADM_205";
    // DBADM_205
    // root
    public String password = "DLSU1234!";
    // DLSU1234!
    // root1234

    public int addOrder() {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("Enter Order Number (0 if new):");
        int v_orderNumber = sc.nextInt();
        sc.nextLine(); // Consume newline

        String v_requiredDate = "";
    int v_customerNumber = 0;

    if (v_orderNumber == 0) {
        System.out.println("Enter Required Date (YYYY-MM-DD):");
        v_requiredDate = sc.nextLine() + " 00:00:00"; // Convert to datetime format

        System.out.println("Enter Customer Number:");
        v_customerNumber = sc.nextInt();
        sc.nextLine(); // Consume newline
    }

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

            // Get old quantity from items
            String lockItemsSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
            pstmt = conn.prepareStatement(lockItemsSQL);
            pstmt.setString(1, v_productCode);
            pstmt.executeQuery();
            pstmt.close();
            
            //lock orderdetails
            String lockOrderDetailsSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? FOR UPDATE";
            pstmt = conn.prepareStatement(lockOrderDetailsSQL);
            pstmt.setInt(1, newOrderNumber_var);
            pstmt.executeQuery();
            pstmt.close();

            // Generate new OrderNumber if needed
            if (v_orderNumber == 0) {
                String lockOrdersSQL = "SELECT * FROM orders FOR UPDATE";
                pstmt = conn.prepareStatement(lockOrdersSQL);
                pstmt.executeQuery();
                pstmt.close();

                // lock orders_audit
                String lockOrdersAuditSQL = "SELECT * FROM orders_audit FOR SHARE";
                pstmt = conn.prepareStatement(lockOrdersAuditSQL);
                pstmt.executeQuery();
                pstmt.close();

                String getMaxOrderNumberSQL = "SELECT MAX(orderNumber) + 1 FROM orders_audit";
                pstmt = conn.prepareStatement(getMaxOrderNumberSQL);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    newOrderNumber_var = rs.getInt(1);
                }
                rs.close();
                pstmt.close();

                //System.out.println("AYO" + newOrderNumber_var);

                if (newOrderNumber_var == 0) {
                    newOrderNumber_var = 9001;
                }

                //System.out.println("New Order Number: " + newOrderNumber_var);
                String insertOrderSQL = "INSERT INTO orders (orderNumber, orderDate, requiredDate, status, customerNumber) VALUES (?, NOW(), ?, 'In Process', ?)";
                pstmt = conn.prepareStatement(insertOrderSQL);
                pstmt.setInt(1, newOrderNumber_var);
                pstmt.setString(2, v_requiredDate);
                pstmt.setInt(3, v_customerNumber);
                pstmt.executeUpdate();
                pstmt.close();
            } else {
                // Check if order exists

                // lock orders
                String lockOrdersSQL = "SELECT * FROM orders WHERE orderNumber = ? LOCK IN SHARE MODE";
                pstmt = conn.prepareStatement(lockOrdersSQL);
                pstmt.setInt(1, v_orderNumber);
                pstmt.executeQuery();

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


            
            
            
        /*

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

        */

            //System.out.println("Order Line Number: " + orderLineNumber_var);
            //System.out.println("Order Number: " + newOrderNumber_var);
            //System.out.println("Product Code: " + v_productCode);
            //System.out.println("Quantity Ordered: " + v_quantityOrdered);
            //System.out.println("Price Each: " + v_priceEach);

            String insertOrderDetailsSQL = "INSERT INTO orderdetails (orderNumber, productCode, quantityOrdered, priceEach) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertOrderDetailsSQL);
            pstmt.setInt(1, newOrderNumber_var);
            pstmt.setString(2, v_productCode);
            pstmt.setInt(3, v_quantityOrdered);
            pstmt.setDouble(4, v_priceEach);
            //pstmt.setInt(5, orderLineNumber_var);
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
                if (sc != null) sc.close();
                //if (nestedCall == 0 && conn != null) { // Added check for conn != null to avoid NullPointerException
                  //  conn.rollback();
                //}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*

public int updateOrderProduct() {
    Scanner scanner = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter Order Line Number: ");
    int v_orderLineNumber = scanner.nextInt();

    System.out.print("Enter New Quantity (or -1 to leave unchanged): ");
        int v_quantityOrdered = scanner.nextInt();

        System.out.print("Enter New Price (or -1 to leave unchanged): ");
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

        // Prepare update order details statement
        StringBuilder updateOrderDetailsSQL = new StringBuilder("UPDATE orderdetails SET ");
        boolean commaNeeded = false;
        if (v_quantityOrdered != -1) {
            updateOrderDetailsSQL.append("quantityOrdered = ?, ");
            commaNeeded = true;
        }
        if (v_priceEach != -1.0) {
            updateOrderDetailsSQL.append("priceEach = ?, ");
            commaNeeded = true;
        }
        updateOrderDetailsSQL.append("end_username = ?, end_userreason = ? WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ?");

        pstmt = conn.prepareStatement(updateOrderDetailsSQL.toString());

        int paramIndex = 1;
        if (v_quantityOrdered != -1) {
            pstmt.setInt(paramIndex++, v_quantityOrdered);
        }
        if (v_priceEach != -1.0) {
            pstmt.setDouble(paramIndex++, v_priceEach);
        }
        pstmt.setString(paramIndex++, v_endusername);
        pstmt.setString(paramIndex++, v_enduserreason);
        pstmt.setInt(paramIndex++, v_orderNumber);
        pstmt.setString(paramIndex++, productCode_var);
        pstmt.setInt(paramIndex, v_orderLineNumber);
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

*/



public int updateOrderProduct() {
    Scanner scanner = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter Order Line Number: ");
    int v_orderLineNumber = scanner.nextInt();

    System.out.print("Enter 1 to delete the order, 0 to update the order: ");
    int action = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int nestedCall = 0;

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

        // Get current status of the order
        String getStatusSQL = "SELECT getCurrentStatus(?) AS status";
        pstmt = conn.prepareStatement(getStatusSQL);
        pstmt.setInt(1, v_orderNumber);
        rs = pstmt.executeQuery();

        String status_var = "";
        if (rs.next()) {
            status_var = rs.getString("status");
        }
        rs.close();
        pstmt.close();

        if (action == 1) {
            // Prevent the deletion of the order if not In Process
            if (!"In Process".equals(status_var)) {
                System.out.println("ERROR 91C1: Cannot delete order that is not In Process.");
                return 0;
            }

            // Lock the row for update in orderdetails
            String selectOrderProductForUpdateSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ? FOR UPDATE";
            pstmt = conn.prepareStatement(selectOrderProductForUpdateSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.setInt(2, v_orderLineNumber);
            pstmt.executeQuery();
            pstmt.close();

            // Return the quantity ordered into current_products
            String getProductCodeSQL = "SELECT productCode, quantityOrdered FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ?";
            pstmt = conn.prepareStatement(getProductCodeSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.setInt(2, v_orderLineNumber);
            rs = pstmt.executeQuery();

            String productCode_var = null;
            int quantityOrdered = 0;
            if (rs.next()) {
                productCode_var = rs.getString("productCode");
                quantityOrdered = rs.getInt("quantityOrdered");
            } else {
                System.out.println("No product code found for the given order number and line number.");
                return 0;
            }

            rs.close();

            // Lock the row for update in current_products
            String selectForUpdateSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
            pstmt = conn.prepareStatement(selectForUpdateSQL);
            pstmt.setString(1, productCode_var);
            pstmt.executeQuery();
            pstmt.close();

            // Update the quantity in current_products
            String updateCurrentProductsSQL = "UPDATE current_products SET quantityInStock = quantityInStock + ? WHERE productCode = ?";
            pstmt = conn.prepareStatement(updateCurrentProductsSQL);
            pstmt.setInt(1, quantityOrdered);
            pstmt.setString(2, productCode_var);
            pstmt.executeUpdate();
            pstmt.close();

            // Delete the order details
            String deleteOrderDetailsSQL = "DELETE FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ?";
            pstmt = conn.prepareStatement(deleteOrderDetailsSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.setInt(2, v_orderLineNumber);
            int rowsDeleted = pstmt.executeUpdate();
            pstmt.close();

            if (rowsDeleted == 0) {
                System.out.println("Order detail not found for deletion!");
                conn.rollback();
                return 0;
            }

            // Optionally, you may also want to delete the order from the orders table if there are no more order details for this order number
            String checkRemainingOrderDetailsSQL = "SELECT COUNT(*) FROM orderdetails WHERE orderNumber = ?";
            pstmt = conn.prepareStatement(checkRemainingOrderDetailsSQL);
            pstmt.setInt(1, v_orderNumber);
            rs = pstmt.executeQuery();
            int remainingOrderDetails = 0;
            if (rs.next()) {
                remainingOrderDetails = rs.getInt(1);
            }
            rs.close();
            pstmt.close();

            if (remainingOrderDetails == 0) {
                // Lock the row for update in orders
                String selectOrderForUpdateSQL = "SELECT * FROM orders WHERE orderNumber = ? FOR UPDATE";
                pstmt = conn.prepareStatement(selectOrderForUpdateSQL);
                pstmt.setInt(1, v_orderNumber);
                pstmt.executeQuery();
                pstmt.close();

                // Update the order's status to "Cancelled" instead of deleting it
                String updateOrderStatusSQL = "UPDATE orders SET status = 'Cancelled' WHERE orderNumber = ?";
                pstmt = conn.prepareStatement(updateOrderStatusSQL);
                pstmt.setInt(1, v_orderNumber);
                pstmt.executeUpdate();
                pstmt.close();
            }

            // Delay to capture possible conflicts
            String delaySQL = "SELECT SLEEP(3)";
            pstmt = conn.prepareStatement(delaySQL);
            pstmt.executeQuery();
            pstmt.close();

            if (nestedCall == 0) {
                conn.commit();
            }
            System.out.println("Order deleted successfully!");
            return 1;

        } else {
            System.out.print("Enter New Quantity (or -1 to leave unchanged): ");
            int v_quantityOrdered = scanner.nextInt();

            System.out.print("Enter New Price (or -1 to leave unchanged): ");
            double v_priceEach = scanner.nextDouble();
            scanner.nextLine();  // Consume newline

            System.out.print("Enter User Name: ");
            String v_endusername = scanner.nextLine();

            System.out.print("Enter Update Reason: ");
            String v_enduserreason = scanner.nextLine();

            String productCode_var = null;
            int v_referenceNumber = -1;

            // Lock orders
            String lockOrdersSQL = "SELECT * FROM orders WHERE orderNumber = ? FOR SHARE";
            pstmt = conn.prepareStatement(lockOrdersSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.executeQuery();
            pstmt.close();

            // Check status
            String checkStatusSQL = "SELECT getCurrentStatus(?) AS status";
            pstmt = conn.prepareStatement(checkStatusSQL);
            pstmt.setInt(1, v_orderNumber);
            rs = pstmt.executeQuery();
            String status = "";
            if (rs.next()) {
                status = rs.getString("status");
            }
            rs.close();
            pstmt.close();

            // If status is Shipped, ask for reference number
            if ("Shipped".equals(status)) {
                System.out.print("Enter Reference Number or -1 if unchanged: ");
                v_referenceNumber = scanner.nextInt();
            }

            // Lock for read
            String lockOrderDetailsSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ? FOR SHARE";
            pstmt = conn.prepareStatement(lockOrderDetailsSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.setInt(2, v_orderLineNumber);
            pstmt.executeQuery();
            pstmt.close();

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

            // Prepare update order details statement
            StringBuilder updateOrderDetailsSQL = new StringBuilder("UPDATE orderdetails SET ");
            boolean commaNeeded = false;
            if (v_quantityOrdered != -1) {
                updateOrderDetailsSQL.append("quantityOrdered = ?, ");
                commaNeeded = true;
            }
            if (v_priceEach != -1.0) {
                updateOrderDetailsSQL.append("priceEach = ?, ");
                commaNeeded = true;
            }
            updateOrderDetailsSQL.append("end_username = ?, end_userreason = ?");

            if (v_referenceNumber != -1) {
                updateOrderDetailsSQL.append(", referenceNo = ?");
            }

            updateOrderDetailsSQL.append(" WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ?");

            pstmt = conn.prepareStatement(updateOrderDetailsSQL.toString());

            int paramIndex = 1;
            if (v_quantityOrdered != -1) {
                pstmt.setInt(paramIndex++, v_quantityOrdered);
            }
            if (v_priceEach != -1.0) {
                pstmt.setDouble(paramIndex++, v_priceEach);
            }
            pstmt.setString(paramIndex++, v_endusername);
            pstmt.setString(paramIndex++, v_enduserreason);

            if (v_referenceNumber != -1) {
                pstmt.setInt(paramIndex++, v_referenceNumber);
            }

            pstmt.setInt(paramIndex++, v_orderNumber);
            pstmt.setString(paramIndex++, productCode_var);
            pstmt.setInt(paramIndex, v_orderLineNumber);
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
        }
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
    } finally {
        // Close resources
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
            scanner.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}





/*

public int updateOrderProduct() {
    Scanner scanner = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter Order Line Number: ");
    int v_orderLineNumber = scanner.nextInt();

    System.out.print("Enter 1 to delete the order, 0 to update the order: ");
    int action = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    if (action == 1) {
        scanner.close();
        return deleteOrder(v_orderNumber, v_orderLineNumber);
    } else {
        System.out.print("Enter New Quantity (or -1 to leave unchanged): ");
        int v_quantityOrdered = scanner.nextInt();

        System.out.print("Enter New Price (or -1 to leave unchanged): ");
        double v_priceEach = scanner.nextDouble();
        scanner.nextLine();  // Consume newline

        System.out.print("Enter User Name: ");
        String v_endusername = scanner.nextLine();

        System.out.print("Enter Update Reason: ");
        String v_enduserreason = scanner.nextLine();
        scanner.close();

        return updateOrderDetails(v_orderNumber, v_orderLineNumber, v_quantityOrdered, v_priceEach, v_endusername, v_enduserreason);
    }
}

private int deleteOrder(int v_orderNumber, int v_orderLineNumber) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int nestedCall = 0;

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

        // Get current status of the order
        String getStatusSQL = "SELECT getCurrentStatus(?) AS status";
        pstmt = conn.prepareStatement(getStatusSQL);
        pstmt.setInt(1, v_orderNumber);
        rs = pstmt.executeQuery();

        String status_var = "";
        if (rs.next()) {
            status_var = rs.getString("status");
        }
        rs.close();
        pstmt.close();

        // Prevent the deletion of the order if not In Process
        if (!"In Process".equals(status_var)) {
            System.out.println("ERROR 91C1: Cannot delete order that is not In Process.");
            return 0;
        }
        
        // Lock the row for update in orderdetails
        String selectOrderProductForUpdateSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ? FOR UPDATE";
        pstmt = conn.prepareStatement(selectOrderProductForUpdateSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        pstmt.executeQuery();
        pstmt.close();

        // return the quantity ordered into current_products
        String getProductCodeSQL = "SELECT productCode, quantityOrdered FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ?";
        pstmt = conn.prepareStatement(getProductCodeSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        rs = pstmt.executeQuery();

        String productCode_var = null;
        int quantityOrdered = 0;
        if (rs.next()) {
            productCode_var = rs.getString("productCode");
            quantityOrdered = rs.getInt("quantityOrdered");
        } else {
            System.out.println("No product code found for the given order number and line number.");
            return 0;
        }

        rs.close();

        // Lock the row for update in current_products
        String selectForUpdateSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
        pstmt = conn.prepareStatement(selectForUpdateSQL);
        pstmt.setString(1, productCode_var);
        pstmt.executeQuery();
        pstmt.close();

        // Update the quantity in current_products
        String updateCurrentProductsSQL = "UPDATE current_products SET quantityInStock = quantityInStock + ? WHERE productCode = ?";
        pstmt = conn.prepareStatement(updateCurrentProductsSQL);
        pstmt.setInt(1, quantityOrdered);
        pstmt.setString(2, productCode_var);
        pstmt.executeUpdate();
        pstmt.close();

        // Delete the order details
        String deleteOrderDetailsSQL = "DELETE FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ?";
        pstmt = conn.prepareStatement(deleteOrderDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        int rowsDeleted = pstmt.executeUpdate();
        pstmt.close();

        if (rowsDeleted == 0) {
            System.out.println("Order detail not found for deletion!");
            conn.rollback();
            return 0;
        }

        // Optionally, you may also want to delete the order from the orders table if there are no more order details for this order number
        String checkRemainingOrderDetailsSQL = "SELECT COUNT(*) FROM orderdetails WHERE orderNumber = ?";
        pstmt = conn.prepareStatement(checkRemainingOrderDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        rs = pstmt.executeQuery();
        int remainingOrderDetails = 0;
        if (rs.next()) {
            remainingOrderDetails = rs.getInt(1);
        }
        rs.close();
        pstmt.close();

        if (remainingOrderDetails == 0) {
            // Lock the row for update in orders
            String selectOrderForUpdateSQL = "SELECT * FROM orders WHERE orderNumber = ? FOR UPDATE";
            pstmt = conn.prepareStatement(selectOrderForUpdateSQL);
            pstmt.setInt(1, v_orderNumber);
            pstmt.executeQuery();
            pstmt.close();
            
            // Update the order's status to "Cancelled" instead of deleting it
    String updateOrderStatusSQL = "UPDATE orders SET status = 'Cancelled' WHERE orderNumber = ?";
    pstmt = conn.prepareStatement(updateOrderStatusSQL);
    pstmt.setInt(1, v_orderNumber);
    pstmt.executeUpdate();
    pstmt.close();
        }

        // Delay to capture possible conflicts
        String delaySQL = "SELECT SLEEP(3)";
        pstmt = conn.prepareStatement(delaySQL);
        pstmt.executeQuery();
        pstmt.close();


        if (nestedCall == 0) {
            conn.commit();
        }
        System.out.println("Order deleted successfully!");
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
    } finally {
        // Close resources
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

private int updateOrderDetails(int v_orderNumber, int v_orderLineNumber, int v_quantityOrdered, double v_priceEach, String v_endusername, String v_enduserreason) {
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

        
        // lock for read
        String lockOrderDetailsSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? AND orderLineNumber = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockOrderDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderLineNumber);
        pstmt.executeQuery();
        pstmt.close();

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

        // Prepare update order details statement
        StringBuilder updateOrderDetailsSQL = new StringBuilder("UPDATE orderdetails SET ");
        boolean commaNeeded = false;
        if (v_quantityOrdered != -1) {
            updateOrderDetailsSQL.append("quantityOrdered = ?, ");
            commaNeeded = true;
        }
        if (v_priceEach != -1.0) {
            updateOrderDetailsSQL.append("priceEach = ?, ");
            commaNeeded = true;
        }
        updateOrderDetailsSQL.append("end_username = ?, end_userreason = ? WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ?");

        pstmt = conn.prepareStatement(updateOrderDetailsSQL.toString());

        int paramIndex = 1;
        if (v_quantityOrdered != -1) {
            pstmt.setInt(paramIndex++, v_quantityOrdered);
        }
        if (v_priceEach != -1.0) {
            pstmt.setDouble(paramIndex++, v_priceEach);
        }
        pstmt.setString(paramIndex++, v_endusername);
        pstmt.setString(paramIndex++, v_enduserreason);
        pstmt.setInt(paramIndex++, v_orderNumber);
        pstmt.setString(paramIndex++, productCode_var);
        pstmt.setInt(paramIndex, v_orderLineNumber);
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
    } finally {
        // Close resources
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

*/

public int updateOrder() {
    Scanner scanner = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = scanner.nextInt();
    scanner.nextLine();  // Consume newline

    System.out.print("Enter New Required Date (YYYY-MM-DD) or -1 for unchanged: ");
    String v_requiredDateStr = scanner.nextLine();
    Timestamp v_requiredDate = null;
    if (!v_requiredDateStr.equals("-1")) {
        v_requiredDate = Timestamp.valueOf(v_requiredDateStr + " 00:00:00");
    }

    System.out.print("Enter New Status or -1 for unchanged: ");
    String v_status = scanner.nextLine();
    if (v_status.equals("-1")) {
        v_status = null;
    }

    System.out.print("Enter New Comments or -1 for unchanged: ");
    String v_comments = scanner.nextLine();
    if (v_comments.equals("-1")) {
        v_comments = null;
    }

    System.out.print("Enter Username: ");
    String endUsername = scanner.nextLine();

    System.out.print("Enter Update Reason: ");
    String endUserReason = scanner.nextLine();

    Timestamp v_shippedDate = null;
    if ("Shipped".equals(v_status)) {
        System.out.print("Enter New Shipped Date (YYYY-MM-DD HH:MM:SS) or -1 for unchanged: ");
        String v_shippedDateStr = scanner.nextLine();
        if (!v_shippedDateStr.equals("-1")) {
            v_shippedDate = Timestamp.valueOf(v_shippedDateStr);
        }
    }

    scanner.close();

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int nestedCall = 0;

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

        // If order is cancelled, update stocks
        if ("Cancelled".equals(v_status)) {
            String selectOrderDetailsSQL = "SELECT productCode, quantityOrdered, orderLineNumber FROM orderdetails WHERE orderNumber = ?";
            pstmt = conn.prepareStatement(selectOrderDetailsSQL);
            pstmt.setInt(1, v_orderNumber);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String updateProduct = rs.getString("productCode");
                int orderQuantity = rs.getInt("quantityOrdered");
                int orderLineNumber = rs.getInt("orderLineNumber");

                // Lock rows for update
                String lockOrderDetailsSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ? FOR UPDATE";
                pstmt = conn.prepareStatement(lockOrderDetailsSQL);
                pstmt.setInt(1, v_orderNumber);
                pstmt.setString(2, updateProduct);
                pstmt.setInt(3, orderLineNumber);
                pstmt.executeQuery();

                String lockCurrentProductsSQL = "SELECT * FROM current_products WHERE productCode = ? FOR UPDATE";
                pstmt = conn.prepareStatement(lockCurrentProductsSQL);
                pstmt.setString(1, updateProduct);
                pstmt.executeQuery();

                // Update order details
                String updateOrderDetailsSQL = "UPDATE orderdetails SET end_username = ?, end_userreason = 'Order Cancelled' WHERE orderNumber = ? AND productCode = ? AND orderLineNumber = ?";
                pstmt = conn.prepareStatement(updateOrderDetailsSQL);
                pstmt.setString(1, endUsername);
                pstmt.setInt(2, v_orderNumber);
                pstmt.setString(3, updateProduct);
                pstmt.setInt(4, orderLineNumber);
                pstmt.executeUpdate();
            }
            rs.close();
            pstmt.close();
        }

        // Get current status of the order
        String getStatusSQL = "SELECT getCurrentStatus(?) AS status, comments, orderDate FROM orders WHERE orderNumber = ?";
        pstmt = conn.prepareStatement(getStatusSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.setInt(2, v_orderNumber);
        rs = pstmt.executeQuery();

        String status_var = "";
        String existingComments = null;
        String orderDateString = null;
        Timestamp orderDate_var = null;
        if (rs.next()) {
            status_var = rs.getString("status");
            existingComments = rs.getString("comments");
            orderDateString = rs.getString("orderDate");
            orderDate_var = Timestamp.valueOf(orderDateString);
        }
        rs.close();
        pstmt.close();

        if(orderDate_var != null && v_requiredDate != null) {
       // Get current status of the order
       String checkDateSQL = "SELECT DATEDIFF(?, ?) AS difference;";
       pstmt = conn.prepareStatement(checkDateSQL);
         pstmt.setTimestamp(1, v_requiredDate);
            pstmt.setTimestamp(2, orderDate_var);
       rs = pstmt.executeQuery();

        int difference = 0;
        if (rs.next()) {
            difference = rs.getInt("difference");
        }
        rs.close();

        if (difference < 3) {
            System.out.println("Error 91C6: Required Date must be at least three days");
            return 0;
        }
        }

        // Check if status transition is valid
        if (v_status != null) {
            String checkStatusChangeSQL = "SELECT checkStatusChange(?, ?) AS statusChange";
            pstmt = conn.prepareStatement(checkStatusChangeSQL);
            pstmt.setString(1, status_var);
            pstmt.setString(2, v_status);
            rs = pstmt.executeQuery();

            boolean isValidTransition = false;
            if (rs.next()) {
                isValidTransition = rs.getBoolean("statusChange");
            }
            rs.close();
            pstmt.close();

            if (!isValidTransition) {
                throw new SQLException("ERROR 91C3: Invalid status transition.");
            }
        }

        // Prevent the updating of the order if cancelled
        if ("Cancelled".equals(status_var)) {
            throw new SQLException("ERROR 91C0: Cannot update cancelled order from the orders table.");
        }

        // Prevent updates if the order is completed
        if ("Completed".equals(status_var)) {
            throw new SQLException("ERROR 91C4: No updates allowed after order is completed.");
        }

        if ("Shipped".equals(v_status)) {
            // Check if the shipped date is valid by calling the MySQL function
            String checkShippedDateSQL = "SELECT checkShippedDate(?, ?, ?) AS isValid";
            pstmt = conn.prepareStatement(checkShippedDateSQL);
            pstmt.setString(1, v_status);
            pstmt.setTimestamp(2, v_shippedDate);
            pstmt.setTimestamp(3, v_requiredDate);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (!rs.getBoolean("isValid")) {
                    System.out.println("ERROR 91C5: Invalid input.");
                    return 0;
                }
            }
            rs.close();
            pstmt.close();
        }

        // Lock the orders table row for update
        String lockOrdersSQL = "SELECT * FROM orders WHERE orderNumber = ? FOR UPDATE";
        pstmt = conn.prepareStatement(lockOrdersSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.executeQuery();
        pstmt.close();

        // Update the orders table
        StringBuilder updateOrdersSQL = new StringBuilder("UPDATE orders SET ");
        boolean first = true;
        if (v_requiredDate != null) {
            updateOrdersSQL.append("requiredDate = ?");
            first = false;
        }
        if (v_status != null) {
            if (!first) updateOrdersSQL.append(", ");
            updateOrdersSQL.append("status = ?");
            first = false;
        }
        if (v_comments != null) {
            if (!first) updateOrdersSQL.append(", ");
            updateOrdersSQL.append("comments = ");
            if (existingComments != null) {
                updateOrdersSQL.append("CONCAT(comments, '\n', ?)");
            } else {
                updateOrdersSQL.append("?");
            }
            first = false;
        }
        if ("Shipped".equals(v_status) && v_shippedDate != null) {
            if (!first) updateOrdersSQL.append(", ");
            updateOrdersSQL.append("shippedDate = ?");
        }
        if (!first) updateOrdersSQL.append(", ");
        updateOrdersSQL.append("end_username = ?, end_userreason = ?");

        updateOrdersSQL.append(" WHERE orderNumber = ?");

        pstmt = conn.prepareStatement(updateOrdersSQL.toString());
        int paramIndex = 1;
        if (v_requiredDate != null) {
            pstmt.setTimestamp(paramIndex++, v_requiredDate);
        }
        if (v_status != null) {
            pstmt.setString(paramIndex++, v_status);
        }
        if (v_comments != null) {
            if (existingComments != null) {
                pstmt.setString(paramIndex++, v_comments);
            } else {
                pstmt.setString(paramIndex++, v_comments);
            }
        }
        if ("Shipped".equals(v_status) && v_shippedDate != null) {
            pstmt.setTimestamp(paramIndex++, v_shippedDate);
        }
        pstmt.setString(paramIndex++, endUsername);
        pstmt.setString(paramIndex++, endUserReason);
        pstmt.setInt(paramIndex++, v_orderNumber);
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

        System.out.println("Order updated successfully!");
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
    } finally {
        // Close resources
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


// function for viewing product and its price
public void viewProduct() {
    Scanner sc = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Product Code: ");
    String v_productCode = sc.nextLine();

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int nestedCall = 0;
    int productQuantity = 0;
    try {
        conn = DriverManager.getConnection(url, username, password);
        //conn.setAutoCommit(false);

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


        // lock products
        String lockProductsSQL = "SELECT * FROM products WHERE productCode = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockProductsSQL);
        pstmt.setString(1, v_productCode);
        pstmt.executeQuery();
        pstmt.close();

        // lock product_pricing
        String lockProductPricingSQL = "SELECT * FROM product_pricing WHERE productCode = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockProductPricingSQL);
        pstmt.setString(1, v_productCode);
        pstmt.executeQuery();
        pstmt.close();

        // lock product_wholesale
        String lockProductWholesaleSQL = "SELECT * FROM product_wholesale WHERE productCode = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockProductWholesaleSQL);
        pstmt.setString(1, v_productCode);
        pstmt.executeQuery();
        pstmt.close();

        // lock current_products
        String lockCurrentProductsSQL = "SELECT * FROM current_products WHERE productCode = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockCurrentProductsSQL);
        pstmt.setString(1, v_productCode);
        pstmt.executeQuery();
        pstmt.close();

        // get product Name from products table
        String productNameSQL = "SELECT productName FROM products WHERE productCode = ?";
        pstmt = conn.prepareStatement(productNameSQL);
        pstmt.setString(1, v_productCode);
        rs = pstmt.executeQuery();

        String productName = "";
        if (rs.next()) {
            productName = rs.getString(1);
        }
        rs.close();
        pstmt.close();

        // get product quantity from current_products
        String productQuantitySQL = "SELECT quantityInStock FROM current_products WHERE productCode = ?";
        pstmt = conn.prepareStatement(productQuantitySQL);
        pstmt.setString(1, v_productCode);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            productQuantity = rs.getInt(1);
        }
        rs.close();
        pstmt.close();

        // get min and max price from product_pricing

        String sql = "select getPrice(?, ?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, v_productCode);
        pstmt.setString(2, "MIN");
        rs = pstmt.executeQuery();

        double min_price = 0;
        if (rs.next()) {
            min_price = rs.getDouble(1);
        }
        rs.close();

        pstmt.setString(2, "MAX");
        rs = pstmt.executeQuery();

        double max_price = 0;
        if (rs.next()) {
            max_price = rs.getDouble(1);
        }
        rs.close();
        pstmt.close();

        // print the product details
        System.out.println("Product Code: " + v_productCode);
        System.out.println("Product Name: " + productName);
        System.out.println("Quantity in Stock: " + productQuantity);
        System.out.println("Minimum Price: " + min_price);
        System.out.println("Maximum Price: " + max_price);

        // Delay to capture possible conflicts
        String delaySQL = "SELECT SLEEP(3)";
        pstmt = conn.prepareStatement(delaySQL);
        pstmt.executeQuery();
        pstmt.close();

        if (nestedCall == 0) {
            conn.commit();
        }

    } catch (SQLException e) {
        e.printStackTrace();
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
            //sc.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


// function for viewing order and its details
public void viewOrder() {
    Scanner sc = new Scanner(System.in);

    // Get user inputs
    System.out.print("Enter Order Number: ");
    int v_orderNumber = sc.nextInt();
    sc.nextLine();  // Consume newline

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int nestedCall = 0;


    try {
        conn = DriverManager.getConnection(url, username, password);
        //conn.setAutoCommit(false);

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

        // lock orders
        String lockOrdersSQL = "SELECT * FROM orders WHERE orderNumber = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockOrdersSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.executeQuery();
        pstmt.close();


        // get order details
        String orderDetailsSQL = "SELECT orderNumber, orderDate, requiredDate, shippedDate, status, comments FROM orders WHERE orderNumber = ?";
        pstmt = conn.prepareStatement(orderDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        rs = pstmt.executeQuery();

        System.out.println("Order Number | Order Date | Required Date | Shipped Date | Status | Comments");
        while (rs.next()) {
            System.out.println(rs.getInt(1) + " | " + rs.getDate(2) + " | " + rs.getDate(3) + " | " + rs.getDate(4) + " | " + rs.getString(5) + " | " + rs.getString(6));
        }

        // lock orderdetails
        String lockOrderDetailsSQL = "SELECT * FROM orderdetails WHERE orderNumber = ? FOR SHARE";
        pstmt = conn.prepareStatement(lockOrderDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        pstmt.executeQuery();
        pstmt.close();

        // get order details
        String orderProductDetailsSQL = "SELECT productCode, quantityOrdered, priceEach, orderLineNumber, referenceno FROM orderdetails WHERE orderNumber = ?";
        pstmt = conn.prepareStatement(orderProductDetailsSQL);
        pstmt.setInt(1, v_orderNumber);
        rs = pstmt.executeQuery();

        System.out.println("Product Code | Quantity Ordered | Price Each | Order Line Number | Reference No");
        while (rs.next()) {
            System.out.println(rs.getString(1) + " | " + rs.getInt(2) + " | " + rs.getDouble(3) + " | " + rs.getInt(4) + " | " + rs.getString(5));
        }
        rs.close();
        pstmt.close();

        // Delay to capture possible conflicts
        String delaySQL = "SELECT SLEEP(3)";
        pstmt = conn.prepareStatement(delaySQL);
        pstmt.executeQuery();
        pstmt.close();

        if (nestedCall == 0) {
            conn.commit();
        }

    } catch (SQLException e) {
        e.printStackTrace();
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
            //sc.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;
        // Letting the user choose between the functions
        System.out.println("Enter Type: \n  [0] - ADD AN ORDER \n  [1] - UPDATE ORDERED PRODUCT \n  [2] - UPDATE ORDER \n  [3] - VIEW PRODUCT DETAILS AND PRICING \n  [4] - VIEW ORDER AND DETAILS \n [5] - EXIT");

        choice = sc.nextInt();
        sales s = new sales();
        if (choice == 0) s.addOrder();
        else if (choice == 1) s.updateOrderProduct();
        else if (choice == 2) s.updateOrder();
        else if (choice == 3) s.viewProduct();
        else if (choice == 4) s.viewOrder();
        else System.out.println("Invalid choice!");

        System.out.println("Press enter key to continue....");
        sc.nextLine();
        sc.close();
    }
}
