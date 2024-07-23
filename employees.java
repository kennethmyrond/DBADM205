import java.sql.*;
import java.util.*;
import java.time.LocalDate;

public class employees {
    // Database URL
    public String url = "jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205";
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
    	Scanner sc = new Scanner(System.in);
        String sql = null;
        int range=0;
        String baseSql = null;

        // \n [1] - All Current Sales Representatives \n [2] - All Previous Sales Representatives
        System.out.println("Enter Type of Record to View: \n [0] - All Sales Representatives record  \n [1] - Record of Specific Sales Rep");
        int recordType = Integer.parseInt(sc.nextLine());

        if(recordType == 0 ){
            System.out.println("Enter Range: \n [0] - Current Sales Representatives \n [1] - Previous Sales Representatives \n [2] - All Records");
            range = Integer.parseInt(sc.nextLine());
            baseSql =    "SELECT sra.employeeNumber, \r\n" + 
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
                                "JOIN employees em ON em.employeeNumber = sra.employeeNumber    \r\n";
        }
        else if(recordType == 1){
            System.out.println("Enter Employee ID:");
            employeeID = Integer.parseInt(sc.nextLine());

            System.out.println("Enter Range: \n [0] - Current \n [1] - Previous \n [2] - All Assignments");
            range = Integer.parseInt(sc.nextLine());
            baseSql =    "SELECT \r\n" + 
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
                condition = "WHERE (sra.endDate IS NULL OR sra.endDate > CURDATE()) \r\n";
                break;
            case 1:
                condition = "WHERE sra.endDate <= CURDATE() \r\n";
                break;
            case 2:
                // No additional condition needed for case 2
                break;
        }
        sql = baseSql + condition + "LOCK IN SHARE MODE;";

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement sleeppstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            if(recordType==1) pstmt.setInt(1, employeeID);

            System.out.println("Press Enter to Start Viewing Sales Rep Assignment");
            sc.nextLine();

            rs = pstmt.executeQuery();

            sleeppstmt = conn.prepareStatement("SELECT SLEEP (3)");
            sleeppstmt.executeQuery();

            boolean assignmentsFound = false;
            
            while (rs.next()) {
            	assignmentsFound = true;
                String SalesRepName = null;
                if(recordType==0){
                    employeeID      = rs.getInt("employeeNumber");
                    SalesRepName = rs.getString("firstName") + ' ' + rs.getString("lastName");
                }
            	officeCode     		= rs.getString("officeCode");
            	startDate     		= rs.getString("startDate");
            	endDate 			= rs.getString("endDate");
            	reason        		= rs.getString("reason");
            	quota            	= rs.getDouble("quota");
            	salesManagerNumber	= rs.getInt("salesManagerNumber");
            	String salesManagerName	= rs.getString("salesManagerFirstName") + ' ' + rs.getString("salesManagerLastName");
            	
                if(recordType==0){
                    System.out.println("Employee Number: " + employeeID);
                    System.out.println("Employee Name: " + SalesRepName);
                }
            	System.out.println("Office Code: " + officeCode);
                System.out.println("Start Date: " + startDate);
                System.out.println("End Date: " + endDate);
                System.out.println("Reason: " + reason);
                System.out.println("Quota: " + quota);
                System.out.println("Sales Manager Number: " + salesManagerNumber);
                System.out.println("Sales Manager Name: " + salesManagerName);
                System.out.println("-----------------------------------");
            }

            if (!assignmentsFound) {
                System.out.println("No records found");
            }

            rs.close();
            pstmt.close();
            sleeppstmt.close();
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

        System.out.println("Enter your employee number:");
        int employeeRoleNo = sc.nextInt();

        boolean isSalesManager = false;

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://mysql-176128-0.cloudclusters.net:10107/dbsalesV2.5G205?useTimezone=true&serverTimezone=UTC&user=DBADM_205&password=DLSU1234!");
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

        employees e = new employees();

        do {
            // Displaying the menu options to the user
            System.out.println( "Enter Type: \n" +
                                "  [0] - CREATE AN EMPLOYEE \n" +
                                "  [1] - RECLASSIFY EMPLOYEE \n" +
                                "  [2] - RESIGN EMPLOYEE \n" +
                                "  [3] - CREATE NEW SALES REP ASSIGNMENT FOR SALES REP ONLY \n" +
                                "  [4] - VIEW SALES REPRESENTATIVE DETAILS \n" +
                                "  [5] - VIEW BASIC EMPLOYEE RECORD \n" +
                                "  [6] - MODIFY EMPLOYEE RECORD \n" +
                                "  [7] - ASSIGN DEPARTMENT MANAGER TO DEPARTMENT \n" +
                                "  [9] - EXIT");

            choice = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            // Handling user choices
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
                case 6:
                    e.modifyEmployee();
                    break;
                case 7:
                    e.assignDepartmentManager();
                    break;
                case 9:
                    System.out.println("Exiting the program...");
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }

            if (choice != 9) {
                System.out.println("Press enter key to continue....");
                sc.nextLine();
            }

        } while (choice != 9);

        sc.close();
    }
}
