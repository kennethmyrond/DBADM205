import java.sql.*;
import java.util.*;
import java.time.LocalDate;

public class employees {
    // Database URL
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/DBSALES26_G205";
    // Database credentials
    public String username = "DBADM_205";
    public String password = "DLSU1234!";

    public int      employeeID;

    public String   lastName;
    public String   firstName;
    public String   extension;
    public String   email; 
    public String   jobTitle;
    public String   employee_type;
    public int      deptCode;
    public int      is_deactivated;
    public String   endUsername = null;
    public String   endUserReason = null;
    

    public employees(){}

    public int addEmployee()        {
    	Scanner sc = new Scanner(System.in);
    	
    	System.out.println("Enter Last Name:");
        lastName = sc.nextLine();

        System.out.println("Enter First Name:");
        firstName = sc.nextLine();

        System.out.println("Enter Extension:");
        extension = sc.nextLine();

        System.out.println("Enter Email:");
        email = sc.nextLine();

        System.out.println("Enter Job Title:");
        jobTitle = sc.nextLine();

        System.out.println("Enter Employee Type (Sales Representatives, Inventory Manager, Sales Manager):");
        employee_type = sc.nextLine();

        System.out.println("Enter Department Code (Enter 0 if not applicable):");
        deptCode = sc.nextInt();
        sc.nextLine(); // Consume newline
        
        try {
            Connection conn;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "{CALL add_employee(?, ?, ?, ?, ?, ?, ?, ?, ?)}"; 
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, extension);
            pstmt.setString(4, email);
            pstmt.setString(5, jobTitle);
            pstmt.setString(6, employee_type);
            pstmt.setInt(7, deptCode);
            pstmt.setString(8, endUsername);
            pstmt.setString(9, endUserReason);
            
            System.out.println("Press Enter to Start Adding Employee");
            sc.nextLine();

            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            conn.close();
            System.out.println("Employee was added!");
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } 

    }

    public int reclassifyEmployee(int tempChoice) {
        Scanner sc = new Scanner(System.in);

        String lockSql = "SELECT * FROM employees WHERE employeeNumber = ? FOR UPDATE";
        String sql1 = "CALL employeeTypeToInventoryManagers (?, ?)";
        String sql2 = "CALL employeeTypeToSalesManager (?, ?)";
        String sql3 = "CALL employeeTypeToSalesRepresentative (?)";

        int employeeID;
        int deptCode = 0;

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setAutoCommit(false);

            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            try (PreparedStatement pstmt = conn.prepareStatement(lockSql)) {
                pstmt.setInt(1, employeeID);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Employee does not exist.");
                        conn.rollback();
                        return 0;
                    }


                    String sql;
                    switch (tempChoice) {
                        case 1:
                            System.out.println("Enter Department Code:");
                            deptCode = Integer.parseInt(sc.nextLine());
                            sql = sql1;
                            break;
                        case 2:
                            System.out.println("Enter Department Code:");
                            deptCode = Integer.parseInt(sc.nextLine());
                            sql = sql2;
                            break;
                        case 3:
                            sql = sql3;
                            break;
                        default:
                            System.out.println("Invalid choice.");
                            conn.rollback();
                            return 0;
                    }


                    try (PreparedStatement updatePstmt = conn.prepareStatement(sql)) {
                        if (tempChoice == 1 || tempChoice == 2) {
                            updatePstmt.setInt(1, employeeID);
                            updatePstmt.setInt(2, deptCode);
                        } else {
                            updatePstmt.setInt(1, employeeID);
                        }
                        updatePstmt.execute();
                    }

                    conn.commit();
                    System.out.println("Employee reclassified successfully.");
                    return 1;
                } catch (SQLException e) {
                    System.out.println("SQL Error: " + e.getMessage());
                    conn.rollback();
                    return 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection or SQL Error: " + e.getMessage());
            return 0;
        } finally {
            sc.close();
        }
    }



    public int resignEmployee()     {
        return 0;
    }

    public int createSalesRepAssign() {
        Scanner sc = new Scanner(System.in);
        int employeeID;
        int officeCode;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate;
        String reason;
        int quota = 10000;
        int salesManagerNumber;
        String end_username = "DBADMIN205@S17";
        String end_userreason = "Test";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        System.out.println("Employee Records of Sales Representatives");

        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            // Fetch and display employees
            String sql = "SELECT employeeNumber FROM employees WHERE employee_type = 'S' AND jobTitle = 'Sales Rep'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            employeeTableHeader();
            while (rs.next()) {
                int empNumber = rs.getInt("employeeNumber");

                String detailsSql = "SELECT * FROM employees WHERE employeeNumber = ?";
                PreparedStatement detailsPstmt = conn.prepareStatement(detailsSql);
                detailsPstmt.setInt(1, empNumber);
                ResultSet detailsRs = detailsPstmt.executeQuery();

                if (detailsRs.next()) {
                    employeeTableRow(detailsRs);
                }

                detailsRs.close();
                detailsPstmt.close();
            }

            rs.close();
            pstmt.close();


            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            System.out.println("Enter Office Code:");
            officeCode = Integer.parseInt(sc.nextLine());

            System.out.println("Enter End Date Assignment [YYYY-MM-DD]:");
            endDate = LocalDate.parse(sc.nextLine());

            System.out.println("Enter reason:");
            reason = sc.nextLine();


            System.out.println("Enter Sales Manager Number:");
            salesManagerNumber = Integer.parseInt(sc.nextLine());

            // Check if the employee exists with a lock
            String checkSql = "SELECT COUNT(*) FROM employees WHERE employeeNumber = ? FOR UPDATE";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, employeeID);
            rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                // Insert into salesRepAssignments
                String insertAssignmentSql = "INSERT INTO salesRepAssignments (employeeNumber, officeCode, startDate, endDate, reason, quota, salesManagerNumber, end_username, end_userreason) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt1 = conn.prepareStatement(insertAssignmentSql);
                pstmt1.setInt(1, employeeID);
                pstmt1.setInt(2, officeCode);
                pstmt1.setDate(3, java.sql.Date.valueOf(startDate));
                pstmt1.setDate(4, java.sql.Date.valueOf(endDate));
                pstmt1.setString(5, reason);
                pstmt1.setInt(6, quota);
                pstmt1.setInt(7, salesManagerNumber);
                pstmt1.setString(8, end_username);
                pstmt1.setString(9, end_userreason);

                pstmt1.executeUpdate();

                // Insert into salesRepresentatives
                String insertRepSql = "INSERT INTO salesRepresentatives (employeeNumber, end_username, end_userreason) "
                        + "VALUES (?, ?, ?)";
                PreparedStatement pstmt2 = conn.prepareStatement(insertRepSql);
                pstmt2.setInt(1, employeeID);
                pstmt2.setString(2, end_username);
                pstmt2.setString(3, end_userreason);

                pstmt2.executeUpdate();

                pstmt1.close();
                pstmt2.close();
                conn.commit();

                System.out.println("SalesRepAssignment Inserted Successfully");
                return 1;
            } else {
                System.out.println("Employee does not exist.");
                conn.rollback();
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback Error: " + rollbackEx.getMessage());
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
            sc.close();
        }
    }

    public int viewSalesRepDetails()     {
        float   incr;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Sales Representative ID:");
        employeeID = sc.nextInt();

        try {
            Connection conn; 
            conn = DriverManager.getConnection("jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205?useTimezone=true&serverTimezone=UTC&user=DBADM_205&password=DLSU1234!");
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);
            
            // PreparedStatement pstmt = conn.prepareStatement("SELECT productName, productLine, quantityInStock, buyPrice, MSRP FROM products WHERE productCode=? LOCK IN SHARE MODE");
            // pstmt.setString(1, productCode);
            
            // System.out.println("Press enter key to start retrieving the data");
            // sc.nextLine();
            
            // ResultSet rs = pstmt.executeQuery();   
            
            // while (rs.next()) {
            //     productName     = rs.getString("productName");
            //     productLine     = rs.getString("productLine");
            //     quantityInStock = rs.getInt("quantityInStock");
            //     buyPrice        = rs.getFloat("buyPrice");
            //     MSRP            = rs.getFloat("MSRP");
            // }
            
            // rs.close();
            
            // System.out.println("Product Name: " + productName);
            // System.out.println("Product Line: " + productLine);
            // System.out.println("Quantity:     " + quantityInStock);
            // System.out.println("Buy Price:    " + buyPrice);
            // System.out.println("MSRP:         " + MSRP);
            
            // System.out.println("Press enter key to end transaction");
            // sc.nextLine();

            // pstmt.close();
            // conn.commit();
            // conn.close();
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int viewEmployee()     {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter [0] To View All Employees \nEnter Employee ID:");
        employeeID = Integer.parseInt(sc.nextLine());

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql;
            PreparedStatement pstmt;

            if (employeeID == 0) {
                sql = "SELECT * FROM employees LOCK IN SHARE MODE";
                pstmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM employees WHERE employeeNumber = ? LOCK IN SHARE MODE";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, employeeID);
            }

            System.out.println("Press Enter to Start Viewing the Employee(s)");
            sc.nextLine();

            ResultSet rs = pstmt.executeQuery();

            if (employeeID == 0) {
                employeeTableHeader();
                while (rs.next()) {
                    employeeTableRow(rs);
                }
            } else {
                if (rs.next()) {
                    employeeTableHeader();
                    employeeTableRow(rs);
                } else {
                    System.out.println("Employee does not exist.");
                }
            }

            rs.close();
            pstmt.close();
            conn.commit();
            conn.close();

            return 1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }


    private void employeeTableHeader() {
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("| Emp ID    | Last Name     | First Name       | Extension |     Email                           | Job Title             | Emp Type |isDeact|");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");
    }

    private void employeeTableRow(ResultSet rs) throws Exception {
        employeeID = rs.getInt("employeeNumber");
        lastName = rs.getString("lastName");
        firstName = rs.getString("firstName");
        extension = rs.getString("extension");
        email = rs.getString("email");
        jobTitle = rs.getString("jobTitle");
        employee_type = rs.getString("employee_type");
        is_deactivated = rs.getInt("is_deactivated");

        System.out.format("| %-9d | %-13s | %-16s | %-9s | %-35s | %-21s | %-8s | %-5d |\n",
                employeeID, lastName, firstName, extension, email, jobTitle, employee_type, is_deactivated);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        Scanner sc     = new Scanner (System.in);
        int     choice;

        System.out.println("Enter your employee number:");
        int employeeRoleNo = sc.nextInt();

        boolean isSalesManager = false;

        String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205?useTimezone=true&serverTimezone=UTC";
        String user = "DBADM_205";
        String password = "DLSU1234!";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection Successful");

            String sql = "SELECT employeeNumber FROM employees WHERE jobTitle LIKE 'Sales Manager%'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (rs.getInt("employeeNumber") == employeeRoleNo) {
                    System.out.println("Logged in as Sales Manager");
                    isSalesManager = true;
                    break;
                }
            }

            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }

        // Letting the user choose between the functions
        // kenneth 0, 2, 4 
        // laiven 1, 3, 5
        System.out.println("Enter Type: \n  [0] - CREATE AN EMPLOYEE \n  [1] - RECLASSIFY EMPLOYEE \n  [2] - CREATE AN EMPLOYEE \n  [3] - CREATE NEW SALES REP ASSIGNMENT OR SALES REP ONLY \n  [4] - VIEW ALL DETAILS OF SALES REP (PREVIOUS ASSIGN) \n  [5] - VIEW BASIC EMPLOYEE RECORD ");
        choice = sc.nextInt();

        employees e = new employees();
        switch (choice) {
            case 0:
                e.addEmployee();
                break;
            case 1:
                System.out.println("\nEnter Type: \n  [1] - Reclassify to Inventory Manager \n  [2] - Reclassify to Sales Manager \n  [3] - Reclassify to Sales Representative \n");
                int tempChoice = sc.nextInt();
                try {
                    e.reclassifyEmployee(tempChoice);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
                break;
            case 2:
                e.resignEmployee();
                break;
            case 3:
                if (isSalesManager) {
                    e.createSalesRepAssign();
                } else {
                    System.out.println("Access denied. Only Sales Managers can create Sales Rep assignments.");
                }
                break;
            case 4:
                e.viewSalesRepDetails();
                break;
            case 5:
                e.viewEmployee();
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
        
        System.out.println("Press enter key to continue....");
        sc.nextLine();
        sc.close();
    }
}
