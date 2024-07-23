import java.sql.*;
import java.util.*;
//import java.time.LocalDate;

public class employees {
    // Database URL
    
    // public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205";
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/DBSALES26_G205";
    public String username = "DBADM_205";
    public String password = "DLSU1234!";

    /* 
    public String url = "jdbc:mysql://127.0.0.1:3306/dbsales26_g205";
    public String username = "root";
    public String password = "DLSU1234";*/


    public int      employeeID;
    
    public String   lastName;
    public String   firstName;
    public String   extension;
    public String   email; 
    public String   jobTitle;
    public String   employee_type;
    public int      deptCode;
    public int      is_deactivated;
    
    public String officeCode;
    public String startDate;
    public String endDate;
    public String reason;
    public double quota;
    public int salesManagerNumber;
    
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

    public int reclassifyEmployee() {
        Scanner sc = new Scanner(System.in);
        int employeeID;
        int deptCode = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        PreparedStatement sleeppstmt = null;

        try {
            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

            String lockSql = "SELECT * FROM employees WHERE employeeNumber = ?";
            pstmt = conn.prepareStatement(lockSql);
            pstmt.setInt(1, employeeID);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Employee does not exist.");
                conn.rollback();
                return 0;
            }

            System.out.println("Enter Type: \n  [1] - Reclassify to Inventory Manager \n  [2] - Reclassify to Sales Manager \n  [3] - Reclassify to Sales Representative \n");
            int tempChoice = Integer.parseInt(sc.nextLine());

            String sql = null;

            switch (tempChoice) {
                case 1:
                    System.out.println("Enter Department Code:");
                    deptCode = Integer.parseInt(sc.nextLine());

                    sql = "CALL employeeTypeToInventoryManagers (?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, employeeID);
                    pstmt.setInt(2, deptCode);
                    break;
                case 2:
                    System.out.println("Enter Department Code:");
                    deptCode = Integer.parseInt(sc.nextLine());

                    sql = "CALL employeeTypeToSalesManager (?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, employeeID);
                    pstmt.setInt(2, deptCode);
                    break;
                case 3:
                    sql = "CALL employeeTypeToSalesRepresentative (?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, employeeID);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    conn.rollback();
                    return 0;
            }

            System.out.println("Press Enter to Start Reclassifying Employee");
            sc.nextLine();

            sleeppstmt = conn.prepareStatement("SELECT SLEEP (3)");
            sleeppstmt.executeQuery();

            System.out.println(pstmt);

            pstmt.execute();
            pstmt.close();
            conn.close();
            sleeppstmt.close();
            System.out.println("Employee reclassified successfully.");

        }  catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
        return 0;
    }

    public int resignEmployee()     {
    	Scanner sc = new Scanner(System.in);
    	
        System.out.println("Enter Employee Number To Resign:");
        employeeID = sc.nextInt();
        sc.nextLine(); // Consume newline
        
        try {
            Connection conn;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "{CALL deactivateEmployee(?, ?, ?)}"; 
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, employeeID);
            pstmt.setString(2, endUsername);
            pstmt.setString(3, endUserReason);
            
            System.out.println("Press Enter to Resign Employee");
            sc.nextLine();

            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            conn.close();
            System.out.println("Employee has Resigned!");
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } 
    }

    public int createSalesRepAssign() {
        Scanner sc = new Scanner(System.in);
        int employeeID;
        int officeCode;
        String startDate;
        String endDate;
        String reason;
        int quota = 10000;
        String end_username = "DBADMIN205@S17";
        String end_userreason = "Test";
        int salesManagerNumber;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        System.out.println("Employee Records of Sales Representatives");

        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);

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

            System.out.println("Enter Start Date Assignment [YYYY-MM-DD]:");
            startDate = sc.nextLine();

            System.out.println("Enter End Date Assignment [YYYY-MM-DD]:");
            endDate = sc.nextLine();

            System.out.println("Enter reason:");
            reason = sc.nextLine();

            System.out.println("Enter Sales Manager ID:");
            salesManagerNumber = Integer.parseInt(sc.nextLine());


            String checkSql = "SELECT COUNT(*) FROM employees WHERE employeeNumber = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, employeeID);
            rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                String insertAssignmentSql = "INSERT INTO salesRepAssignments (employeeNumber, officeCode, startDate, endDate, reason, quota, salesManagerNumber, end_username, end_userreason) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt1 = conn.prepareStatement(insertAssignmentSql);
                pstmt1.setInt(1, employeeID);
                pstmt1.setInt(2, officeCode);
                pstmt1.setString(3, startDate);
                pstmt1.setString(4, startDate);
                pstmt1.setString(5, reason);
                pstmt1.setInt(6, quota);
                pstmt1.setInt(7, salesManagerNumber);
                pstmt1.setString(8, end_username);
                pstmt1.setString(9, end_userreason);

                pstmt1.executeUpdate();

                String insertRepSql = "INSERT INTO salesRepresentatives (employeeNumber, end_username, end_userreason) " + "VALUES (?, ?, ?)";
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

    public int viewSalesRepDetails() {
        Scanner sc = new Scanner(System.in);
        String sql = null;
        int range = 0;
        String baseSql = null;

        // \n [1] - All Current Sales Representatives \n [2] - All Previous Sales Representatives
        System.out.println("Enter Type of Record to View: \n [0] - All Sales Representatives record \n [1] - Record of Specific Sales Rep");
        int recordType = Integer.parseInt(sc.nextLine());

        if (recordType == 0) {
            System.out.println("Enter Range: \n [0] - Current Sales Representatives \n [1] - Previous Sales Representatives \n [2] - All Records");
            range = Integer.parseInt(sc.nextLine());
            baseSql = "SELECT sra.employeeNumber, \r\n" +
                      "    sra.officeCode, \r\n" +
                      "    em.firstName, em.lastName, \r\n" +
                      "    sra.startDate, \r\n" +
                      "    sra.endDate, \r\n" +
                      "    sra.reason, \r\n" +
                      "    sra.quota, \r\n" +
                      "    sra.salesManagerNumber,\r\n" +
                      "    e.firstName AS salesManagerFirstName,\r\n" +
                      "    e.lastName AS salesManagerLastName\r\n" +
                      "FROM salesRepAssignments sra\r\n" +
                      "JOIN employees e ON e.employeeNumber = sra.salesManagerNumber\r\n" +
                      "JOIN employees em ON em.employeeNumber = sra.employeeNumber\r\n";
        } else if (recordType == 1) {
            System.out.println("Enter Employee ID:");
            int employeeID = Integer.parseInt(sc.nextLine());

            System.out.println("Enter Range: \n [0] - Current \n [1] - Previous \n [2] - All Assignments");
            range = Integer.parseInt(sc.nextLine());
            baseSql = "SELECT \r\n" +
                      "    sra.officeCode, \r\n" +
                      "    sra.startDate, \r\n" +
                      "    sra.endDate, \r\n" +
                      "    sra.reason, \r\n" +
                      "    sra.quota, \r\n" +
                      "    sra.salesManagerNumber,\r\n" +
                      "    e.firstName AS salesManagerFirstName,\r\n" +
                      "    e.lastName AS salesManagerLastName\r\n" +
                      "FROM salesRepAssignments sra\r\n" +
                      "JOIN employees e ON e.employeeNumber = sra.salesManagerNumber\r\n" +
                      "WHERE sra.employeeNumber = ?\r\n";
        }

        String condition = "";
        switch (range) {
            case 0:
                condition = "WHERE (sra.endDate IS NULL OR sra.endDate >= CURDATE()) \r\n";
                break;
            case 1:
                condition = "WHERE sra.endDate < CURDATE() \r\n";
                break;
            case 2:
                // No additional condition needed for case 2
                break;
        }
        sql = baseSql + condition + "LOCK IN SHARE MODE;";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            if (recordType == 1) pstmt.setInt(1, employeeID);

            System.out.println("Press Enter to Start Viewing Sales Rep Assignment");
            sc.nextLine();

            rs = pstmt.executeQuery();

            // Print table headers
            System.out.printf("%-15s %-15s %-20s %-20s %-15s %-15s %-15s %-20s %-20s%n",
                              "Employee Number", "Office Code", "First Name", "Last Name", "Start Date", "End Date", "Reason", "Quota", "Sales Manager");
            System.out.println("---------------------------------------------------------------------------------------------------------------------");

            boolean assignmentsFound = false;

            while (rs.next()) {
                assignmentsFound = true;
                int employeeNumber = 0;
                String salesRepName = null;
                if (recordType == 0) {
                    employeeNumber = rs.getInt("employeeNumber");
                    salesRepName = rs.getString("firstName") + ' ' + rs.getString("lastName");
                }
                String officeCode = rs.getString("officeCode");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");
                String reason = rs.getString("reason");
                double quota = rs.getDouble("quota");
                int salesManagerNumber = rs.getInt("salesManagerNumber");
                String salesManagerName = rs.getString("salesManagerFirstName") + ' ' + rs.getString("salesManagerLastName");

                if (recordType == 0) {
                    System.out.printf("%-15d %-15s %-20s %-20s %-15s %-15s %-15s %-20.2f %-20s%n",
                                      employeeNumber, officeCode, rs.getString("firstName"), rs.getString("lastName"), startDate, endDate, reason, quota, salesManagerName);
                } else {
                    System.out.printf("%-15s %-15s %-15s %-15s %-20.2f %-20s%n",
                                      officeCode, startDate, endDate, reason, quota, salesManagerName);
                }
            }

            if (!assignmentsFound) {
                System.out.println("No records found");
            }

            rs.close();
            pstmt.close();
            conn.commit();
            conn.close();

            return 1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
<<<<<<< HEAD
    
    public int viewEmployee() {
=======

    public int viewEmployee()     {
>>>>>>> a76fb518d1869101c1e04f8bd43fdec266beaf12
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter [0] To View All Employees \nEnter Employee ID:");
        int employeeID = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql;

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

            rs = pstmt.executeQuery();

<<<<<<< HEAD
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
=======
            conn.commit();
            conn.close();

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

            return 1;

>>>>>>> a76fb518d1869101c1e04f8bd43fdec266beaf12
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
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

    public int modifySalesRepDetails() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Employee ID:");
        int employeeID = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement fetchPstmt = null;
        PreparedStatement updatePstmt = null;
        PreparedStatement jobTitlesPstmt = null;
        ResultSet rs = null;
        ResultSet jobTitlesRs = null;


        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String fetchSql = "SELECT * FROM salesRepAssignments WHERE employeeNumber = ? FOR UPDATE";
            fetchPstmt = conn.prepareStatement(fetchSql);
            fetchPstmt.setInt(1, employeeID);

            System.out.println("Press enter key to start retrieving the data");
            sc.nextLine();

            rs = fetchPstmt.executeQuery();
            if (rs.next()) {
                int officeCode = rs.getInt("officeCode");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");
                String reason = rs.getString("reason");
                int quota = rs.getInt("quota");

                System.out.println("Current Details:");
                System.out.println("Office Code: " + officeCode);
                System.out.println("Start Date: " + startDate);
                System.out.println("End Date: " + endDate);
                System.out.println("Reason: " + reason);
                System.out.println("Quota: " + quota);
            } else {
                System.out.println("No employee found with the given ID.");
                return 1;
            }
            rs.close();

            fetchPstmt = conn.prepareStatement("SELECT SLEEP (3)");
            fetchPstmt.executeQuery();

            fetchPstmt.close();

            int empDetail;
            // do {
            // Displaying the menu options to the user
            System.out.println("\nSelect Sale Representative Assignmment Detail To Edit: \n" +
                    "  [1] - End Date \n" +
                    "  [2] - Reason \n" +
                    "  [3] - Quota \n" +
                    "  [9] - EXIT");

            int salesRepDetail = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            String updateSql = null;
            switch (salesRepDetail) {
                case 1:
                    System.out.println("Enter New End Date:");
                    String newEndDate = sc.nextLine();

                    updateSql = "UPDATE salesRepAssignments SET endDate = ? WHERE employeeNumber = ?";
                    updatePstmt = conn.prepareStatement(updateSql);
                    updatePstmt.setString(1, newEndDate);
                    updatePstmt.setInt(2, employeeID);
                    break;
                case 2:
                    System.out.println("Enter New Reason:");
                    String newReason = sc.nextLine();

                    updateSql = "UPDATE salesRepAssignments SET reason = ? WHERE employeeNumber = ?";
                    updatePstmt = conn.prepareStatement(updateSql);
                    updatePstmt.setString(1, newReason);
                    updatePstmt.setInt(2, employeeID);
                    break;
                case 3:
                    System.out.println("Enter Quota");
                    String newQuota = sc.nextLine();

                    updateSql = "UPDATE salesRepAssignments SET reason = ? WHERE employeeNumber = ?";
                    updatePstmt = conn.prepareStatement(updateSql);
                    updatePstmt.setString(1, newQuota);
                    updatePstmt.setInt(2, employeeID);
                    break;
                case 9:
                    System.out.println("Exiting...");
                    return 1;
                default:
                    System.out.println("Invalid choice, please try again.");
                    return 1;
            }

            int rowsUpdated = updatePstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Sales Representative Assignment details updated successfully.");
            } else {
                System.out.println("No employee found with the given ID.");
            }
            updatePstmt.close();
            // } while (empDetail != 9);

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error updating employee details: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (fetchPstmt != null) fetchPstmt.close();
                if (updatePstmt != null) updatePstmt.close();
                if (jobTitlesRs != null) jobTitlesRs.close();
                if (jobTitlesPstmt != null) jobTitlesPstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return 0;
    }

    public int modifyEmployee() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Employee ID:");
        int employeeID = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement fetchPstmt = null;
        PreparedStatement updatePstmt = null;
        PreparedStatement jobTitlesPstmt = null;
        ResultSet rs = null;
        ResultSet jobTitlesRs = null;


        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String fetchSql = "SELECT extension, email, jobTitle FROM employees WHERE employeeNumber = ? FOR UPDATE";
            fetchPstmt = conn.prepareStatement(fetchSql);
            fetchPstmt.setInt(1, employeeID);

            System.out.println("Press enter key to start retrieving the data");
            sc.nextLine();

            rs = fetchPstmt.executeQuery();
            if (rs.next()) {
                String currentExtension = rs.getString("extension");
                String currentEmail = rs.getString("email");
                String currentJobTitle = rs.getString("jobTitle");

                System.out.println("Current Details:");
                System.out.println("Extension: " + currentExtension);
                System.out.println("Email: " + currentEmail);
                System.out.println("Job Title: " + currentJobTitle);
            } else {
                System.out.println("No employee found with the given ID.");
                return 1;
            }
            rs.close();

            fetchPstmt = conn.prepareStatement("SELECT SLEEP (3)");
            fetchPstmt.executeQuery();

            fetchPstmt.close();

            int empDetail;
            // do {
                // Displaying the menu options to the user
                System.out.println("\nSelect Employee Detail To Edit: \n" +
                        "  [1] - Extension \n" +
                        "  [2] - Email \n" +
                        "  [3] - Job Title \n" +
                        "  [9] - EXIT");

                empDetail = sc.nextInt();
                sc.nextLine(); // Consume the newline character

                String updateSql = null;
                switch (empDetail) {
                    case 1:
                        System.out.println("Enter New Extension:");
                        String newExtension = sc.nextLine();

                        updateSql = "UPDATE employees SET extension = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newExtension);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 2:
                        System.out.println("Enter New Email:");
                        String newEmail = sc.nextLine();

                        updateSql = "UPDATE employees SET email = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newEmail);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 3:
                        System.out.println("Available Job Titles:");
                        String jobTitlesSql = "SELECT jobTitle FROM employees_jobTitles LOCK IN SHARE MODE;";
                        jobTitlesPstmt = conn.prepareStatement(jobTitlesSql);
                        jobTitlesRs = jobTitlesPstmt.executeQuery();
                        
                        while (jobTitlesRs.next()) {
                            System.out.println("- " + jobTitlesRs.getString("jobTitle"));
                        }
                        
                        jobTitlesRs.close();
                        jobTitlesPstmt.close();

                        System.out.println("\nEnter New Job Title:");
                        String newJobTitle = sc.nextLine();

                        updateSql = "UPDATE employees SET jobTitle  = ? WHERE employeeNumber = ?";
                        updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setString(1, newJobTitle);
                        updatePstmt.setInt(2, employeeID);
                        break;
                    case 9:
                        System.out.println("Exiting...");
                        return 1;
                    default:
                        System.out.println("Invalid choice, please try again.");
                        return 1;
                }

                int rowsUpdated = updatePstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Employee details updated successfully.");
                } else {
                    System.out.println("No employee found with the given ID.");
                }
                updatePstmt.close();
            // } while (empDetail != 9);

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error updating employee details: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (fetchPstmt != null) fetchPstmt.close();
                if (updatePstmt != null) updatePstmt.close();
                if (jobTitlesRs != null) jobTitlesRs.close();
                if (jobTitlesPstmt != null) jobTitlesPstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return 0;
    }

 
    public void addDepartment() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Department Name:");
        String deptName = sc.nextLine();

        System.out.println("Enter Department Manager Number:");
        int deptManagerNumber = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            String sql = "SELECT * FROM dbsales26_g205.departments LOCK IN SHARE MODE; ";
            conn.prepareStatement(sql);

            pstmt = conn.prepareStatement("SELECT SLEEP (3)");
            pstmt.executeQuery();

            sql = "INSERT INTO departments (deptName, deptManagerNumber) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, deptName);
            pstmt.setInt(2, deptManagerNumber);


            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Department added successfully.");
            } else {
                System.out.println("Failed to add the department.");
            }

        } catch (SQLException e) {
            System.out.println("Error adding department: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.commit();
                    conn.close();
                    }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void viewDepartments() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            // Lock the departments table for reading
            // String lockSql = "LOCK TABLES departments READ";
            // pstmt = conn.prepareStatement(lockSql);
            // pstmt.execute();

            String sql = "SELECT deptCode, deptName, deptManagerNumber FROM departments LOCK IN SHARE MODE; ";
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            pstmt = conn.prepareStatement("SELECT SLEEP (3)");
            pstmt.executeQuery();


            // Print table headers
            System.out.printf("%-10s %-30s %-20s %n", "Dept Code", "Dept Name", "Manager Number");
            System.out.println("----------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                int deptCode = rs.getInt("deptCode");
                String deptName = rs.getString("deptName");
                int deptManagerNumber = rs.getInt("deptManagerNumber");

                System.out.printf("%-10d %-30s %-20d %n", deptCode, deptName, deptManagerNumber);
            }

            conn.commit();

        } catch (SQLException e) {
            System.out.println("Error viewing departments: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void editDepartmentName() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Department Code:");
        int deptCode = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement fetchPstmt = null;
        PreparedStatement updatePstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            // Fetch and display current department details
            String fetchSql = "SELECT deptCode, deptName, deptManagerNumber FROM departments WHERE deptCode = ? FOR UPDATE";
            fetchPstmt = conn.prepareStatement(fetchSql);
            fetchPstmt.setInt(1, deptCode);

            rs = fetchPstmt.executeQuery();

            fetchPstmt = conn.prepareStatement("SELECT SLEEP (3)");
            fetchPstmt.executeQuery();
            if (rs.next()) {
                String deptName = rs.getString("deptName");
                int deptManagerNumber = rs.getInt("deptManagerNumber");

                System.out.println("Current Department Details:");
                System.out.println("Department Code: " + deptCode);
                System.out.println("Department Name: " + deptName);
                System.out.println("Manager Number: " + deptManagerNumber);
            } else {
                System.out.println("No department found with the given code.");
                return;
            }


            // Input new department name
            System.out.println("Enter New Department Name:");
            String newDeptName = sc.nextLine();

            // Update department name
            String updateSql = "UPDATE departments SET deptName = ? WHERE deptCode = ?";
            updatePstmt = conn.prepareStatement(updateSql);

            updatePstmt.setString(1, newDeptName);
            updatePstmt.setInt(2, deptCode);

            int rowsUpdated = updatePstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Department name updated successfully.");
            } else {
                System.out.println("No department found with the given code.");
            }

            conn.commit();

        } catch (SQLException e) {
            System.out.println("Error updating department name: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (fetchPstmt != null) fetchPstmt.close();
                if (updatePstmt != null) updatePstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    public void assignDepartmentManager() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Department Code:");
        int deptCode = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement fetchPstmt = null;
        PreparedStatement SleepPstmt = null;
        PreparedStatement updatePstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            // Fetch and display current department details
            String fetchSql = "SELECT deptName, deptManagerNumber FROM departments WHERE deptCode = ? FOR UPDATE";
            fetchPstmt = conn.prepareStatement(fetchSql);
            fetchPstmt.setInt(1, deptCode);

            rs = fetchPstmt.executeQuery();
            if (rs.next()) {
                String deptName = rs.getString("deptName");
                int currentManagerNumber = rs.getInt("deptManagerNumber");

                System.out.println("Current Department Details:");
                System.out.println("Department Code: " + deptCode);
                System.out.println("Department Name: " + deptName);
                System.out.println("Current Manager Number: " + currentManagerNumber);
            } else {
                System.out.println("No department found with the given code.");
                return;
            }
            rs.close();

            fetchPstmt = conn.prepareStatement("SELECT SLEEP (3)");
            fetchPstmt.executeQuery();

            fetchPstmt.close();

            // Input new department manager details
            System.out.println("Enter New Department Manager Number:");
            int newManagerNumber = Integer.parseInt(sc.nextLine());

            // Update department with new manager details
            String updateSql = "UPDATE departments SET deptManagerNumber = ? WHERE deptCode = ?";
            updatePstmt = conn.prepareStatement(updateSql);

            updatePstmt.setInt(1, newManagerNumber);
            updatePstmt.setInt(2, deptCode);

            int rowsUpdated = updatePstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Department manager updated successfully.");
            } else {
                System.out.println("No department found with the given code.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating department manager: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (fetchPstmt != null) fetchPstmt.close();
                if (updatePstmt != null) updatePstmt.close();
                if (conn != null) {
                    conn.commit();
                    conn.close();}
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void deleteDepartment() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Department Code:");
        int deptCode = Integer.parseInt(sc.nextLine());

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            // Lock the departments table for writing
            String fetchSql = "SELECT * FROM departments WHERE deptCode = ? FOR UPDATE";
            pstmt = conn.prepareStatement(fetchSql);
            pstmt.setInt(1, deptCode);

            pstmt = conn.prepareStatement("SELECT SLEEP (3)");
            pstmt.executeQuery();


            String sql = "DELETE FROM departments WHERE deptCode = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, deptCode);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Department deleted successfully.");
            } else {
                System.out.println("No department found with the given code.");
            }

            conn.commit();

        } catch (SQLException e) {
            System.out.println("Error deleting department: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        employees e = new employees();

        do {
            // Displaying the main menu options to the user
            System.out.println("Choose a category: \n" +
                               "  [1] - EMPLOYEE \n" +
                               "  [2] - SALES REPRESENTATIVE\n" +
                               "  [3] - DEPARTMENTS \n" +
                               "  [4] - OFFICES \n" +
                               "  [0] - Exit");

            choice = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            switch (choice) {
                case 1: // Employee
                    int employeeChoice;
                    do {
                        System.out.println("Enter Activity: \n" +
                                           "  [1] - CREATE AN EMPLOYEE \n" +
                                           "  [2] - RECLASSIFY EMPLOYEE \n" +
                                           "  [3] - RESIGN EMPLOYEE \n" +
                                           "  [4] - VIEW BASIC EMPLOYEE RECORD \n" +
                                           "  [5] - MODIFY EMPLOYEE RECORD \n" +
                                           "  [0] - EXIT");

                        employeeChoice = sc.nextInt();
                        sc.nextLine(); // Consume the newline character

                        switch (employeeChoice) {
                            case 1:
                                e.addEmployee();
                                break;
                            case 2:
                                e.reclassifyEmployee();
                                break;
                            case 3:
                                e.resignEmployee();
                                break;
                            case 4:
                                e.viewEmployee();
                                break;
                            case 5:
                                e.modifyEmployee();
                                break;
                            case 0:
                                System.out.println("Returning to main menu...");
                                break;
                            default:
                                System.out.println("Invalid choice, please try again.");
                        }

                        if (employeeChoice != 0) {
                            System.out.println("Press enter key to continue....");
                            sc.nextLine();
                        }

                    } while (employeeChoice != 0);
                    break;

                case 2: // SalesRep
                    int salesRepChoice;
                    do {
                        System.out.println("Enter Activity: \n" +
                                           "  [1] - CREATE NEW SALES REP ASSIGNMENT FOR SALES REP ONLY \n" +
                                           "  [2] - VIEW SALES REPRESENTATIVE DETAILS \n" +
                                           "  [3] - EDIT SALES REPRESENTATIVE ASSIGNMENT DETAILS \n" +
                                           "  [0] - EXIT");

                        salesRepChoice = sc.nextInt();
                        sc.nextLine(); // Consume the newline character

                        switch (salesRepChoice) {
                            case 1:
                                e.createSalesRepAssign();
                                break;
                            case 2:
                                e.viewSalesRepDetails();
                                break;
                            case 3:
                                e.modifySalesRepDetails();
                                break;
                            case 0:
                                System.out.println("Returning to main menu...");
                                break;
                            default:
                                System.out.println("Invalid choice, please try again.");
                        }

                        if (salesRepChoice != 0) {
                            System.out.println("Press enter key to continue....");
                            sc.nextLine();
                        }

                    } while (salesRepChoice != 0);
                    break;

                case 3: // Department
                    int departmentChoice;
                    do {
                        System.out.println("Enter Type: \n" +
                                           "  [1] - ADD DEPARTMENT \n" +
                                           "  [2] - VIEW DEPARTMENT \n" +
                                           "  [3] - EDIT DEPARTMENT NAME \n" +
                                           "  [4] - ASSIGN DEPARTMENT MANAGER TO DEPARTMENT \n" +
                                           "  [5] - DELETE DEPARTMENT \n" +
                                           "  [0] - EXIT");

                        departmentChoice = sc.nextInt();
                        sc.nextLine(); // Consume the newline character

                        switch (departmentChoice) {
                            case 1:
                                e.addDepartment();
                                break;
                            case 2:
                                e.viewDepartments();
                                break;
                            case 3:
                                e.editDepartmentName();
                                break;
                            case 4:
                                e.assignDepartmentManager();
                                break;
                            case 5:
                                e.deleteDepartment();
                                break;
                            case 0:
                                System.out.println("Returning to main menu...");
                                break;
                            default:
                                System.out.println("Invalid choice, please try again.");
                        }

                        if (departmentChoice != 0) {
                            System.out.println("Press enter key to continue....");
                            sc.nextLine();
                        }

                    } while (departmentChoice != 0);
                    break;
                case 4: // Offices
                    // Add logic for offices if needed
                    break;

                case 0:
                    System.out.println("Exiting the program...");
                    break;

                default:
                    System.out.println("Invalid choice, please try again.");
            }

            if (choice != 0) {
                System.out.println("Press enter key to continue....");
                sc.nextLine();
            }

        } while (choice != 0);

        sc.close();
    }
}
